package fr.anthognie.spawn.listeners;

import fr.anthognie.spawn.Main;
import fr.anthognie.spawn.gui.AdminDashboardGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class AdminDashboardListener implements Listener {

    private final Main plugin;

    public AdminDashboardListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        // VÉRIFICATION DU TITRE EXACT
        if (!event.getView().getTitle().equals(AdminDashboardGUI.GUI_TITLE)) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        switch (slot) {
            case 10: // Gérer Spawn
                player.closeInventory();
                plugin.getSpawnConfigGUI().open(player);
                break;
            case 12: // Gérer FFA
                player.closeInventory();
                player.performCommand("ffaconfig"); // Commande du module FFA
                break;
            case 14: // Gérer Airdrops
                player.closeInventory();
                player.performCommand("airdropconfig"); // Commande du module Airdrops
                break;
            case 16: // ItemDB
                player.closeInventory();
                player.performCommand("itemdb");
                break;
            case 26: // BuildMode
                player.closeInventory();
                player.performCommand("buildmode");
                break;
            case 31: // Fermer
                player.closeInventory();
                break;
        }
    }
}