package fr.anthognie.Core.listeners;

import fr.anthognie.Core.Main;
import fr.anthognie.Core.managers.ItemConfigManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ItemDatabaseListener implements Listener {

    private final Main plugin;
    private final ItemConfigManager itemConfigManager;

    public ItemDatabaseListener(Main plugin) {
        this.plugin = plugin;
        this.itemConfigManager = plugin.getItemConfigManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith("§cAdmin - DB Items")) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        // --- GESTION SUPPRESSION (CLIC DROIT) ---
        // On vérifie que ce n'est pas un bouton de menu (Paper, Barrier, Glass)
        if (clickedItem.getType() != Material.PAPER &&
                clickedItem.getType() != Material.BARRIER &&
                clickedItem.getType() != Material.GRAY_STAINED_GLASS_PANE) {

            if (event.isRightClick()) {
                // Pour retrouver le path, on peut parser le lore qu'on a ajouté dans le GUI
                if (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasLore()) {
                    for (String line : clickedItem.getItemMeta().getLore()) {
                        if (line.startsWith("§7Path: §f")) {
                            String path = line.replace("§7Path: §f", "");

                            itemConfigManager.deleteItem(path);
                            player.sendMessage("§cItem supprimé de la base de données : " + path);

                            // Rafraîchir la page
                            plugin.getItemDatabaseGUI().open(player, 1);
                            return;
                        }
                    }
                }
            }
        }

        if (clickedItem.getType() == Material.BARRIER) {
            player.closeInventory();
        }
    }
}