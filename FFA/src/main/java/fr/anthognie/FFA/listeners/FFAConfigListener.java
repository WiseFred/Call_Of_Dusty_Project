package fr.anthognie.FFA.listeners;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.gui.FFAConfigGUI;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class FFAConfigListener implements Listener {

    private final Main plugin;

    public FFAConfigListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.equals(FFAConfigGUI.GUI_TITLE)) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        FileConfiguration config = plugin.getFfaConfigManager().getConfig();

        switch (event.getSlot()) {
            case 10: // --- NOUVEAU : Toggle Jeu ---
                boolean gameEnabled = config.getBoolean("game-enabled", true);
                config.set("game-enabled", !gameEnabled);
                plugin.getFfaConfigManager().saveConfig();
                plugin.getFfaConfigGUI().open(player);
                break;

            case 12: // Modifier le Shop
                player.closeInventory();
                player.performCommand("editshop");
                break;

            case 14: // Gérer les Spawns (juste un bouton info)
                player.closeInventory();
                player.sendMessage("§eUtilisez /addspawn pour ajouter un spawn.");
                break;

            case 16: // Toggle Scoreboard
                boolean sbEnabled = config.getBoolean("scoreboard.enabled", true);
                config.set("scoreboard.enabled", !sbEnabled);
                plugin.getFfaConfigManager().saveConfig();
                plugin.getFfaConfigGUI().open(player);
                break;

            case 40: // Quitter
                player.closeInventory();
                break;
        }
    }
}