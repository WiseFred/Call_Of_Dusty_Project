package fr.anthognie.FFA.listeners;

import fr.anthognie.FFA.Main;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class FFAProtectionListener implements Listener {

    private final Main plugin;

    public FFAProtectionListener(Main plugin) {
        this.plugin = plugin;
    }

    private boolean isFFAWorld(World world) {
        return world != null && world.getName().equals(plugin.getFfaManager().getFFAWorldName());
    }

    private boolean canBuild(Player player) {
        return plugin.getCore().getBuildModeManager().isInBuildMode(player);
    }

    // 1. Protection Faim
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player && isFFAWorld(event.getEntity().getWorld())) {
            event.setCancelled(true);
            event.setFoodLevel(20);
        }
    }

    // 2. Protection "Coup de crosse" / Dégâts blocs (CRITIQUE pour les mods d'armes)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDamage(BlockDamageEvent event) {
        if (isFFAWorld(event.getPlayer().getWorld())) {
            if (!canBuild(event.getPlayer())) {
                event.setCancelled(true);
            }
        }
    }

    // 3. Interactions Physiques & Clics
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (isFFAWorld(event.getPlayer().getWorld())) {
            // Empêche le clic gauche (casser) sur un bloc
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (!canBuild(event.getPlayer())) {
                    event.setCancelled(true);
                }
            }
            // Piétinement Farmland
            if (event.getAction() == Action.PHYSICAL && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.FARMLAND) {
                event.setCancelled(true);
            }
        }
    }

    // 4. Projectiles & Entités (Balles qui cassent les vitres)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (isFFAWorld(event.getBlock().getWorld())) {
            if (event.getEntity() instanceof Player) {
                if (!canBuild((Player) event.getEntity())) event.setCancelled(true);
            } else {
                event.setCancelled(true);
            }
        }
    }

    // --- PROTECTIONS STANDARD ---
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        if (isFFAWorld(event.getPlayer().getWorld()) && !canBuild(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event) {
        if (isFFAWorld(event.getPlayer().getWorld()) && !canBuild(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (isFFAWorld(event.getLocation().getWorld())) event.blockList().clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (isFFAWorld(event.getBlock().getWorld())) {
            event.blockList().clear();
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (isFFAWorld(event.getEntity().getWorld())) {
            if (!(event.getRemover() instanceof Player) || !canBuild((Player) event.getRemover())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHangingBreakGlobal(HangingBreakEvent event) {
        if (isFFAWorld(event.getEntity().getWorld()) && event.getCause() != HangingBreakEvent.RemoveCause.ENTITY) {
            event.setCancelled(true);
        }
    }
}