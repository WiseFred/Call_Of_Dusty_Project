package fr.anthognie.airdrops.listeners;

import fr.anthognie.airdrops.Main;
import fr.anthognie.airdrops.gui.LootKitListGUI;
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
        if (!event.getView().getTitle().equals("§8Config Airdrops")) return;
        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        Material mat = event.getCurrentItem().getType();

        // Édition Normal
        if (mat == Material.CHEST) {
            new LootKitListGUI(plugin).open(player, "normal");
        }
        // Édition Ultimate
        else if (mat == Material.ENDER_CHEST) {
            new LootKitListGUI(plugin).open(player, "ultimate");
        }
        // Force Spawn
        else if (mat == Material.BEACON) {
            plugin.getAirdropManager().forceAirdrop(false); // Force un drop normal
            player.sendMessage("§aAirdrop forcé !");
            player.closeInventory();
        }
        // Supprimer Tout (TNT)
        else if (mat == Material.TNT) {
            plugin.getAirdropManager().resetAllAirdrops();
            player.sendMessage("§cTous les airdrops ont été supprimés.");
            player.closeInventory();
        }
    }
}