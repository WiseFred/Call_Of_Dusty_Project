package fr.anthognie.FFA.listeners;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.gui.FFAConfigGUI;
import fr.anthognie.FFA.managers.ConfigManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class FFAConfigListener implements Listener {

    private final Main plugin;
    private final ConfigManager configManager;

    public FFAConfigListener(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getFfaConfigManager();
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(FFAConfigGUI.GUI_TITLE)) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        switch (slot) {
            case 11: // Toggle Spawn Protection
                boolean prot = !configManager.getConfig().getBoolean("spawn-protection.enabled");
                configManager.getConfig().set("spawn-protection.enabled", prot);
                configManager.saveConfig();
                plugin.getFfaConfigGUI().open(player); // Refresh
                break;

            case 13: // Toggle Build Mode
                boolean build = !configManager.getConfig().getBoolean("build-mode.enabled");
                configManager.getConfig().set("build-mode.enabled", build);
                configManager.saveConfig();
                plugin.getFfaConfigGUI().open(player);
                break;

            case 15: // Toggle Scoreboard
                boolean board = !configManager.getConfig().getBoolean("scoreboard.enabled");
                configManager.getConfig().set("scoreboard.enabled", board);
                configManager.saveConfig();
                plugin.getFfaConfigGUI().open(player);
                break;

            case 40: // Retour
                player.performCommand("codadmin");
                break;
        }
    }
}