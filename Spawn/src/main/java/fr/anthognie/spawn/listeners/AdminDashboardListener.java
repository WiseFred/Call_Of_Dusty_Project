package fr.anthognie.spawn.listeners;

import fr.anthognie.spawn.Main;
import fr.anthognie.spawn.gui.AdminDashboardGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class AdminDashboardListener implements Listener {

    private final Main plugin;

    public AdminDashboardListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.equals(AdminDashboardGUI.GUI_TITLE)) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        switch (event.getSlot()) {
            case 10: // Gérer le Spawn
                plugin.getSpawnConfigGUI().open(player);
                break;

            case 12: // Gérer le FFA
                player.closeInventory();
                player.performCommand("ffaconfig");
                break;

            case 14: // Gérer les Airdrops
                player.closeInventory();
                player.performCommand("airdropconfig");
                break;

            case 16: // Gérer la DB d'items
                player.closeInventory();
                // --- MODIFICATION ---
                // On exécute la commande du catalogue d'items
                player.performCommand("itemdb");
                break;

            case 26: // Mode Build
                player.closeInventory();
                player.performCommand("buildmode");
                break;

            case 31: // Quitter
                player.closeInventory();
                break;
        }
    }
}