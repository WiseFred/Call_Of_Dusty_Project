package fr.anthognie.spawn.listeners;

import fr.anthognie.Core.managers.BuildModeManager;
import fr.anthognie.spawn.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnListener implements Listener {

    private final Main plugin;
    private final FileConfiguration config;
    private final String spawnWorldName;
    private Location spawnLocation;
    private final BuildModeManager buildModeManager;
    private final ItemStack compassItem;
    private final int compassSlot;

    public SpawnListener(Main plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.spawnWorldName = config.getString("spawn-world", "world");

        // Récupération du Manager du Core pour le BuildMode
        // Assure-toi que ton Core a bien cette méthode accessible
        this.buildModeManager = fr.anthognie.Core.Main.getInstance().getBuildModeManager();

        this.compassSlot = config.getInt("compass-selector.item.slot", 4);
        this.compassItem = new ItemStack(Material.COMPASS);
        ItemMeta meta = this.compassItem.getItemMeta();
        meta.setDisplayName(config.getString("compass-selector.item.name"));
        meta.setLore(config.getStringList("compass-selector.item.lore"));
        this.compassItem.setItemMeta(meta);
    }

    // --- GESTION DE LA FAIM (SPAWN) ---
    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            // Si le joueur est au Spawn et n'est PAS en BuildMode
            if (isInSpawn(player) && !buildModeManager.isInBuildMode(player)) {
                event.setCancelled(true); // Annule la baisse
                player.setFoodLevel(20);  // Force la barre pleine
                player.setSaturation(20); // Saturation max pour éviter le tremblement
            }
        }
    }

    // --- AUTRES PROTECTIONS (Protection TOTALE si pas BuildMode) ---

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && isInSpawn((Player) event.getEntity())) {
            if (!buildModeManager.isInBuildMode((Player) event.getEntity())) {
                event.setCancelled(true); // Invincible
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isInSpawn(event.getPlayer()) && !buildModeManager.isInBuildMode(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isInSpawn(event.getPlayer()) && !buildModeManager.isInBuildMode(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (isInSpawn(event.getPlayer()) && !buildModeManager.isInBuildMode(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        if (isInSpawn(event.getPlayer()) && !buildModeManager.isInBuildMode(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    // --- JOIN & TELEPORT ---
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!config.getBoolean("teleport-on-join.enabled", true)) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (spawnLocation == null) {
                    World world = Bukkit.getWorld(spawnWorldName);
                    if (world != null) {
                        spawnLocation = new Location(world,
                                config.getDouble("teleport-on-join.x"),
                                config.getDouble("teleport-on-join.y"),
                                config.getDouble("teleport-on-join.z"),
                                (float) config.getDouble("teleport-on-join.yaw"),
                                (float) config.getDouble("teleport-on-join.pitch"));
                    }
                }
                if (spawnLocation != null) {
                    player.teleport(spawnLocation);
                    player.getInventory().clear();
                    player.getInventory().setItem(compassSlot, compassItem);

                    // Reset stats
                    player.setHealth(player.getMaxHealth());
                    player.setFoodLevel(20);
                    player.setGameMode(GameMode.ADVENTURE);
                }
            }
        }.runTaskLater(plugin, 2L);
    }

    private boolean isInSpawn(Player player) {
        return player.getWorld().getName().equals(spawnWorldName);
    }
}