package fr.anthognie.FFA.game;

import fr.anthognie.Core.managers.EconomyManager;
import fr.anthognie.Core.managers.ItemConfigManager;
import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.managers.ConfigManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class FFAManager {

    private final Main plugin;
    private final World ffaWorld;
    private final ConfigManager ffaConfigManager;
    private final ItemConfigManager itemConfigManager;
    private final EconomyManager economyManager;
    private final Location lobbyLocation;

    // --- COMBAT TAG ---
    private final Map<UUID, Long> combatTagTimer = new HashMap<>();
    private final Map<UUID, UUID> lastAttacker = new HashMap<>();

    private ItemStack camoHelmet, camoChestplate, camoLeggings, camoBoots;
    private final Map<UUID, BukkitTask> regenTasks = new HashMap<>();
    private final Set<UUID> invinciblePlayers = new HashSet<>();
    private final Map<UUID, BukkitTask> invincibilityTasks = new HashMap<>();
    private BukkitTask foodTask;

    public FFAManager(Main plugin, ItemConfigManager itemConfigManager, ConfigManager ffaConfigManager) {
        this.plugin = plugin;
        this.itemConfigManager = itemConfigManager;
        this.economyManager = plugin.getEconomyManager();
        this.ffaConfigManager = ffaConfigManager;

        World defaultWorld = Bukkit.getWorld("world");
        this.lobbyLocation = (defaultWorld != null) ? defaultWorld.getSpawnLocation() : null;

        String worldName = ffaConfigManager.getConfig().getString("ffa-world", "ffa");
        this.ffaWorld = Bukkit.getWorld(worldName);

        createCamoArmor();
        startFoodTask();
    }

    public void joinArena(Player player) {
        if (ffaWorld == null) {
            player.sendMessage("§cMonde FFA introuvable.");
            return;
        }

        economyManager.saveInventory(player.getUniqueId(), player.getInventory().getContents());

        // Essai de téléportation aux spawns définis dans ffa.yml
        boolean ported = teleportToRandomSpawn(player);
        if (!ported) {
            player.sendMessage("§cAttention: Aucun spawn trouvé dans ffa.yml, téléportation au spawn du monde.");
        }

        player.sendMessage("§a§lFFA §8» §7Bienvenue !");
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_ELYTRA, 1f, 1f);

        // Délai pour donner le kit (évite les bugs d'inventaire)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) setupPlayerForGame(player);
            }
        }.runTaskLater(plugin, 5L);
    }

    public void leaveArena(Player player) {
        player.getInventory().clear();
        ItemStack[] saved = economyManager.loadInventory(player.getUniqueId());
        if (saved != null) {
            player.getInventory().setContents(saved);
            economyManager.clearInventory(player.getUniqueId());
        }
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.removePotionEffect(PotionEffectType.BLINDNESS);

        if (lobbyLocation != null)
            player.teleport(lobbyLocation);

        clearRegenTask(player);
        clearInvincibility(player);

        // Nettoyage combat tag
        combatTagTimer.remove(player.getUniqueId());
        lastAttacker.remove(player.getUniqueId());
    }

    public void setupPlayerForGame(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);

        for (PotionEffect effect : player.getActivePotionEffects())
            player.removePotionEffect(effect.getType());

        giveStarterKit(player);
        applySpawnProtection(player);
    }

    public void startRespawnSequence(Player player, Player killer) {
        plugin.getKillstreakManager().handleDeath(player);

        if (killer != null && !killer.equals(player)) {
            plugin.getKillstreakManager().handleKill(killer);
            plugin.getScoreboardManager().recordKill(killer, player);
            plugin.getLevelManager().addXp(killer, 100);
            economyManager.addMoney(killer.getUniqueId(), 10);
            killer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§6+10 Coins  §b+100 XP"));
            killer.playSound(killer.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
        }

        player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_DEATH, 1f, 0.5f);
        player.setGameMode(GameMode.SPECTATOR);
        player.setHealth(player.getMaxHealth());

        clearRegenTask(player);
        clearInvincibility(player);
        combatTagTimer.remove(player.getUniqueId()); // Reset du tag à la mort

        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1, false, false));

        if (killer != null) player.setSpectatorTarget(killer);

        new BukkitRunnable() {
            int timer = 5;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }
                if (timer > 0) {
                    player.sendTitle("§c§lVOUS ÊTES MORT", "§7Respawn dans §e" + timer + "s", 0, 25, 0);
                    if (timer <= 3)
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, (timer == 1 ? 2f : timer == 2 ? 1f : 0.5f));
                } else {
                    this.cancel();
                    if (player.getGameMode() == GameMode.SPECTATOR)
                        player.setSpectatorTarget(null);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    respawnPlayer(player);
                }
                timer--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void respawnPlayer(Player player) {
        teleportToRandomSpawn(player);
        // Délai avant de redonner le stuff pour s'assurer que le TP est fini
        new BukkitRunnable() {
            @Override
            public void run() {
                setupPlayerForGame(player);
            }
        }.runTaskLater(plugin, 2L);
    }

    private boolean teleportToRandomSpawn(Player player) {
        List<Location> spawns = ffaConfigManager.getSpawnLocations();

        if (spawns != null && !spawns.isEmpty()) {
            Location loc = spawns.get(ThreadLocalRandom.current().nextInt(spawns.size()));
            player.teleport(loc);
            player.setFallDistance(0);
            return true;
        } else {
            // Fallback: spawn du monde
            if (ffaWorld != null) {
                player.teleport(ffaWorld.getSpawnLocation());
            }
            return false;
        }
    }

    // --- SYSTEME COMBAT TAG ---

    public void tagPlayer(Player victim, Player attacker) {
        if (victim.equals(attacker)) return;
        long time = System.currentTimeMillis();

        // Tag de la victime
        combatTagTimer.put(victim.getUniqueId(), time);
        lastAttacker.put(victim.getUniqueId(), attacker.getUniqueId());

        // Tag de l'attaquant (lui aussi est en combat)
        combatTagTimer.put(attacker.getUniqueId(), time);
        lastAttacker.put(attacker.getUniqueId(), victim.getUniqueId());
    }

    public void checkCombatLog(Player player) {
        if (!combatTagTimer.containsKey(player.getUniqueId())) return;

        long lastHit = combatTagTimer.get(player.getUniqueId());
        // Si dégâts il y a moins de 10 secondes (10000ms)
        if (System.currentTimeMillis() - lastHit < 10000) {

            Bukkit.broadcastMessage("§c§l" + player.getName() + " §7a fui le combat ! (Kill validé)");

            Player killer = null;
            if (lastAttacker.containsKey(player.getUniqueId())) {
                killer = Bukkit.getPlayer(lastAttacker.get(player.getUniqueId()));
            }

            // On compte la mort
            plugin.getKillstreakManager().handleDeath(player);

            // On récompense le tueur
            if (killer != null && killer.isOnline()) {
                plugin.getKillstreakManager().handleKill(killer);
                plugin.getScoreboardManager().recordKill(killer, player);
                economyManager.addMoney(killer.getUniqueId(), 10);
                killer.sendMessage("§aVous avez tué §e" + player.getName() + " §a(Déconnexion) !");
            }

            // Punition: clear inventaire
            player.getInventory().clear();
        }
    }

    public void applySpawnProtection(Player player) {
        UUID uuid = player.getUniqueId();
        invinciblePlayers.add(uuid);

        BukkitTask task = new BukkitRunnable() {
            int timeLeft = 10;
            @Override
            public void run() {
                if (!player.isOnline() || !invinciblePlayers.contains(uuid)) {
                    this.cancel();
                    return;
                }
                if (timeLeft <= 0) {
                    this.cancel();
                    clearInvincibility(player);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§cProtection terminée"));
                    player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1f);
                    return;
                }
                StringBuilder bar = new StringBuilder("§aProtection: §e");
                for (int i = 0; i < timeLeft; i++) bar.append("■");
                for (int i = timeLeft; i < 10; i++) bar.append("§7■");
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(bar.toString() + " §f(" + timeLeft + "s)"));
                player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, player.getLocation().add(0, 1, 0), 5, 0.2, 0.5, 0.2, 0.01);
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        invincibilityTasks.put(uuid, task);
    }

    public boolean isInvincible(Player player) {
        return invinciblePlayers.contains(player.getUniqueId());
    }

    public void clearInvincibility(Player player) {
        UUID u = player.getUniqueId();
        invinciblePlayers.remove(u);
        if (invincibilityTasks.containsKey(u)) {
            invincibilityTasks.get(u).cancel();
            invincibilityTasks.remove(u);
        }
    }

    public void giveStarterKit(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.clear();

        ItemStack pistol = itemConfigManager.getItemStack("kits.ffa.pistol");
        ItemStack bullets = itemConfigManager.getItemStack("kits.ffa.bullets");

        if (pistol != null) {
            inv.addItem(pistol.clone());
        } else {
            inv.addItem(new ItemStack(Material.STONE_SWORD));
        }

        if (bullets != null) {
            inv.addItem(bullets.clone());
        }

        // --- CORRECTION: STEAKS SUPPRIMÉS ICI ---

        inv.setHelmet(camoHelmet);
        inv.setChestplate(camoChestplate);
        inv.setLeggings(camoLeggings);
        inv.setBoots(camoBoots);

        player.updateInventory();
    }

    private void createCamoArmor() {
        Color c = Color.fromRGB(85, 107, 47);
        camoHelmet = i(Material.LEATHER_HELMET, c);
        camoChestplate = i(Material.LEATHER_CHESTPLATE, c);
        camoLeggings = i(Material.LEATHER_LEGGINGS, c);
        camoBoots = i(Material.LEATHER_BOOTS, c);
    }

    private ItemStack i(Material m, Color c) {
        ItemStack s = new ItemStack(m);
        LeatherArmorMeta meta = (LeatherArmorMeta) s.getItemMeta();
        if (meta != null) {
            meta.setColor(c);
            meta.setUnbreakable(true);
            s.setItemMeta(meta);
        }
        return s;
    }

    private void startFoodTask() {
        foodTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (ffaWorld != null)
                    for (Player p : ffaWorld.getPlayers()) {
                        if (p.getFoodLevel() < 20) p.setFoodLevel(20);
                    }
            }
        }.runTaskTimer(plugin, 0L, 40L);
    }

    public void clearRegenTask(Player p) {
        if (regenTasks.containsKey(p.getUniqueId())) {
            regenTasks.get(p.getUniqueId()).cancel();
            regenTasks.remove(p.getUniqueId());
        }
    }

    public void handlePlayerDamage(Player p) {
        UUID id = p.getUniqueId();
        if (regenTasks.containsKey(id)) regenTasks.get(id).cancel();

        // Régénération après 8 secondes (160 ticks)
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!p.isOnline() || p.getHealth() >= p.getMaxHealth() || p.getGameMode() == GameMode.SPECTATOR) {
                            this.cancel();
                            regenTasks.remove(id);
                            return;
                        }
                        p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + 1));
                    }
                }.runTaskTimer(plugin, 0L, 10L);
            }
        }.runTaskLater(plugin, 160L);

        regenTasks.put(id, task);
    }

    public String getFFAWorldName() {
        return (ffaWorld == null) ? "ffa" : ffaWorld.getName();
    }

    public void triggerNuke(Player launcher) {
        if (ffaWorld == null) return;
        Bukkit.broadcastMessage("§4§l⚠ ALERTE NUCLÉAIRE ⚠");
        Bukkit.broadcastMessage("§cUne Nuke Tactique a été lancée par §4" + launcher.getName() + " §c!");
        ffaWorld.setTime(18000);
        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                if (tick >= 10) {
                    this.cancel();
                    return;
                }
                for (Player p : ffaWorld.getPlayers()) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 10f, 0.5f);
                    p.playSound(p.getLocation(), Sound.ENTITY_GHAST_SCREAM, 10f, 0.5f);
                    p.sendTitle("§4§lNUKE EN APPROCHE", "§cImpact dans " + (10 - tick) + "s...", 0, 20, 0);
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : ffaWorld.getPlayers()) {
                    if (p.getUniqueId().equals(launcher.getUniqueId())) {
                        p.sendMessage("§aVous observez la destruction...");
                        continue;
                    }
                    p.playSound(p.getLocation(), Sound.ENTITY_TNT_PRIMED, 10f, 2.0f);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1, false, false));
                    p.sendTitle("§f ", "§f ", 0, 40, 10);
                }
                Bukkit.broadcastMessage("§4§lBOOM !");
            }
        }.runTaskLater(plugin, 200L);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : ffaWorld.getPlayers()) {
                    if (p.getUniqueId().equals(launcher.getUniqueId())) continue;
                    p.setVelocity(new Vector(0, 1.5, 0));
                    p.setHealth(0);
                }
                ffaWorld.setTime(6000);
            }
        }.runTaskLater(plugin, 260L);
    }
}