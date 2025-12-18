package fr.anthognie.airdrops.listeners;

import fr.anthognie.airdrops.Main;
import fr.anthognie.airdrops.gui.AirdropConfigGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class AirdropConfigListener implements Listener {

    private final Main plugin;

    public AirdropConfigListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.equals(AirdropConfigGUI.GUI_TITLE)) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        switch (event.getSlot()) {
            case 10: // Toggle Normal
                player.performCommand("airdrop toggle normal " + (clickedItem.getType() == Material.REDSTONE_BLOCK ? "on" : "off"));
                plugin.getAirdropConfigGUI().open(player);
                break;
            case 12: // Toggle Ultime
                player.performCommand("airdrop toggle ultimate " + (clickedItem.getType() == Material.REDSTONE_BLOCK ? "on" : "off"));
                plugin.getAirdropConfigGUI().open(player);
                break;
            case 19: // Force Normal
                player.performCommand("airdrop force normal");
                player.closeInventory();
                break;
            case 21: // Force Ultime
                player.performCommand("airdrop force ultimate");
                player.closeInventory();
                break;

            case 24: // Gérer Kits de Loot
                // --- MODIFICATION ---
                plugin.getLootBrowserGUI().open(player);
                break;

            case 28: // Reset Airdrops
                player.performCommand("airdrop reset");
                player.closeInventory();
                player.sendMessage("§cTous les airdrops actifs ont été supprimés.");
                break;

            case 40: // Retour
                player.performCommand("codadmin");
                break;
        }
    }
}