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
        this.buildModeManager = plugin.getCore().getBuildModeManager();

        // Préparation de la boussole pour le give au join
        this.compassSlot = config.getInt("compass-selector.item.slot", 4);
        this.compassItem = new ItemStack(Material.COMPASS);
        ItemMeta meta = this.compassItem.getItemMeta();
        meta.setDisplayName(config.getString("compass-selector.item.name"));
        meta.setLore(config.getStringList("compass-selector.item.lore"));
        this.compassItem.setItemMeta(meta);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!config.getBoolean("teleport-on-join.enabled", true)) {
            return;
        }

        Player player = event.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                // 1. Initialisation Spawn Location
                if (spawnLocation == null) {
                    World world = Bukkit.getWorld(spawnWorldName);
                    if (world != null) {
                        spawnLocation = new Location(
                                world,
                                config.getDouble("teleport-on-join.x"),
                                config.getDouble("teleport-on-join.y"),
                                config.getDouble("teleport-on-join.z"),
                                (float) config.getDouble("teleport-on-join.yaw"),
                                (float) config.getDouble("teleport-on-join.pitch")
                        );
                    }
                }

                if (spawnLocation != null) {
                    // 2. Téléportation
                    player.teleport(spawnLocation);

                    // 3. NETTOYAGE INVENTAIRE (CORRECTION PRIORITAIRE)
                    // Si le joueur vient de se connecter et est envoyé au spawn,
                    // on doit s'assurer qu'il n'a pas gardé son stuff du FFA.
                    player.getInventory().clear();

                    // 4. Donner la boussole
                    player.getInventory().setItem(compassSlot, compassItem);

                    // 5. Règles de base
                    applySpawnRules(player);
                }
            }
        }.runTaskLater(plugin, 2L); // Petit délai pour être sûr que tout est chargé
    }

    // ... (applySpawnRules, protections onDamage, onHunger... restent inchangés) ...
    private void applySpawnRules(Player player) {
        if (config.getBoolean("force-gamemode.enabled", true)) {
            try {
                player.setGameMode(GameMode.valueOf(config.getString("force-gamemode.gamemode", "ADVENTURE").toUpperCase()));
            } catch (Exception e) {}
        }
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setFireTicks(0);
    }
    private boolean isInSpawn(Player player) { return player.getWorld().getName().equals(spawnWorldName); }
    @EventHandler public void onDamage(EntityDamageEvent event) { if(config.getBoolean("protection.invincible", true) && event.getEntity() instanceof Player && !buildModeManager.isInBuildMode((Player)event.getEntity()) && isInSpawn((Player)event.getEntity())) event.setCancelled(true); }
    @EventHandler public void onHunger(FoodLevelChangeEvent event) { if(config.getBoolean("protection.no-hunger", true) && event.getEntity() instanceof Player && !buildModeManager.isInBuildMode((Player)event.getEntity()) && isInSpawn((Player)event.getEntity())) { event.setCancelled(true); event.setFoodLevel(20); } }
    @EventHandler public void onBlockBreak(BlockBreakEvent event) { if(!buildModeManager.isInBuildMode(event.getPlayer()) && config.getBoolean("protection.no-break", true) && isInSpawn(event.getPlayer())) event.setCancelled(true); }
    @EventHandler public void onBlockPlace(BlockPlaceEvent event) { if(!buildModeManager.isInBuildMode(event.getPlayer()) && config.getBoolean("protection.no-place", true) && isInSpawn(event.getPlayer())) event.setCancelled(true); }
    @EventHandler public void onItemDrop(PlayerDropItemEvent event) { if(!buildModeManager.isInBuildMode(event.getPlayer()) && config.getBoolean("protection.no-drop", true) && isInSpawn(event.getPlayer())) event.setCancelled(true); }
    @EventHandler public void onItemPickup(PlayerPickupItemEvent event) { if(!buildModeManager.isInBuildMode(event.getPlayer()) && config.getBoolean("protection.no-pickup", true) && isInSpawn(event.getPlayer())) event.setCancelled(true); }
}