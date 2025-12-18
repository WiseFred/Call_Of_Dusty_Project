package fr.anthognie.airdrops.managers;

import fr.anthognie.airdrops.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class AirdropManager {

    private final Main plugin;
    private final LootManager lootManager;
    private final World world;
    private final List<Location> locations = new ArrayList<>();
    private final int dropHeight;
    private final Random random = new Random();

    private final Map<Location, BukkitTask> laserTasks = new HashMap<>();
    private final Map<Location, BukkitTask> emptyChestTasks = new HashMap<>();

    private BossBar ultimateBossBar;
    private long ultimateTicksRemaining;
    private final long ultimateTotalTime;

    private boolean normalDropsEnabled = false;
    private boolean ultimateDropsEnabled = false;
    private BukkitTask normalTimerTask;
    private BukkitTask ultimateTimerTask;

    public AirdropManager(Main plugin, LootManager lootManager) {
        this.plugin = plugin;
        this.lootManager = lootManager;
        plugin.saveDefaultConfig();

        String worldName = plugin.getConfig().getString("world", "ffa");
        this.world = Bukkit.getWorld(worldName);
        this.dropHeight = plugin.getConfig().getInt("drop-height", 100);

        this.ultimateTotalTime = plugin.getConfig().getLong("timers.ultimate", 900) * 20L;
        this.ultimateTicksRemaining = this.ultimateTotalTime;

        loadLocations();
    }

    private void loadLocations() {
        if (world == null) {
            plugin.getLogger().severe("Monde Airdrop '" + plugin.getConfig().getString("world") + "' introuvable !");
            return;
        }

        locations.addAll(plugin.getConfig().getStringList("locations").stream()
                .map(this::parseLocation)
                .collect(Collectors.toList()));

        plugin.getLogger().info("Chargé " + locations.size() + " emplacements d'airdrops.");
    }

    private Location parseLocation(String s) {
        String[] parts = s.split(" ");
        double x = Double.parseDouble(parts[0]);
        double y = Double.parseDouble(parts[1]);
        double z = Double.parseDouble(parts[2]);
        return new Location(world, x, y, z);
    }

    public void startTimers() {
        long normalTime = plugin.getConfig().getLong("timers.normal", 600) * 20L;

        normalTimerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (normalDropsEnabled) {
                    spawnNormalAirdrop();
                }
            }
        }.runTaskTimer(plugin, normalTime, normalTime);

        ultimateBossBar = Bukkit.createBossBar(
                "§c§lAIRDROP ULTIME §f» §eCalcul...",
                BarColor.RED,
                BarStyle.SOLID
        );
        ultimateBossBar.setProgress(1.0);

        ultimateTimerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ultimateDropsEnabled) {
                    ultimateTicksRemaining = ultimateTotalTime;
                    ultimateBossBar.setProgress(1.0);
                    long minutes = (ultimateTicksRemaining / 20) / 60;
                    ultimateBossBar.setTitle(String.format("§c§lAIRDROP ULTIME §f» §7(Désactivé) - Prochain dans %dm 0s", minutes));
                    return;
                }

                ultimateTicksRemaining -= 20L;

                if (ultimateTicksRemaining <= 0) {
                    Location groundLoc = locations.get(random.nextInt(locations.size()));
                    broadcastToFFA("§c§l[AIRDROP ULTIME] §fL'airdrop est en train de tomber à X:" + groundLoc.getBlockX() + ", Z:" + groundLoc.getBlockZ());
                    spawnFallingAirdrop(groundLoc, true);
                    ultimateTicksRemaining = ultimateTotalTime;
                }

                double progress = (double) ultimateTicksRemaining / ultimateTotalTime;
                ultimateBossBar.setProgress(progress);

                long minutes = (ultimateTicksRemaining / 20) / 60;
                long seconds = (ultimateTicksRemaining / 20) % 60;
                ultimateBossBar.setTitle(String.format("§c§lAIRDROP ULTIME §f» §eProchain drop dans %dm %ds", minutes, seconds));

                if (ultimateTicksRemaining == 100L) {
                    broadcastToFFA("§c§l[AIRDROP ULTIME] §fArrivée dans 5 secondes...");
                    world.playSound(world.getSpawnLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2.0F, 1.0F);
                } else if (ultimateTicksRemaining == 80L) {
                    broadcastToFFA("§c§l[AIRDROP ULTIME] §fArrivée dans 4 secondes...");
                    world.playSound(world.getSpawnLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2.0F, 1.0F);
                } else if (ultimateTicksRemaining == 60L) {
                    broadcastToFFA("§c§l[AIRDROP ULTIME] §fArrivée dans 3 secondes...");
                    world.playSound(world.getSpawnLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2.0F, 1.0F);
                } else if (ultimateTicksRemaining == 40L) {
                    broadcastToFFA("§c§l[AIRDROP ULTIME] §fArrivée dans 2 secondes...");
                    world.playSound(world.getSpawnLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2.0F, 1.0F);
                } else if (ultimateTicksRemaining == 20L) {
                    broadcastToFFA("§c§l[AIRDROP ULTIME] §fArrivée dans 1 seconde...");
                    world.playSound(world.getSpawnLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2.0F, 1.5F);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void spawnNormalAirdrop() {
        if (locations.isEmpty() || world == null) return;
        Location groundLoc = locations.get(random.nextInt(locations.size()));

        broadcastToFFA("§6[Airdrop] §fUn airdrop normal est en route vers X:" + groundLoc.getBlockX() + ", Z:" + groundLoc.getBlockZ());
        spawnFallingAirdrop(groundLoc, false);
    }

    public void forceAirdrop(boolean isUltimate) {
        if (locations.isEmpty() || world == null) {
            plugin.getLogger().warning("Impossible de forcer l'airdrop: monde ou emplacements non chargés.");
            return;
        }

        if (isUltimate) {
            this.ultimateTicksRemaining = 100L;
        } else {
            Location groundLoc = locations.get(random.nextInt(locations.size()));
            broadcastToFFA("§a[Admin] §fForçage d'un airdrop §6NORMAL§f à X:" + groundLoc.getBlockX() + ", Z:" + groundLoc.getBlockZ());
            spawnFallingAirdrop(groundLoc, false);
        }
    }

    public void setDropsEnabled(boolean isUltimate, boolean enabled) {
        if (isUltimate) {
            this.ultimateDropsEnabled = enabled;
            if(enabled) {
                for(Player p : world.getPlayers()) showBossBar(p);
            } else {
                for(Player p : world.getPlayers()) hideBossBar(p);
                resetAllAirdrops();
            }
        } else {
            this.normalDropsEnabled = enabled;
            if (!enabled) {
                resetAllAirdrops();
            }
        }
    }

    public boolean areDropsEnabled(boolean isUltimate) {
        return isUltimate ? ultimateDropsEnabled : normalDropsEnabled;
    }

    public void cancelAllTimers() {
        if (normalTimerTask != null) normalTimerTask.cancel();
        if (ultimateTimerTask != null) ultimateTimerTask.cancel();
        if (ultimateBossBar != null) ultimateBossBar.removeAll();
        resetAllAirdrops();
    }

    public void resetAllAirdrops() {
        for (Location loc : new ArrayList<>(emptyChestTasks.keySet())) {
            emptyChestTasks.get(loc).cancel();
            if (loc.getBlock().getType() == Material.CHEST) {
                loc.getBlock().setType(Material.AIR);
            }
        }
        emptyChestTasks.clear();

        for (BukkitTask task : laserTasks.values()) {
            task.cancel();
        }
        laserTasks.clear();
        plugin.getLogger().info("Tous les airdrops actifs ont été supprimés.");
    }

    private void broadcastToFFA(String message) {
        if (world == null) return;
        for (Player player : world.getPlayers()) {
            player.sendMessage(message);
        }
    }

    public void showBossBar(Player player) {
        if (ultimateBossBar != null) {
            ultimateBossBar.addPlayer(player);
        }
    }

    public void hideBossBar(Player player) {
        if (ultimateBossBar != null) {
            ultimateBossBar.removePlayer(player);
        }
    }

    private void spawnFallingAirdrop(Location groundLoc, boolean isUltimate) {
        Location startLoc = groundLoc.clone().add(0, dropHeight, 0);

        Block block = groundLoc.getBlock();
        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            block.setType(Material.AIR);
        }

        ArmorStand as = (ArmorStand) world.spawnEntity(startLoc, EntityType.ARMOR_STAND);
        as.setGravity(false);
        as.setInvisible(true);
        as.setCanPickupItems(false);
        as.setMarker(true);
        as.getEquipment().setHelmet(new ItemStack(isUltimate ? Material.BEACON : Material.IRON_BLOCK));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (as.getLocation().getY() <= groundLoc.getY()) {
                    this.cancel();
                    as.remove();

                    groundLoc.getBlock().setType(Material.CHEST);
                    Chest chest = (Chest) groundLoc.getBlock().getState();
                    lootManager.fillChest(chest, isUltimate);

                    world.playSound(groundLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
                    world.spawnParticle(Particle.EXPLOSION_LARGE, groundLoc.clone().add(0.5, 0.5, 0.5), 10);

                    if (isUltimate) {
                        startLaser(groundLoc);
                    }
                    startEmptyChestTimer(groundLoc);

                    return;
                }

                as.teleport(as.getLocation().subtract(0, 0.5, 0));
                world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, as.getLocation().add(0, 2, 0), 5, 0.1, 0.1, 0.1, 0.01);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void startLaser(Location loc) {
        BukkitTask laserTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (loc.getBlock().getType() != Material.CHEST) {
                    this.cancel();
                    laserTasks.remove(loc);
                    return;
                }
                loc.getWorld().spawnParticle(Particle.TOTEM, loc.clone().add(0.5, 0.5, 0.5), 140, 0.1, 150, 0.1, 0.003);
            }
        }.runTaskTimer(plugin, 0L, 10L);

        laserTasks.put(loc, laserTask);
    }

    private void startEmptyChestTimer(Location loc) {
        BukkitTask emptyTask = new BukkitRunnable() {
            int secondsEmpty = 0;
            @Override
            public void run() {
                Block block = loc.getBlock();
                if (block.getType() != Material.CHEST) {
                    this.cancel();
                    emptyChestTasks.remove(loc);
                    if (laserTasks.containsKey(loc)) laserTasks.get(loc).cancel();
                    laserTasks.remove(loc);
                    return;
                }

                Chest chest = (Chest) block.getState();
                if (chest.getInventory().isEmpty()) {
                    secondsEmpty++;
                } else {
                    secondsEmpty = 0;
                }

                if (secondsEmpty >= 30) {
                    this.cancel();
                    emptyChestTasks.remove(loc);
                    block.setType(Material.AIR);
                    world.spawnParticle(Particle.SMOKE_NORMAL, loc.clone().add(0.5, 0.5, 0.5), 20, 0.2, 0.2, 0.2, 0.1);
                    if (laserTasks.containsKey(loc)) laserTasks.get(loc).cancel();
                    laserTasks.remove(loc);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);

        emptyChestTasks.put(loc, emptyTask);
    }
}