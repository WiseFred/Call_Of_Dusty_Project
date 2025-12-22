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
import org.bukkit.event.entity.EntityRegainHealthEvent; // IMPORT
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnListener implements Listener {

    private final Main plugin;
    private final BuildModeManager buildModeManager;

    public SpawnListener(Main plugin) {
        this.plugin = plugin;
        this.buildModeManager = plugin.getCore().getBuildModeManager();
    }

    // --- NOURRITURE BLOQUÉE & REGEN VANILLA DÉSACTIVÉE ---

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
            Player player = (Player) event.getEntity();
            player.setFoodLevel(20);
            player.setSaturation(20f);
        }
    }

    // NOUVEAU : On bloque la régénération naturelle (liée à la saturation)
    // Pour que seule la régénération Custom (CoD) fonctionne.
    @EventHandler
    public void onRegen(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            // Si la raison est la nourriture (SATIATED) ou la regen naturelle (REGEN)
            if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED
                    || event.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN) {
                event.setCancelled(true);
            }
        }
    }

    // --- PROTECTION ARMES (CGM) ---
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
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

    // --- GESTION CONNEXION & RESPAWN ---

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        p.setFoodLevel(20);
        p.setSaturation(20f);

        // CORRECTION : On vide l'inventaire pour ne pas garder le kit FFA au spawn
        p.getInventory().clear();

        if (plugin.getConfig().contains("spawn.location")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (p.isOnline()) {
                        p.teleport(plugin.getConfig().getLocation("spawn.location"));
                    }
                }
            }.runTaskLater(plugin, 2L);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (plugin.getConfig().contains("spawn.location")) {
            event.setRespawnLocation(plugin.getConfig().getLocation("spawn.location"));
        }
    }
}