package fr.anthognie.FFA.listeners;

import fr.anthognie.Core.managers.BuildModeManager;
import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.game.FFAManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;

public class FFAProtectionListener implements Listener {

    private final FFAManager ffaManager;
    private final BuildModeManager buildModeManager;

    public FFAProtectionListener(Main plugin) {
        this.ffaManager = plugin.getFfaManager();
        this.buildModeManager = plugin.getCore().getBuildModeManager();
    }

    private boolean isProtected(Player player) {
        // Si le joueur est dans le FFA ET qu'il n'est PAS en mode build
        return player.getWorld().getName().equals(ffaManager.getFFAWorldName())
                && !buildModeManager.isInBuildMode(player);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isProtected(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isProtected(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (isProtected(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (isProtected(player)) {
            // --- NOUVEAU : Blocage de l'armure ---
            if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
                event.setCancelled(true);
            }
            // (Tu peux ajouter d'autres règles ici, comme le shift-clic)
        }
    }
}