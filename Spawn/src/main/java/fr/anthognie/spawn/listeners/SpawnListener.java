package fr.anthognie.spawn.listeners;

import fr.anthognie.Core.managers.BuildModeManager;
import fr.anthognie.spawn.Main;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class SpawnListener implements Listener {

    private final Main plugin;
    private final BuildModeManager buildModeManager;

    public SpawnListener(Main plugin) {
        this.plugin = plugin;
        this.buildModeManager = plugin.getCore().getBuildModeManager();
    }

    // --- NOURRITURE BLOQUÉE (GLOBAL) ---
    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            // On annule TOUTE modification de la barre de faim (baisse ou hausse)
            event.setCancelled(true);
            // On force la barre à fond pour permettre le sprint
            ((Player) event.getEntity()).setFoodLevel(20);
        }
    }

    // --- PROTECTION ARMES (CGM) ---
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        // Empêche les balles de casser des blocs (vitres, fleurs...)
        if (event.getHitBlock() != null) {
            if (event.getEntity().getShooter() instanceof Player) {
                Player shooter = (Player) event.getEntity().getShooter();
                if (!buildModeManager.isInBuildMode(shooter)) {
                    event.setCancelled(true);
                    event.getEntity().remove();
                }
            }
        }
    }

    // --- PROTECTIONS CLASSIQUES (BuildMode) ---
    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!buildModeManager.isInBuildMode(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (!buildModeManager.isInBuildMode(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (!buildModeManager.isInBuildMode(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            // Pas de dégâts au Spawn (sauf si on est en FFA)
            if (!player.getWorld().getName().equals("ffa")) {
                event.setCancelled(true);
                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    if (plugin.getConfig().contains("spawn.location")) {
                        player.teleport(plugin.getConfig().getLocation("spawn.location"));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().setFoodLevel(20); // Force la bouffe à la connexion
        if (!event.getPlayer().hasPlayedBefore() && plugin.getConfig().contains("spawn.location")) {
            event.getPlayer().teleport(plugin.getConfig().getLocation("spawn.location"));
        }
    }
}