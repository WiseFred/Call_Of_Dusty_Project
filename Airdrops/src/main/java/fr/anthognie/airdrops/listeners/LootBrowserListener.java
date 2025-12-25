package fr.anthognie.airdrops.listeners;

import fr.anthognie.airdrops.Main;
import fr.anthognie.airdrops.gui.LootBrowserGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class LootBrowserListener implements Listener {

    private final Main plugin;

    public LootBrowserListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        boolean isUltimate = false;

        // Vérification : Est-ce un menu de loot (Normal OU Ultime) ?
        if (title.equals(LootBrowserGUI.TITLE_ULTIMATE)) {
            isUltimate = true;
        } else if (!title.equals(LootBrowserGUI.TITLE_NORMAL)) {
            return; // Ce n'est pas notre GUI
        }

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        int slot = event.getSlot();

        // Bouton Retour
        if (slot == 45) {
            plugin.getAirdropConfigGUI().open(player);
            return;
        }

        // Bouton Ajouter Item
        if (slot == 49) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand == null || hand.getType() == Material.AIR) {
                player.sendMessage("§cPrenez un item en main pour l'ajouter !");
                return;
            }

            // Ajout par défaut : 50% de chance, quantité 1-1
            plugin.getLootManager().addLootItem(hand, 50, 1, 1, isUltimate);
            player.sendMessage("§aItem ajouté aux loots " + (isUltimate ? "Ultimes" : "Normaux") + " !");
            plugin.getLootBrowserGUI().open(player, isUltimate); // Refresh
            return;
        }

        // Clic Droit sur un item -> Supprimer
        if (event.isRightClick() && clicked.getType() != Material.ARROW && clicked.getType() != Material.EMERALD_BLOCK) {
            plugin.getLootManager().removeLootItem(clicked, isUltimate);
            player.sendMessage("§cItem supprimé.");
            plugin.getLootBrowserGUI().open(player, isUltimate); // Refresh
        }
    }
}