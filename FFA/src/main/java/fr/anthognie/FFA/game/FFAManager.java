package fr.anthognie.FFA.game;

import fr.anthognie.Core.managers.EconomyManager;
import fr.anthognie.Core.managers.ItemConfigManager;
import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.managers.ConfigManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class FFAManager {

    private final Main plugin;
    private final World ffaWorld;
    private final ConfigManager ffaConfigManager;
    private final ItemConfigManager itemConfigManager;
    private final EconomyManager economyManager;

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

        // On charge le monde FFA
        String worldName = ffaConfigManager.getConfig().getString("ffa-world", "ffa");
        this.ffaWorld = Bukkit.getWorld(worldName);

        createCamoArmor();
        startFoodTask();
    }

    public void joinArena(Player player) {
        if (ffaWorld == null) {
            player.sendMessage("§cERREUR : Le monde FFA n'est pas chargé !");
            return;
        }

        // --- CORRECTION CRITIQUE ---
        // On récupère les spawns
        List<Location> spawns = ffaConfigManager.getSpawnLocations();

        // Si la liste est vide ou nulle, ON NE TÉLÉPORTE PAS.
        // C'est ça qui évitera de tomber du ciel (le fameux Y=100).
        if (spawns == null || spawns.isEmpty()) {
            player.sendMessage("§c§lERREUR : §cAucun point de spawn défini !");
            player.sendMessage("§eUtilisez /addspawn dans l'arène avant de jouer.");
            return;
        }

        // Sauvegarde inventaire
        economyManager.saveInventory(player.getUniqueId(), player.getInventory().getContents());

        // Téléportation sur un spawn aléatoire SÛR
        player.teleport(getRandomSpawnPoint());

        setupPlayerForGame(player);
        player.sendMessage("§aVous avez rejoint l'arène FFA !");
    }

    // Alias pour GameSelector
    public void joinFFA(Player player) { joinArena(player); }

    public void leaveArena(Player player) {
        player.getInventory().clear();

        // Restauration inventaire
        ItemStack[] saved = economyManager.loadInventory(player.getUniqueId());
        if (saved != null) {
            player.getInventory().setContents(saved);
            economyManager.clearInventory(player.getUniqueId());
        }

        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.removePotionEffect(PotionEffectType.BLINDNESS);

        // Retour au spawn principal du serveur
        if (plugin.getServer().getWorlds().size() > 0) {
            player.teleport(plugin.getServer().getWorlds().get(0).getSpawnLocation());
        }

        clearRegenTask(player);
        clearInvincibility(player);
    }

    public void setupPlayerForGame(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        giveStarterKit(player);
        applySpawnProtection(player);
    }

    private Location getRandomSpawnPoint() {
        List<Location> spawns = ffaConfigManager.getSpawnLocations();
        // Sécurité supplémentaire
        if (spawns == null || spawns.isEmpty()) return null;
        return spawns.get(ThreadLocalRandom.current().nextInt(spawns.size()));
    }

    public void startRespawnSequence(Player player, Player killer) {
        player.setGameMode(GameMode.SPECTATOR);
        player.setHealth(player.getMaxHealth());
        clearRegenTask(player);
        clearInvincibility(player);

        if (killer != null && killer.isOnline()) {
            player.setSpectatorTarget(killer);
            player.sendMessage("§cTué par §e" + killer.getName());
        }

        player.sendTitle("§cVOUS ÊTES MORT", "§7Respawn dans 3s...", 0, 60, 10);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                player.setSpectatorTarget(null);
                respawnPlayer(player);
            }
        }.runTaskLater(plugin, 60L);
    }

    public void startRespawnSequence(Player player) { startRespawnSequence(player, null); }

    public void respawnPlayer(Player player) {
        Location spawn = getRandomSpawnPoint();
        if (spawn != null) {
            player.teleport(spawn);
            setupPlayerForGame(player);
        } else {
            // Si les spawns ont disparu entre temps, on kick au lobby pour sécurité
            leaveArena(player);
            player.sendMessage("§cErreur de spawn, retour au lobby.");
        }
    }

    // --- LE RESTE (Camo, Regen, etc.) inchangé ---

    private void createCamoArmor() {
        Color camoGreen = Color.fromRGB(85, 107, 47);
        camoHelmet = createArmor(Material.LEATHER_HELMET, camoGreen);
        camoChestplate = createArmor(Material.LEATHER_CHESTPLATE, camoGreen);
        camoLeggings = createArmor(Material.LEATHER_LEGGINGS, camoGreen);
        camoBoots = createArmor(Material.LEATHER_BOOTS, camoGreen);
    }

    private ItemStack createArmor(Material mat, Color c) {
        ItemStack i = new ItemStack(mat);
        LeatherArmorMeta m = (LeatherArmorMeta) i.getItemMeta();
        m.setColor(c); m.setUnbreakable(true); i.setItemMeta(m);
        return i;
    }

    private void startFoodTask() {
        // Force la nourriture UNIQUEMENT dans le monde FFA
        foodTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (ffaWorld == null) return;
                for (Player p : ffaWorld.getPlayers()) {
                    if (p.getFoodLevel() < 20) p.setFoodLevel(20);
                }
            }
        }.runTaskTimer(plugin, 0L, 40L);
    }

    public void handlePlayerDamage(Player player) {
        UUID id = player.getUniqueId();
        if (regenTasks.containsKey(id)) regenTasks.get(id).cancel();

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                startHealing(player);
            }
        }.runTaskLater(plugin, 100L);
        regenTasks.put(id, task);
    }

    private void startHealing(Player player) {
        UUID id = player.getUniqueId();
        BukkitTask t = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || player.getHealth() >= player.getMaxHealth()) {
                    this.cancel(); regenTasks.remove(id); return;
                }
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 1));
            }
        }.runTaskTimer(plugin, 0L, 10L);
        regenTasks.put(id, t);
    }

    public void clearRegenTask(Player player) {
        if (regenTasks.containsKey(player.getUniqueId())) {
            regenTasks.get(player.getUniqueId()).cancel();
            regenTasks.remove(player.getUniqueId());
        }
    }

    public void giveStarterKit(Player player) {
        PlayerInventory inventory = player.getInventory();
        inventory.clear();
        ItemStack pistol = itemConfigManager.getItemStack("kits.ffa.pistol");
        ItemStack bullets = itemConfigManager.getItemStack("kits.ffa.bullets");
        if (pistol != null) inventory.addItem(pistol.clone());
        if (bullets != null) inventory.addItem(bullets.clone());
        inventory.setHelmet(camoHelmet);
        inventory.setChestplate(camoChestplate);
        inventory.setLeggings(camoLeggings);
        inventory.setBoots(camoBoots);
    }

    public boolean isInvincible(Player player) { return invinciblePlayers.contains(player.getUniqueId()); }

    public void clearInvincibility(Player player) {
        UUID u = player.getUniqueId();
        invinciblePlayers.remove(u);
        if (invincibilityTasks.containsKey(u)) {
            invincibilityTasks.get(u).cancel();
            invincibilityTasks.remove(u);
        }
    }

    public void applySpawnProtection(Player player) {
        // Code d'origine
        UUID uuid = player.getUniqueId();
        invinciblePlayers.add(uuid);
        player.sendTitle("", "§a§lProtection de spawn !", 0, 20, 10);
        BukkitTask task = new BukkitRunnable() {
            int ticksLived = 0;
            @Override
            public void run() {
                ticksLived += 2;
                if (!player.isOnline() || !invinciblePlayers.contains(uuid)) { this.cancel(); invincibilityTasks.remove(uuid); return; }
                if (ticksLived > 200) { this.cancel(); clearInvincibility(player); player.sendTitle("", "§c§lProtection terminée !", 0, 20, 10); return; }
                if (player.getWorld().equals(ffaWorld)) ffaWorld.spawnParticle(Particle.SMOKE_NORMAL, player.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.01);
            }
        }.runTaskTimer(plugin, 0L, 2L);
        invincibilityTasks.put(uuid, task);
    }

    public String getFFAWorldName() { return (ffaWorld == null) ? "ffa" : ffaWorld.getName(); }
}