package fr.anthognie.FFA.game;

import fr.anthognie.Core.managers.EconomyManager;
import fr.anthognie.FFA.Main;
import fr.anthognie.Core.managers.ItemConfigManager;
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

public class FFAManager {

    private final Main plugin;
    private final Location spawnLocation;
    private final World ffaWorld;
    private final ConfigManager ffaConfigManager;
    private final List<Location> spawnPoints;
    private final Random random = new Random();

    private final ItemConfigManager itemConfigManager;
    private final EconomyManager economyManager;

    private ItemStack camoHelmet, camoChestplate, camoLeggings, camoBoots;

    private final Map<UUID, BukkitTask> regenTasks = new HashMap<>();
    private final Set<UUID> invinciblePlayers = new HashSet<>();
    private final Map<UUID, BukkitTask> invincibilityTasks = new HashMap<>();

    // Tâche pour forcer la nourriture
    private BukkitTask foodTask;

    public FFAManager(Main plugin, ItemConfigManager itemConfigManager, ConfigManager ffaConfigManager) {
        this.plugin = plugin;
        this.itemConfigManager = itemConfigManager;
        this.economyManager = plugin.getEconomyManager();
        this.ffaConfigManager = ffaConfigManager;

        World defaultWorld = Bukkit.getWorld("world");
        this.spawnLocation = (defaultWorld != null) ? defaultWorld.getSpawnLocation() : new Location(Bukkit.getWorlds().get(0), 0, 100, 0);

        this.ffaWorld = Bukkit.getWorld("ffa");
        this.spawnPoints = ffaConfigManager.getSpawnLocations();

        createCamoArmor();
        startFoodTask();
    }

    private void startFoodTask() {
        foodTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (ffaWorld == null) return;
                for (Player p : ffaWorld.getPlayers()) {
                    if (p.getFoodLevel() < 20 || p.getSaturation() < 20) {
                        p.setFoodLevel(20);
                        p.setSaturation(20f);
                        p.setExhaustion(0f);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void createCamoArmor() {
        Color camoGreen = Color.fromRGB(85, 107, 47);
        camoHelmet = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta helmetMeta = (LeatherArmorMeta) camoHelmet.getItemMeta();
        helmetMeta.setColor(camoGreen); helmetMeta.setUnbreakable(true); camoHelmet.setItemMeta(helmetMeta);
        camoChestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta chestMeta = (LeatherArmorMeta) camoChestplate.getItemMeta();
        chestMeta.setColor(camoGreen); chestMeta.setUnbreakable(true); camoChestplate.setItemMeta(chestMeta);
        camoLeggings = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta legsMeta = (LeatherArmorMeta) camoLeggings.getItemMeta();
        legsMeta.setColor(camoGreen); legsMeta.setUnbreakable(true); camoLeggings.setItemMeta(legsMeta);
        camoBoots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) camoBoots.getItemMeta();
        bootsMeta.setColor(camoGreen); bootsMeta.setUnbreakable(true); camoBoots.setItemMeta(bootsMeta);
    }

    // --- REGEN COD ---
    public void handlePlayerDamage(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (regenTasks.containsKey(playerUUID)) {
            regenTasks.get(playerUUID).cancel();
            regenTasks.remove(playerUUID);
        }
        BukkitTask delayTask = new BukkitRunnable() {
            @Override
            public void run() {
                startHealing(player);
            }
        }.runTaskLater(plugin, 100L); // 5 secondes
        regenTasks.put(playerUUID, delayTask);
    }

    private void startHealing(Player player) {
        UUID playerUUID = player.getUniqueId();
        BukkitTask healingTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || player.isDead() || !player.getWorld().getName().equals(getFFAWorldName())) {
                    this.cancel();
                    regenTasks.remove(playerUUID);
                    return;
                }
                double maxHealth = player.getMaxHealth();
                double currentHealth = player.getHealth();
                if (currentHealth < maxHealth) {
                    player.setHealth(Math.min(maxHealth, currentHealth + 1.0));
                } else {
                    this.cancel();
                    regenTasks.remove(playerUUID);
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
        regenTasks.put(playerUUID, healingTask);
    }

    public void clearRegenTask(Player player) {
        if (regenTasks.containsKey(player.getUniqueId())) {
            regenTasks.get(player.getUniqueId()).cancel();
            regenTasks.remove(player.getUniqueId());
        }
    }

    // --- INVINCIBILITÉ ---
    public boolean isInvincible(Player player) { return invinciblePlayers.contains(player.getUniqueId()); }

    public void clearInvincibility(Player player) {
        UUID uuid = player.getUniqueId();
        invinciblePlayers.remove(uuid);
        if (invincibilityTasks.containsKey(uuid)) {
            invincibilityTasks.get(uuid).cancel();
            invincibilityTasks.remove(uuid);
        }
    }

    public void applySpawnProtection(Player player) {
        UUID uuid = player.getUniqueId();
        invinciblePlayers.add(uuid);
        player.sendTitle("", "§a§lProtection de spawn !", 0, 20, 10);
        BukkitTask task = new BukkitRunnable() {
            int ticksLived = 0;
            @Override
            public void run() {
                ticksLived += 2;
                if (!player.isOnline() || !invinciblePlayers.contains(uuid)) {
                    this.cancel();
                    invincibilityTasks.remove(uuid); return;
                }
                if (ticksLived > 200) {
                    this.cancel();
                    invincibilityTasks.remove(uuid); invinciblePlayers.remove(uuid);
                    player.sendTitle("", "§c§lProtection terminée !", 0, 20, 10); return;
                }
                if (player.getWorld().equals(ffaWorld)) {
                    ffaWorld.spawnParticle(Particle.SMOKE_NORMAL, player.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.01);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
        invincibilityTasks.put(uuid, task);
    }

    // --- RESPAWN & DEATH ---
    public void startRespawnSequence(Player player, Player killer) {
        economyManager.clearInventory(player.getUniqueId());
        player.setGameMode(GameMode.SPECTATOR);
        player.setHealth(player.getMaxHealth());
        clearRegenTask(player);
        clearInvincibility(player);
        if (killer != null && killer.isOnline()) player.setSpectatorTarget(killer);

        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 120, 1, false, false, false));

        new BukkitRunnable() {
            int countdown = 5;
            @Override
            public void run() {
                if (killer != null && !killer.isOnline()) player.setSpectatorTarget(null);
                if (countdown > 0) {
                    player.sendTitle("§cVOUS ÊTES MORT", "§fRéapparition dans §e" + countdown + "s", 0, 25, 0);
                    countdown--;
                } else {
                    this.cancel();
                    player.setSpectatorTarget(null);
                    player.removePotionEffect(PotionEffectType.BLINDNESS);
                    respawnPlayer(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    public void startRespawnSequence(Player player) { startRespawnSequence(player, null); }

    public void respawnPlayer(Player player) {
        player.teleport(getRandomSpawnPoint());
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        giveStarterKit(player);
        applySpawnProtection(player);
    }

    public void joinArena(Player player) {
        player.sendMessage("§aVous avez rejoint l'arène FFA !");
        player.teleport(getRandomSpawnPoint());
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        applySpawnProtection(player);

        ItemStack[] savedInventory = economyManager.loadInventory(player.getUniqueId());
        if (savedInventory != null && savedInventory.length > 0) {
            player.getInventory().setContents(savedInventory);
            economyManager.clearInventory(player.getUniqueId());
        } else {
            giveStarterKit(player);
        }
    }

    public void leaveArena(Player player) {
        economyManager.saveInventory(player.getUniqueId(), player.getInventory().getContents());
        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.teleport(spawnLocation);
        clearRegenTask(player);
        clearInvincibility(player);
    }

    private Location getRandomSpawnPoint() {
        if (spawnPoints.isEmpty()) return this.ffaWorld.getSpawnLocation();
        return spawnPoints.get(random.nextInt(spawnPoints.size()));
    }

    public void giveStarterKit(Player player) {
        PlayerInventory inventory = player.getInventory();
        inventory.clear();

        ItemStack pistol = itemConfigManager.getItemStack("kits.ffa.pistol");
        ItemStack bullets = itemConfigManager.getItemStack("kits.ffa.bullets");

        if (pistol != null) inventory.addItem(pistol.clone());
        if (bullets != null) inventory.addItem(bullets.clone());

        inventory.setHelmet(camoHelmet); inventory.setChestplate(camoChestplate);
        inventory.setLeggings(camoLeggings); inventory.setBoots(camoBoots);
    }

    public String getFFAWorldName() { return (this.ffaWorld == null) ? "ffa" : this.ffaWorld.getName(); }
}