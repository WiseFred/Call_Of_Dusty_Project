package fr.anthognie.Core.listeners;

import fr.anthognie.Core.Main;
import fr.anthognie.Core.gui.ItemDatabaseGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ItemDatabaseListener implements Listener {

    private final Main plugin;
    private final ItemDatabaseChatListener chatListener;

    public ItemDatabaseListener(Main plugin) {
        this.plugin = plugin;
        this.chatListener = plugin.getItemDatabaseChatListener();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // CORRECTION IMPORTANTE : Vérification basée sur le préfixe défini dans ItemDatabaseGUI
        // Le titre contient le numéro de page, donc on utilise startsWith
        if (!event.getView().getTitle().startsWith("§cAdmin - DB Items")) return;

        event.setCancelled(true); // Bloque tout par défaut

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        // Gestion du bouton "Ajouter un Item" (Nether Star)
        if (clickedItem != null && clickedItem.getType() == Material.NETHER_STAR) {

            // CAS 1 : Rien sur le curseur
            if (cursorItem == null || cursorItem.getType() == Material.AIR) {
                player.sendMessage("§cPrenez l'item à sauvegarder sur votre curseur (souris) PUIS cliquez sur ce bouton !");
                return;
            }

            // CAS 2 : Item sur le curseur -> SAUVEGARDE
            ItemStack itemToSave = cursorItem.clone();

            player.closeInventory();

            chatListener.startSession(player, itemToSave);

            player.sendMessage("§aItem détecté ! §e" + itemToSave.getType().toString());
            player.sendMessage("§aEntrez maintenant le chemin de sauvegarde dans le chat (ex: kits.ffa.pistol) :");
        }

        // Gestion pagination (Optionnel, à ajouter si tu veux que les boutons Page Suivante/Précédente marchent)
        if (clickedItem != null && clickedItem.getType() == Material.PAPER) {
            if (clickedItem.getItemMeta().getDisplayName().contains("Suivante")) {
                // Logique page suivante (récupérer page actuelle via titre et +1)
                // plugin.getItemDatabaseGUI().open(player, page + 1);
            } else if (clickedItem.getItemMeta().getDisplayName().contains("Précédente")) {
                // Logique page précédente
            }
        }
    }
}