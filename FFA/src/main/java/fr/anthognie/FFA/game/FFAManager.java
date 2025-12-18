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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class FFAManager {

    private final Main plugin;
    private final Location spawnLocation;
    private final World ffaWorld;
    private final ConfigManager ffaConfigManager;
    private final List<Location> spawnPoints;
    private final Random random = new Random();

    private final ItemConfigManager itemConfigManager;
    private final EconomyManager economyManager;
    private ItemStack ffaPistol;
    private ItemStack ffaBullets;

    private ItemStack camoHelmet, camoChestplate, camoLeggings, camoBoots;

    private final Map<UUID, BukkitTask> regenTasks = new HashMap<>();
    private final Set<UUID> invinciblePlayers = new HashSet<>();
    private final Map<UUID, BukkitTask> invincibilityTasks = new HashMap<>();
    private final Set<UUID> headshotCache = new HashSet<>();

    public FFAManager(Main plugin, ItemConfigManager itemConfigManager, ConfigManager ffaConfigManager) {
        this.plugin = plugin;
        this.itemConfigManager = itemConfigManager;
        this.economyManager = plugin.getEconomyManager();
        this.ffaConfigManager = ffaConfigManager;
        this.spawnLocation = new Location(org.bukkit.Bukkit.getWorld("world"), 0.5, 100, 0.5);
        this.ffaWorld = org.bukkit.Bukkit.getWorld("ffa");
        this.spawnPoints = ffaConfigManager.getSpawnLocations();

        loadKits();
        createCamoArmor();
    }

    private void createCamoArmor() {
        Color camoGreen = Color.fromRGB(85, 107, 47);
        camoHelmet = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta helmetMeta = (LeatherArmorMeta) camoHelmet.getItemMeta();
        helmetMeta.setColor(camoGreen);
        helmetMeta.setUnbreakable(true);
        camoHelmet.setItemMeta(helmetMeta);
        camoChestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta chestMeta = (LeatherArmorMeta) camoChestplate.getItemMeta();
        chestMeta.setColor(camoGreen);
        chestMeta.setUnbreakable(true);
        camoChestplate.setItemMeta(chestMeta);
        camoLeggings = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta legsMeta = (LeatherArmorMeta) camoLeggings.getItemMeta();
        legsMeta.setColor(camoGreen);
        legsMeta.setUnbreakable(true);
        camoLeggings.setItemMeta(legsMeta);
        camoBoots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) camoBoots.getItemMeta();
        bootsMeta.setColor(camoGreen);
        bootsMeta.setUnbreakable(true);
        camoBoots.setItemMeta(bootsMeta);
    }

    public void loadKits() {
        this.ffaPistol = itemConfigManager.getItemStack("kits.ffa.pistol");
        this.ffaBullets = itemConfigManager.getItemStack("kits.ffa.bullets");
    }

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
        }.runTaskLater(plugin, 100L);
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
                if (player.getHealth() < player.getMaxHealth()) {
                    player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 2.0));
                } else {
                    this.cancel();
                    regenTasks.remove(playerUUID);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
        regenTasks.put(playerUUID, healingTask);
    }

    public void clearRegenTask(Player player) {
        if (regenTasks.containsKey(player.getUniqueId())) {
            regenTasks.get(player.getUniqueId()).cancel();
            regenTasks.remove(player.getUniqueId());
        }
    }

    public boolean isInvincible(Player player) {
        return invinciblePlayers.contains(player.getUniqueId());
    }

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
                    invincibilityTasks.remove(uuid);
                    return;
                }
                if (ticksLived > 200) { // 10 secondes
                    this.cancel();
                    invincibilityTasks.remove(uuid);
                    invinciblePlayers.remove(uuid);
                    player.sendTitle("", "§c§lProtection terminée !", 0, 20, 10);
                    return;
                }
                if (player.getWorld().equals(ffaWorld)) {
                    ffaWorld.spawnParticle(
                            Particle.SMOKE_NORMAL,
                            player.getLocation().add(0, 1, 0),
                            10, 0.3, 0.5, 0.3, 0.01
                    );
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
        invincibilityTasks.put(uuid, task);
    }

    public void startRespawnSequence(Player player, Player killer) {
        player.setGameMode(GameMode.SPECTATOR);
        player.setHealth(player.getMaxHealth());
        clearRegenTask(player);
        clearInvincibility(player);

        if (killer != null && killer.isOnline()) {
            player.setSpectatorTarget(killer);
        }

        new BukkitRunnable() {
            int countdown = 5;
            @Override
            public void run() {
                if (killer != null && !killer.isOnline()) {
                    player.setSpectatorTarget(null);
                }
                if (countdown > 0) {
                    player.sendTitle("§cVOUS ÊTES MORT", "§fRéapparition dans §e" + countdown + "s", 0, 25, 0);
                    countdown--;
                } else {
                    this.cancel();
                    player.setSpectatorTarget(null);
                    respawnPlayer(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void startRespawnSequence(Player player) {
        startRespawnSequence(player, null);
    }

    private Location getRandomSpawnPoint() {
        if (spawnPoints.isEmpty()) {
            plugin.getLogger().warning("Aucun point de spawn FFA n'est défini !");
            return this.ffaWorld.getSpawnLocation();
        }
        return spawnPoints.get(random.nextInt(spawnPoints.size()));
    }

    public void respawnPlayer(Player player) {
        player.teleport(getRandomSpawnPoint());
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setFireTicks(0);
        giveStarterKit(player);
        applySpawnProtection(player);
    }

    public void joinArena(Player player) {
        player.sendMessage("§aVous avez rejoint l'arène FFA !");
        player.teleport(getRandomSpawnPoint());
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setFireTicks(0);
        applySpawnProtection(player);

        ItemStack[] savedInventory = economyManager.loadInventory(player.getUniqueId());

        if (savedInventory != null) {
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
        player.teleport(spawnLocation);
        clearRegenTask(player);
        clearInvincibility(player);
    }

    public void giveStarterKit(Player player) {
        PlayerInventory inventory = player.getInventory();
        inventory.clear();

        if (this.ffaPistol != null) {
            inventory.addItem(this.ffaPistol.clone());
        } else {
            player.sendMessage("§cKit 'kits.ffa.pistol' non défini. /itemdb");
        }
        if (this.ffaBullets != null) {
            inventory.addItem(this.ffaBullets.clone());
        } else {
            player.sendMessage("§cKit 'kits.ffa.bullets' non défini. /itemdb");
        }

        inventory.setHelmet(camoHelmet);
        inventory.setChestplate(camoChestplate);
        inventory.setLeggings(camoLeggings);
        inventory.setBoots(camoBoots);
    }

    public String getFFAWorldName() {
        if (this.ffaWorld == null) {
            return "ffa_INVALIDE";
        }
        return this.ffaWorld.getName();
    }

    // --- MÉTHODES HEADSHOT (QUI MANQUAIENT) ---

    /**
     * Appelé par le "sniffer" ProtocolLib quand un son de headshot est détecté.
     * On met le tueur dans le cache pour 1/10e de seconde (2 ticks).
     */
    public void recordHeadshot(Player killer) {
        UUID uuid = killer.getUniqueId();
        headshotCache.add(uuid);

        // On le retire du cache après 2 ticks
        new BukkitRunnable() {
            @Override
            public void run() {
                headshotCache.remove(uuid);
            }
        }.runTaskLater(plugin, 2L); // 2 ticks, c'est très court
    }

    /**
     * Appelé par le PlayerDamageListener pour vérifier si le kill était un headshot.
     * @return true si le tueur est dans le cache (s'il a fait un headshot y'a 2 ticks)
     */
    public boolean checkAndConsumeHeadshot(Player killer) {
        // On vérifie et on retire (consomme) le headshot
        return headshotCache.remove(killer.getUniqueId());
    }
}