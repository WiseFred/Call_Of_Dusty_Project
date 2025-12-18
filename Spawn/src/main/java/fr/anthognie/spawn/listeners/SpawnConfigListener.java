package fr.anthognie.spawn.listeners;

import fr.anthognie.spawn.Main;
import fr.anthognie.spawn.gui.SpawnConfigGUI;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class SpawnConfigListener implements Listener {

    private final Main plugin;

    public SpawnConfigListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.equals(SpawnConfigGUI.GUI_TITLE)) {
            return; // Ce n'est pas le bon GUI
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        // Gestion du clic
        String path = null;
        switch (event.getSlot()) {
            case 10: path = "protection.invincible"; break;
            case 12: path = "protection.no-hunger"; break;
            case 14: path = "protection.no-break"; break;
            case 16: path = "protection.no-place"; break;
            case 28: path = "protection.no-drop"; break;
            case 30: path = "protection.no-pickup"; break;
            case 34: path = "force-gamemode.enabled"; break;

            case 40: // Bouton Retour
                plugin.getAdminDashboardGUI().open(player);
                return;

            default:
                return; // Clic sur un slot vide
        }

        // On a cliqué sur un bouton "toggle"
        FileConfiguration config = plugin.getConfig();
        boolean currentValue = config.getBoolean(path);
        config.set(path, !currentValue); // On inverse la valeur

        plugin.saveConfig(); // On sauvegarde sur le disque

        // On rafraîchit le menu pour montrer le changement
        plugin.getSpawnConfigGUI().open(player);
    }
}