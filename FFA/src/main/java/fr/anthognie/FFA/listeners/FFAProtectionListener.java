package fr.anthognie.FFA.listeners;

import fr.anthognie.FFA.Main;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
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

    // --- 1. PROTECTION NOURRITURE (Totale) ---
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (isFFAWorld(player.getWorld())) {
            event.setCancelled(true);
            event.setFoodLevel(20); // Force la bouffe au max
            player.setFoodLevel(20);
            player.setSaturation(20f);
        }
    }

    // --- 2. PROTECTION BLOCS (Blindage) ---

    // Joueur casse un bloc
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        if (isFFAWorld(event.getPlayer().getWorld())) {
            if (!canBuild(event.getPlayer())) {
                event.setCancelled(true);
            }
        }
    }

    // Joueur pose un bloc
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event) {
        if (isFFAWorld(event.getPlayer().getWorld())) {
            if (!canBuild(event.getPlayer())) {
                event.setCancelled(true);
            }
        }
    }

    // Explosion d'entité (TNT, Creeper, Grenades)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (isFFAWorld(event.getLocation().getWorld())) {
            event.blockList().clear(); // Aucune destruction
        }
    }

    // Explosion de bloc (Lit)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (isFFAWorld(event.getBlock().getWorld())) {
            event.blockList().clear();
            event.setCancelled(true);
        }
    }

    // Cadres / Tableaux cassés par une entité (balle, joueur)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (isFFAWorld(event.getEntity().getWorld())) {
            if (event.getRemover() instanceof Player) {
                if (!canBuild((Player) event.getRemover())) event.setCancelled(true);
            } else {
                event.setCancelled(true); // Projectiles, explosions...
            }
        }
    }

    // Cadres cassés (autres causes)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHangingBreakGlobal(HangingBreakEvent event) {
        if (isFFAWorld(event.getEntity().getWorld()) && event.getCause() != HangingBreakEvent.RemoveCause.ENTITY) {
            event.setCancelled(true);
        }
    }

    // Piétinement (Farmland)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (isFFAWorld(event.getPlayer().getWorld()) && event.getAction() == Action.PHYSICAL && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.FARMLAND) {
            event.setCancelled(true);
        }
    }

    // ANTI-CASSE PAR PROJECTILES (VItres, Nénuphars, etc.)
    // C'est cet évent qui gère les balles qui cassent les vitres ou les Endermen
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (isFFAWorld(event.getBlock().getWorld())) {
            // On annule TOUT changement de bloc venant d'une entité
            // Sauf si c'est un joueur en build mode (cas rare, généralement joueur = BlockBreakEvent)
            if (event.getEntity() instanceof Player) {
                if (!canBuild((Player) event.getEntity())) {
                    event.setCancelled(true);
                }
            } else {
                // Balle, Flèche, Mob... -> Annulé
                event.setCancelled(true);
            }
        }
    }
}