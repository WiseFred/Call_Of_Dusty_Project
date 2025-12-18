package fr.anthognie.spawn.listeners;

import fr.anthognie.spawn.gui.GameSelectorGUI;
import org.bukkit.Bukkit; // <-- NOUVEL IMPORT
import org.bukkit.Material;
import org.bukkit.World; // <-- NOUVEL IMPORT
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GameSelectorListener implements Listener {

    private final GameSelectorGUI gameSelectorGUI;

    public GameSelectorListener(GameSelectorGUI gameSelectorGUI) {
        this.gameSelectorGUI = gameSelectorGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.equals(gameSelectorGUI.getTitle())) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (event.getSlot() == gameSelectorGUI.getFfaSlot()) {
            player.closeInventory();

            // --- LA CORRECTION EST ICI ---
            // On n'utilise plus Multiverse, on téléporte le joueur
            // directement avec l'API Bukkit.

            // 1. On récupère le monde "ffa"
            World ffaWorld = Bukkit.getWorld("ffa");

            if (ffaWorld == null) {
                player.sendMessage("§cErreur: Le monde FFA est hors ligne. Contactez un admin.");
                return;
            }

            // 2. On téléporte le joueur au spawn de ce monde
            // (Le listener du plugin FFA prendra le relais à partir d'ici)
            player.teleport(ffaWorld.getSpawnLocation());
            // -----------------------------
        }
    }
}