package fr.anthognie.airdrops.listeners;

import fr.anthognie.airdrops.Main;
import fr.anthognie.airdrops.gui.AirdropConfigGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class AirdropConfigListener implements Listener {

    private final Main plugin;

    public AirdropConfigListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(AirdropConfigGUI.GUI_TITLE)) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        switch (slot) {
            case 11: // Edit Normal
                plugin.getLootBrowserGUI().open(player, false);
                break;
            case 15: // Edit Ultime
                plugin.getLootBrowserGUI().open(player, true);
                break;
            case 12: // Force Normal
                plugin.getAirdropManager().forceAirdrop(false);
                player.sendMessage("§aAirdrop normal forcé !");
                player.closeInventory();
                break;
            case 14: // Force Ultime
                plugin.getAirdropManager().forceAirdrop(true);
                player.sendMessage("§6Airdrop ultime forcé !");
                player.closeInventory();
                break;
            case 22: // Clear All
                plugin.getAirdropManager().removeAllAirdrops();
                player.sendMessage("§cTous les airdrops ont été supprimés.");
                player.closeInventory();
                break;

            case 20: // Toggle Normal
                boolean currentN = plugin.getAirdropManager().areDropsEnabled(false);
                plugin.getAirdropManager().setDropsEnabled(false, !currentN);
                plugin.getAirdropConfigGUI().open(player); // Refresh
                break;

            case 24: // Toggle Ultime
                boolean currentU = plugin.getAirdropManager().areDropsEnabled(true);
                plugin.getAirdropManager().setDropsEnabled(true, !currentU);
                plugin.getAirdropConfigGUI().open(player); // Refresh
                break;
        }
    }
}