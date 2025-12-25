package fr.anthognie.spawn.listeners;

import fr.anthognie.spawn.gui.GameSelectorGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GameSelectorListener implements Listener {

    private final GameSelectorGUI gameSelectorGUI;

    // CONSTRUCTEUR CORRIGÉ : Il doit prendre GameSelectorGUI, pas Main !
    public GameSelectorListener(GameSelectorGUI gameSelectorGUI) {
        this.gameSelectorGUI = gameSelectorGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Vérif titre
        if (!event.getView().getTitle().equals(gameSelectorGUI.getTitle())) {
            return;
        }

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // Clic sur la TNT (FFA)
        if (event.getSlot() == gameSelectorGUI.getFfaSlot()) {
            player.closeInventory();

            // Méthode 1 : Commande (Plus sûr car gère tout le process du plugin FFA)
            player.performCommand("joinffa");

            // Méthode 2 : TP direct (si la commande échoue, décommentez ceci)
            /*
            World ffaWorld = Bukkit.getWorld("ffa");
            if (ffaWorld != null) player.teleport(ffaWorld.getSpawnLocation());
            else player.sendMessage("§cMonde FFA introuvable !");
            */
        }
    }
}