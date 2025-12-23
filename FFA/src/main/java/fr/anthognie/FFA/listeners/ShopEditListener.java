package fr.anthognie.FFA.listeners;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.gui.ShopGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ShopEditListener implements Listener {

    private final Main plugin;
    private int selectedSlot = -1;

    public ShopEditListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.equals(ShopGUI.ADMIN_TITLE)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        // Clic dans l'inventaire du joueur (Ajout d'item)
        if (event.getClickedInventory() != null && event.getClickedInventory().equals(player.getInventory())) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                int firstEmpty = event.getView().getTopInventory().firstEmpty();
                if (firstEmpty != -1) {
                    plugin.getShopGUI().addShopItem(clickedItem, firstEmpty);
                    player.sendMessage("§aItem ajouté au shop !");
                    plugin.getShopGUI().open(player, true); // Refresh
                } else {
                    player.sendMessage("§cLe shop est plein !");
                }
            }
            return;
        }

        // Clic dans le shop (Déplacement / Suppression)
        if (event.getClickedInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory())) {
            if (event.isRightClick()) {
                // Suppression
                plugin.getShopGUI().removeShopItem(slot);
                player.sendMessage("§cItem supprimé.");
                plugin.getShopGUI().open(player, true);
                return;
            }

            if (event.isLeftClick()) {
                // Déplacement
                if (selectedSlot == -1) {
                    if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                        selectedSlot = slot;
                        player.sendMessage("§eItem sélectionné. Cliquez sur une nouvelle case.");
                    }
                } else {
                    // Si on clique sur la même case, on annule
                    if (selectedSlot == slot) {
                        selectedSlot = -1;
                        player.sendMessage("§cDéplacement annulé.");
                        return;
                    }
                    plugin.getShopGUI().moveShopItem(selectedSlot, slot);
                    player.sendMessage("§aItem déplacé !");
                    selectedSlot = -1;
                    plugin.getShopGUI().open(player, true);
                }
            }
        }
    }
}