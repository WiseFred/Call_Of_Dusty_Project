package fr.anthognie.airdrops.listeners;

import fr.anthognie.airdrops.Main;
import fr.anthognie.airdrops.gui.LootBrowserGUI;
import fr.anthognie.airdrops.gui.LootKitListGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class LootBrowserListener implements Listener {

    private final Main plugin;
    private final LootKitListGUI lootKitListGUI;

    public LootBrowserListener(Main plugin) {
        this.plugin = plugin;
        this.lootKitListGUI = plugin.getLootKitListGUI();
    }

    @EventHandler
    public void onBrowserClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.equals(LootBrowserGUI.GUI_TITLE)) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        switch (event.getSlot()) {
            case 11: // Kits Normaux
                lootKitListGUI.open(player, "normal");
                break;
            case 15: // Kits Ultimes
                lootKitListGUI.open(player, "ultimate");
                break;
            case 26: // Retour
                player.performCommand("airdropconfig");
                break;
        }
    }

    @EventHandler
    public void onKitListClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith(LootKitListGUI.GUI_TITLE_PREFIX)) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        String type = title.replace(LootKitListGUI.GUI_TITLE_PREFIX, "").toLowerCase();

        switch (event.getSlot()) {
            case 48: // Créer un Kit
                player.closeInventory();
                plugin.getLootKitChatListener().startSession(player, type);
                player.sendMessage("§a---------------------------------");
                player.sendMessage("§eVeuillez taper le NOM du kit, suivi d'un espace,");
                player.sendMessage("§epuis sa CHANCE (poids) dans le chat.");
                player.sendMessage("§eExemple: kit_armes 40");
                player.sendMessage("§7(Tapez 'annuler' pour annuler)");
                player.sendMessage("§a---------------------------------");
                break;
            case 50: // Retour
                plugin.getLootBrowserGUI().open(player);
                break;
            default:
                // Clic sur un kit existant
                if (clickedItem.getType() == Material.BOOK) {
                    String kitName = clickedItem.getItemMeta().getDisplayName().substring(2); // Enlève "§e"

                    if (event.isLeftClick()) {
                        // Éditer
                        player.performCommand("airdrop editloot " + type + " " + kitName);
                    } else if (event.isRightClick()) {
                        // Supprimer
                        player.performCommand("airdrop deletekit " + type + " " + kitName);
                        lootKitListGUI.open(player, type); // Rafraîchir
                    }
                }
                break;
        }
    }
}