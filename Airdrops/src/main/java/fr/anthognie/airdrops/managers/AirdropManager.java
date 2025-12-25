package fr.anthognie.airdrops.managers;

import fr.anthognie.airdrops.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AirdropManager {

    private final Main plugin;
    private final LootManager lootManager;
    private final FileConfiguration config;

    private final Set<Location> activeAirdrops = new HashSet<>();
    private final Map<Location, Integer> particleTasks = new HashMap<>();
    private final Map<Location, Integer> despawnTasks = new HashMap<>(); // Tâches de suppression auto (3min)

    private BukkitRunnable normalDropTask;
    private BukkitRunnable ultimateDropTask;
    private BossBar ultimateBossBar;
    private int ultimateTimerSeconds = 900;

    private boolean normalEnabled = false;
    private boolean ultimateEnabled = false;

    private final List<Location> dropLocations = new ArrayList<>();

    public AirdropManager(Main plugin) {
        this.plugin = plugin;
        this.lootManager = plugin.getLootManager();
        this.config = plugin.getConfig();
        loadLocations();
    }

    private void loadLocations() {
        World world = Bukkit.getWorld("ffa");
        if (world == null) return;
        dropLocations.add(new Location(world, 36, 72, -41));
        dropLocations.add(new Location(world, 61, 70, -14));
        // ... (Liste des locs) ...
        dropLocations.add(new Location(world, 3, 100, -23));
    }

    public void startTimers() {
        normalDropTask = new BukkitRunnable() {
            @Override
            public void run() { if (normalEnabled) forceAirdrop(false); }
        };
        normalDropTask.runTaskTimer(plugin, 12000L, 12000L);

        ultimateBossBar = Bukkit.createBossBar("§6Prochain Airdrop Ultime: 15:00", BarColor.RED, BarStyle.SOLID);
        ultimateBossBar.setVisible(ultimateEnabled);

        ultimateDropTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ultimateEnabled) { ultimateBossBar.setVisible(false); return; }
                ultimateBossBar.setVisible(true);
                World ffa = Bukkit.getWorld("ffa");
                if (ffa != null) for (Player p : ffa.getPlayers()) ultimateBossBar.addPlayer(p);

                if (ultimateTimerSeconds > 0) {
                    ultimateTimerSeconds--;
                    int min = ultimateTimerSeconds / 60;
                    int sec = ultimateTimerSeconds % 60;
                    ultimateBossBar.setTitle(String.format("§6Prochain Airdrop Ultime: %02d:%02d", min, sec));
                    ultimateBossBar.setProgress((double) ultimateTimerSeconds / 900.0);
                } else {
                    forceAirdrop(true);
                    ultimateTimerSeconds = 900;
                }
            }
        };
        ultimateDropTask.runTaskTimer(plugin, 0L, 20L);
    }

    public void cancelAllTimers() {
        if (normalDropTask != null) normalDropTask.cancel();
        if (ultimateDropTask != null) ultimateDropTask.cancel();
        if (ultimateBossBar != null) ultimateBossBar.removeAll();
        for (Integer taskId : despawnTasks.values()) Bukkit.getScheduler().cancelTask(taskId);
        despawnTasks.clear();
    }

    public void forceAirdrop(boolean isUltimate) {
        if (dropLocations.isEmpty()) return;
        Location loc = dropLocations.get(new Random().nextInt(dropLocations.size()));
        spawnAirdrop(loc, isUltimate);
    }

    public void spawnRandomAirdrop() { forceAirdrop(false); }
    public void removeAllAirdrops() { resetAllAirdrops(); }

    public void spawnAirdrop(Location location, boolean isUltimate) {
        if (activeAirdrops.contains(location)) removeAirdrop(location);

        Block block = location.getBlock();
        block.setType(Material.CHEST);
        activeAirdrops.add(location);

        if (block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            lootManager.fillChest(chest, isUltimate);
        }

        String typeName = isUltimate ? "§6§lULTIME" : "§eNormal";
        Bukkit.broadcastMessage("§7[§6Airdrop§7] Un Airdrop " + typeName + " §7vient de tomber en §e" + location.getBlockX() + ", " + location.getBlockZ() + " §7!");
        startParticleEffect(location, isUltimate);

        // CORRECTION : Timer de disparition 3 minutes
        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (activeAirdrops.contains(location)) {
                    removeAirdrop(location);
                    Bukkit.broadcastMessage("§7[§6Airdrop§7] L'airdrop en §e" + location.getBlockX() + ", " + location.getBlockZ() + " §7a disparu.");
                    playDisappearEffect(location);
                }
                despawnTasks.remove(location);
            }
        }.runTaskLater(plugin, 3600L).getTaskId(); // 3 * 60 * 20 = 3600 ticks
        despawnTasks.put(location, taskId);
    }

    private void startParticleEffect(Location location, boolean isUltimate) {
        if (particleTasks.containsKey(location)) Bukkit.getScheduler().cancelTask(particleTasks.get(location));
        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!activeAirdrops.contains(location) || location.getBlock().getType() != Material.CHEST) {
                    this.cancel();
                    particleTasks.remove(location);
                    return;
                }
                if (isUltimate) location.getWorld().spawnParticle(Particle.TOTEM, location.clone().add(0.5, 1, 0.5), 10, 0.1, 0.5, 0.1, 0.05);
                else location.getWorld().spawnParticle(Particle.SMOKE_LARGE, location.clone().add(0.5, 1, 0.5), 5, 0.1, 0.5, 0.1, 0.01);
            }
        }.runTaskTimer(plugin, 0L, 10L).getTaskId();
        particleTasks.put(location, taskId);
    }

    private void playDisappearEffect(Location location) {
        location.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location.clone().add(0.5, 0.5, 0.5), 1);
        location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
    }

    public void checkAndHandleEmptyChest(Location location) {
        if (!activeAirdrops.contains(location)) return;
        Block block = location.getBlock();
        if (block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            if (chest.getInventory().isEmpty()) {

                // Annuler le timer de 3 minutes car il est vidé
                if (despawnTasks.containsKey(location)) {
                    Bukkit.getScheduler().cancelTask(despawnTasks.get(location));
                    despawnTasks.remove(location);
                }

                // CORRECTION : Disparition après 15 secondes
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (activeAirdrops.contains(location)) {
                            removeAirdrop(location);
                            playDisappearEffect(location);
                        }
                    }
                }.runTaskLater(plugin, 300L); // 15 secondes
            }
        }
    }

    public void removeAirdrop(Location location) {
        if (particleTasks.containsKey(location)) {
            Bukkit.getScheduler().cancelTask(particleTasks.get(location));
            particleTasks.remove(location);
        }
        if (despawnTasks.containsKey(location)) {
            Bukkit.getScheduler().cancelTask(despawnTasks.get(location));
            despawnTasks.remove(location);
        }
        activeAirdrops.remove(location);
        if (location.getBlock().getType() == Material.CHEST) location.getBlock().setType(Material.AIR);
    }

    public void resetAllAirdrops() {
        for (Location loc : new HashSet<>(activeAirdrops)) removeAirdrop(loc);
    }

    public boolean isAirdrop(Location location) { return activeAirdrops.contains(location); }

    // Setters pour l'activation
    public void setDropsEnabled(boolean ultimate, boolean enabled) {
        if (ultimate) ultimateEnabled = enabled;
        else normalEnabled = enabled;
    }
    public boolean areDropsEnabled(boolean ultimate) {
        return ultimate ? ultimateEnabled : normalEnabled;
    }
}