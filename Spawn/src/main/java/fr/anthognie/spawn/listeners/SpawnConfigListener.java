package fr.anthognie.spawn.listeners;

import fr.anthognie.spawn.Main;
import fr.anthognie.spawn.gui.SpawnConfigGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class SpawnConfigListener implements Listener {

    private final Main plugin;

    public SpawnConfigListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(SpawnConfigGUI.GUI_TITLE)) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        switch (slot) {
            case 11: // Définir Spawn
                player.closeInventory();
                // Commande interne ou logique directe
                player.getWorld().setSpawnLocation(player.getLocation());
                plugin.getConfig().set("spawn.location", player.getLocation());
                plugin.saveConfig();
                player.sendMessage("§aSpawn principal défini ici !");
                break;

            case 13: // Définir Lobby (Si utilisé, sinon juste spawn)
                player.closeInventory();
                plugin.getConfig().set("lobby.location", player.getLocation());
                plugin.saveConfig();
                player.sendMessage("§eLobby défini ici !");
                break;

            case 15: // Toggle Void TP
                boolean current = plugin.getConfig().getBoolean("void-tp.enabled");
                plugin.getConfig().set("void-tp.enabled", !current);
                plugin.saveConfig();
                plugin.getSpawnConfigGUI().open(player); // Refresh
                break;

            case 40: // Retour
                player.closeInventory();
                plugin.getAdminDashboardGUI().open(player);
                break;
        }
    }
}