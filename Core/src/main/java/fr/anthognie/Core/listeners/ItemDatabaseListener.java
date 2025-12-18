package fr.anthognie.Core.listeners;

import fr.anthognie.Core.Main;
import fr.anthognie.Core.gui.ItemDatabaseGUI;
import fr.anthognie.Core.managers.ItemConfigManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemDatabaseListener implements Listener {

    private final Main plugin;
    private final ItemConfigManager itemConfigManager;
    private final ItemDatabaseGUI gui;

    public ItemDatabaseListener(Main plugin) {
        this.plugin = plugin;
        this.itemConfigManager = plugin.getItemConfigManager();
        this.gui = plugin.getItemDatabaseGUI();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith(ItemDatabaseGUI.GUI_TITLE_PREFIX)) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        int currentPage = Integer.parseInt(title.substring(ItemDatabaseGUI.GUI_TITLE_PREFIX.length(), title.length() - 1));

        // --- GESTION DES BOUTONS DE CONTRÔLE ---
        if (event.getSlot() == 45) { // Page Précédente
            if (currentPage > 1) {
                gui.open(player, currentPage - 1);
            }
            return;
        }
        if (event.getSlot() == 53) { // Page Suivante
            gui.open(player, currentPage + 1);
            return;
        }
        if (event.getSlot() == 49) { // Retour (vers /codadmin)
            player.performCommand("codadmin");
            return;
        }
        if (event.getSlot() == 48) { // Ajouter un Item
            ItemStack itemOnCursor = event.getCursor();
            if (itemOnCursor == null || itemOnCursor.getType() == Material.AIR) {
                player.sendMessage("§cPrenez un item dans votre main (curseur) et cliquez à nouveau sur ce bouton.");
                return;
            }
            plugin.getItemDatabaseChatListener().startSession(player, itemOnCursor.clone());
            player.closeInventory();
            player.sendMessage("§a---------------------------------");
            player.sendMessage("§eVeuillez taper le 'PATH' de cet item dans le chat.");
            player.sendMessage("§7(Ex: 'kits.ffa.pistol' ou 'shop.weapons.rifle')");
            player.sendMessage("§7(Tapez 'annuler' pour annuler)");
            player.sendMessage("§a---------------------------------");
            return;
        }

        // --- GESTION DES CLICS SUR LES ITEMS ---
        if (event.getSlot() < 45) {
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null || !meta.hasLore()) return;

            String path = null;
            for (String line : meta.getLore()) {
                if (line.startsWith("§7Path: §f")) {
                    path = line.substring("§7Path: §f".length());
                    break;
                }
            }

            if (path == null) return;

            if (event.isRightClick()) { // Clic Droit = Supprimer
                itemConfigManager.setItemStack(path, null);
                itemConfigManager.saveConfig();
                player.sendMessage("§cItem '" + path + "' supprimé.");
                gui.open(player, currentPage); // Rafraîchir
            }
        }
    }
}