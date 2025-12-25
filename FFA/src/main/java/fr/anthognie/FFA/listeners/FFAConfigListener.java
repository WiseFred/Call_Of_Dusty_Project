package fr.anthognie.FFA.listeners;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.gui.FFAConfigGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class FFAConfigListener implements Listener {

    private final Main plugin;

    public FFAConfigListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        // Vérifie le bon titre
        if (!event.getView().getTitle().equals(FFAConfigGUI.GUI_TITLE)) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        switch (slot) {
            case 11: // Émeraude -> Éditer le Shop
                player.closeInventory();
                // Lance la commande /editshop en tant que joueur (permission déjà vérifiée par le menu admin)
                player.performCommand("editshop");
                break;

            case 13: // Tête -> Gérer les Joueurs
                // Ouvre le GUI de sélection des joueurs (pour reset stats, etc.)
                plugin.getPlayerStatsGUI().open(player);
                break;

            case 15: // Boussole -> Spawns
                player.closeInventory();
                player.sendMessage("§8§m--------------------------------");
                player.sendMessage("§6§lCONFIGURATION SPAWNS");
                player.sendMessage("§7Pour ajouter un point de spawn :");
                player.sendMessage("§e1. §7Allez à l'endroit désiré.");
                player.sendMessage("§e2. §7Regardez dans la bonne direction.");
                player.sendMessage("§e3. §7Tapez §6/addspawn");
                player.sendMessage("§8§m--------------------------------");
                break;
        }
    }
}