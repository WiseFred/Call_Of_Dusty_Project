package fr.anthognie.airdrops.listeners;

import fr.anthognie.airdrops.Main;
import fr.anthognie.airdrops.managers.AirdropManager;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class AirdropPlayerListener implements Listener {

    private final AirdropManager airdropManager;

    public AirdropPlayerListener(Main plugin) {
        this.airdropManager = plugin.getAirdropManager();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();

        // On vérifie si l'inventaire fermé est un coffre
        if (inv.getHolder() instanceof Chest) {
            Chest chest = (Chest) inv.getHolder();
            Location location = chest.getLocation();

            // Si c'est un Airdrop actif
            if (airdropManager.isAirdrop(location)) {
                // On délègue la vérification au manager
                airdropManager.checkAndHandleEmptyChest(location);
            }
        }
    }
}