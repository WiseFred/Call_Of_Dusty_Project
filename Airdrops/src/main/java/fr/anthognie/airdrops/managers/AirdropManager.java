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

    private BukkitRunnable normalDropTask;
    private BukkitRunnable ultimateDropTask;
    private BossBar ultimateBossBar;
    private int ultimateTimerSeconds = 900;

    // Désactivés par défaut
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

        // VOS POSITIONS HARDCODÉES
        dropLocations.add(new Location(world, 36, 72, -41));
        dropLocations.add(new Location(world, 61, 70, -14));
        dropLocations.add(new Location(world, 62, 69, 18));
        dropLocations.add(new Location(world, 50, 70, 45));
        dropLocations.add(new Location(world, 58, 70, 61));
        dropLocations.add(new Location(world, 64, 82, 77));
        dropLocations.add(new Location(world, 61, 84, 86));
        dropLocations.add(new Location(world, 59, 97, 96));
        dropLocations.add(new Location(world, 59, 80, 100));
        dropLocations.add(new Location(world, 48, 70, 89));
        dropLocations.add(new Location(world, 26, 70, 57));
        dropLocations.add(new Location(world, 12, 69, 50));
        dropLocations.add(new Location(world, -9, 75, 23));
        dropLocations.add(new Location(world, 48, 96, 21));
        dropLocations.add(new Location(world, 25, 70, -10));
        dropLocations.add(new Location(world, 19, 69, -50));
        dropLocations.add(new Location(world, -3, 75, -49));
        dropLocations.add(new Location(world, -4, 88, -23));
        dropLocations.add(new Location(world, 0, 96, 2));
        dropLocations.add(new Location(world, 41, 92, 105));
        dropLocations.add(new Location(world, -21, 72, 47));
        dropLocations.add(new Location(world, -36, 70, 48));
        dropLocations.add(new Location(world, -39, 88, 71));
        dropLocations.add(new Location(world, -59, 71, 78));
        dropLocations.add(new Location(world, -79, 68, 99));
        dropLocations.add(new Location(world, -118, 76, 94));
        dropLocations.add(new Location(world, -109, 72, 47));
        dropLocations.add(new Location(world, -110, 71, 29));
        dropLocations.add(new Location(world, -106, 95, 17));
        dropLocations.add(new Location(world, -114, 95, 23));
        dropLocations.add(new Location(world, -130, 66, -2));
        dropLocations.add(new Location(world, -107, 70, -8));
        dropLocations.add(new Location(world, -131, 81, -21));
        dropLocations.add(new Location(world, -128, 64, -47));
        dropLocations.add(new Location(world, -128, 63, -58));
        dropLocations.add(new Location(world, -131, 63, -73));
        dropLocations.add(new Location(world, -82, 70, -65));
        dropLocations.add(new Location(world, -87, 69, -32));
        dropLocations.add(new Location(world, -88, 73, -0));
        dropLocations.add(new Location(world, -32, 70, 24));
        dropLocations.add(new Location(world, -40, 70, 5));
        dropLocations.add(new Location(world, -79, 73, -9));
        dropLocations.add(new Location(world, -59, 75, -66));
        dropLocations.add(new Location(world, -37, 69, -67));
        dropLocations.add(new Location(world, -41, 71, -55));
        dropLocations.add(new Location(world, -35, 77, -35));
        dropLocations.add(new Location(world, -34, 86, -42));
        dropLocations.add(new Location(world, -33, 83, -21));
        dropLocations.add(new Location(world, -68, 69, 37));
        dropLocations.add(new Location(world, -25, 104, 4));
        dropLocations.add(new Location(world, 3, 100, -23));
    }

    public void startTimers() {
        normalDropTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (normalEnabled) {
                    forceAirdrop(false);
                }
            }
        };
        normalDropTask.runTaskTimer(plugin, 12000L, 12000L);

        ultimateBossBar = Bukkit.createBossBar("§6Prochain Airdrop Ultime: 15:00", BarColor.RED, BarStyle.SOLID);
        ultimateBossBar.setVisible(ultimateEnabled);

        ultimateDropTask = new BukkitRunnable() {
            @Override
            public void run() {
                World ffaWorld = Bukkit.getWorld("ffa");
                if (ffaWorld != null) {
                    for (Player p : ffaWorld.getPlayers()) {
                        ultimateBossBar.addPlayer(p);
                    }
                }

                if (!ultimateEnabled) {
                    ultimateBossBar.setVisible(false);
                    return;
                } else {
                    ultimateBossBar.setVisible(true);
                }

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
    }

    public void forceAirdrop(boolean isUltimate) {
        if (dropLocations.isEmpty()) return;
        Location loc = dropLocations.get(new Random().nextInt(dropLocations.size()));
        spawnAirdrop(loc, isUltimate);
    }

    // --- ALIAS POUR LE GUI ET COMPATIBILITÉ ---
    public void spawnRandomAirdrop() {
        forceAirdrop(false);
    }

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

        if (isUltimate) {
            location.getWorld().playSound(location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 0.5f);
        } else {
            location.getWorld().playSound(location, Sound.ENTITY_CHICKEN_EGG, 1.0f, 0.5f);
        }
        startParticleEffect(location, isUltimate);
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
                if (isUltimate) {
                    location.getWorld().spawnParticle(Particle.TOTEM, location.clone().add(0.5, 1, 0.5), 10, 0.1, 0.5, 0.1, 0.05);
                    for (int y = 1; y < 50; y+=2) {
                        location.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, location.clone().add(0.5, y, 0.5), 1, 0, 0, 0, 0);
                    }
                } else {
                    location.getWorld().spawnParticle(Particle.SMOKE_LARGE, location.clone().add(0.5, 1, 0.5), 5, 0.1, 0.5, 0.1, 0.01);
                }
            }
        }.runTaskTimer(plugin, 0L, 10L).getTaskId();
        particleTasks.put(location, taskId);
    }

    public void checkAndHandleEmptyChest(Location location) {
        if (!activeAirdrops.contains(location)) return;

        Block block = location.getBlock();
        if (block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            if (chest.getInventory().isEmpty()) {
                if (particleTasks.containsKey(location)) {
                    Bukkit.getScheduler().cancelTask(particleTasks.get(location));
                    particleTasks.remove(location);
                }
                activeAirdrops.remove(location);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (block.getType() == Material.CHEST) {
                            block.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location.clone().add(0.5, 0.5, 0.5), 1);
                            block.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
                            block.setType(Material.AIR);
                        }
                    }
                }.runTaskLater(plugin, 300L);
            }
        }
    }

    public void removeAirdrop(Location location) {
        if (particleTasks.containsKey(location)) {
            Bukkit.getScheduler().cancelTask(particleTasks.get(location));
            particleTasks.remove(location);
        }
        activeAirdrops.remove(location);
        if (location.getBlock().getType() == Material.CHEST) {
            location.getBlock().setType(Material.AIR);
        }
    }

    public void resetAllAirdrops() {
        for (Location loc : new HashSet<>(activeAirdrops)) removeAirdrop(loc);
    }

    // AJOUT : Alias pour le GUI
    public void removeAllAirdrops() {
        resetAllAirdrops();
    }

    public boolean isAirdrop(Location location) { return activeAirdrops.contains(location); }

    public void setDropsEnabled(boolean ultimate, boolean enabled) {
        if (ultimate) ultimateEnabled = enabled; else normalEnabled = enabled;
    }
    public boolean areDropsEnabled(boolean ultimate) { return ultimate ? ultimateEnabled : normalEnabled; }
}