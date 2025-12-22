package fr.anthognie.FFA.listeners;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.gui.ShopGUI;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopEditListener implements Listener {

    private final Main plugin;
    // Pour mémoriser quel item est sélectionné (Drag & Drop)
    private final Map<UUID, Integer> selectedSlot = new HashMap<>();

    public ShopEditListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.equals(ShopGUI.ADMIN_TITLE)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        // Si clic hors inventaire
        if (slot < 0) return;

        // --- CAS 1 : AJOUT D'ITEM (Clic dans l'inventaire du joueur) ---
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            // On cherche le premier slot vide dans le shop
            int firstEmpty = event.getView().getTopInventory().firstEmpty();
            if (firstEmpty != -1) {
                plugin.getShopGUI().addShopItem(clickedItem, firstEmpty);
                player.sendMessage("§aItem ajouté au shop (Prix par défaut: 100$)");
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
                plugin.getShopGUI().open(player, true); // Rafraîchir
            } else {
                player.sendMessage("§cLe shop est plein !");
            }
            return;
        }

        // --- CAS 2 : SUPPRESSION (Clic Droit) ---
        if (event.isRightClick()) {
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                plugin.getShopGUI().removeShopItem(slot);
                player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1f, 1f);
                player.sendMessage("§cItem supprimé.");
                plugin.getShopGUI().open(player, true);

                // Si on supprime, on annule toute sélection en cours
                selectedSlot.remove(player.getUniqueId());
            }
            return;
        }

        // --- CAS 3 : DÉPLACEMENT (Clic Gauche) ---
        if (event.isLeftClick()) {
            // Si on a déjà sélectionné un item
            if (selectedSlot.containsKey(player.getUniqueId())) {
                int oldSlot = selectedSlot.get(player.getUniqueId());

                // Si on reclique sur le même, on désélectionne
                if (oldSlot == slot) {
                    selectedSlot.remove(player.getUniqueId());
                    player.sendMessage("§eSélection annulée.");
                    return;
                }

                // Sinon, on déplace/échange
                plugin.getShopGUI().moveShopItem(oldSlot, slot);
                player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1f, 1f);
                player.sendMessage("§aItem déplacé !");

                selectedSlot.remove(player.getUniqueId());
                plugin.getShopGUI().open(player, true);

            } else {
                // Première sélection (on ne sélectionne que si c'est un item valide)
                if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                    selectedSlot.put(player.getUniqueId(), slot);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 2f);
                    player.sendMessage("§eItem sélectionné. Cliquez sur un autre slot pour déplacer.");
                    player.closeInventory(); // Petit hack visuel ou juste reopen ?
                    plugin.getShopGUI().open(player, true); // On reopen pour refresh
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        // Nettoyage de la mémoire quand on ferme le menu
        if (event.getView().getTitle().equals(ShopGUI.ADMIN_TITLE)) {
            selectedSlot.remove(event.getPlayer().getUniqueId());
        }
    }
}