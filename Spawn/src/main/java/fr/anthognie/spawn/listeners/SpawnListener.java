package fr.anthognie.spawn.listeners;

import fr.anthognie.Core.managers.BuildModeManager;
import fr.anthognie.spawn.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.scheduler.BukkitRunnable; // <-- NOUVEL IMPORT

public class SpawnListener implements Listener {

    private final Main plugin;
    private final FileConfiguration config;
    private final String spawnWorldName;
    private Location spawnLocation;
    private final BuildModeManager buildModeManager;

    public SpawnListener(Main plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.spawnWorldName = config.getString("spawn-world", "world");
        // On récupère le BuildModeManager (important)
        this.buildModeManager = plugin.getCore().getBuildModeManager();
    }

    // --- TÉLÉPORTATION À LA CONNEXION (CORRIGÉE) ---
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!config.getBoolean("teleport-on-join.enabled", true)) {
            return;
        }

        Player player = event.getPlayer();

        // --- LE FIX : On retarde d'un tick ---
        // Ça garantit que Multiverse a chargé les mondes
        new BukkitRunnable() {
            @Override
            public void run() {
                // 1. On charge le spawn location (s'il n'est pas déjà chargé)
                if (spawnLocation == null) {
                    World world = Bukkit.getWorld(spawnWorldName);
                    if (world == null) {
                        plugin.getLogger().warning("Le monde spawn '" + spawnWorldName + "' n'a pas pu être chargé ! Téléportation annulée.");
                        return;
                    }
                    spawnLocation = new Location(
                            world,
                            config.getDouble("teleport-on-join.x"),
                            config.getDouble("teleport-on-join.y"),
                            config.getDouble("teleport-on-join.z"),
                            (float) config.getDouble("teleport-on-join.yaw"),
                            (float) config.getDouble("teleport-on-join.pitch")
                    );
                }

                // 2. On téléporte (Maintenant, ça marche à coup sûr)
                player.teleport(spawnLocation);

                // 3. On applique les règles (gamemode, vie, faim)
                applySpawnRules(player);
            }
        }.runTaskLater(plugin, 1L); // 1L = 1 tick
    }

    // Méthode pour appliquer les règles (vie, faim, gamemode)
    private void applySpawnRules(Player player) {
        if (config.getBoolean("force-gamemode.enabled", true)) {
            try {
                GameMode gm = GameMode.valueOf(config.getString("force-gamemode.gamemode", "ADVENTURE").toUpperCase());
                player.setGameMode(gm);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Gamemode invalide dans config.yml !");
            }
        }

        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setFireTicks(0);
    }

    // --- PROTECTIONS DIVERSES ---
    private boolean isInSpawn(Player player) {
        return player.getWorld().getName().equals(spawnWorldName);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (config.getBoolean("protection.invincible", true) && event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (buildModeManager.isInBuildMode(player)) {
                return;
            }
            if (isInSpawn(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (config.getBoolean("protection.no-hunger", true) && event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (buildModeManager.isInBuildMode(player)) {
                return;
            }
            if (isInSpawn(player)) {
                event.setCancelled(true);
                event.setFoodLevel(20);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (buildModeManager.isInBuildMode(player)) {
            return;
        }
        if (config.getBoolean("protection.no-break", true)) {
            if (isInSpawn(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (buildModeManager.isInBuildMode(player)) {
            return;
        }
        if (config.getBoolean("protection.no-place", true)) {
            if (isInSpawn(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (buildModeManager.isInBuildMode(player)) {
            return;
        }
        if (config.getBoolean("protection.no-drop", true)) {
            if (isInSpawn(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (buildModeManager.isInBuildMode(player)) {
            return;
        }
        if (config.getBoolean("protection.no-pickup", true)) {
            if (isInSpawn(player)) {
                event.setCancelled(true);
            }
        }
    }
}