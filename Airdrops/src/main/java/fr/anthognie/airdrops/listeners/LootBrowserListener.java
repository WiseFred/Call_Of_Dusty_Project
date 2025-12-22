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

    public LootBrowserListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        // --- GESTION DU MENU PRINCIPAL LOOTS (Browser) ---
        if (title.equals(LootBrowserGUI.GUI_TITLE)) { //
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();

            switch (slot) {
                case 11: // Bouton "Loot Normal"
                    plugin.getLootKitListGUI().open(player, "normal");
                    break;
                case 15: // Bouton "Loot Ultime"
                    plugin.getLootKitListGUI().open(player, "ultimate");
                    break;
                case 22: // Bouton "Retour" (Harmonisé au centre)
                    // Revient au menu de configuration Airdrops
                    plugin.getAirdropConfigGUI().open(player);
                    break;
            }
        }

        // --- GESTION DE LA LISTE DES KITS (Sous-menu) ---
        // Vérifie si le titre commence par le préfixe défini dans LootKitListGUI
        else if (title.startsWith(LootKitListGUI.GUI_TITLE_PREFIX)) { //
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            Player player = (Player) event.getWhoClicked();

            // On détermine le type (normal/ultimate) en analysant le titre
            // Ex: "§8Kits: normal" -> on récupère "normal"
            String type = title.replace(LootKitListGUI.GUI_TITLE_PREFIX, "").toLowerCase();

            // Gestion du bouton "Retour" dans la liste des kits (généralement slot 49)
            if (event.getSlot() == 49) {
                plugin.getLootBrowserGUI().open(player);
                return;
            }

            // Gestion du bouton "Créer un Kit" (généralement slot 45, 48 ou 50 selon ta config)
            // Adapte ce slot si tu l'as changé dans LootKitListGUI, ici je garde une logique standard
            if (clickedItem.getType() == Material.NETHER_STAR) { // Supposons que l'étoile est le bouton créer
                player.closeInventory();
                plugin.getLootKitChatListener().startSession(player, type);
                player.sendMessage("§a---------------------------------");
                player.sendMessage("§eVeuillez taper le NOM du kit, suivi d'un espace,");
                player.sendMessage("§epuis sa CHANCE (poids) dans le chat.");
                player.sendMessage("§eExemple: kit_soldat 50");
                player.sendMessage("§7(Tapez 'annuler' pour stopper)");
                player.sendMessage("§a---------------------------------");
                return;
            }

            // Gestion des clics sur les items de kits (Livres)
            if (clickedItem.getType() == Material.BOOK) {
                if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) return;

                // On récupère le nom du kit (en enlevant le code couleur §e s'il y est)
                String kitName = clickedItem.getItemMeta().getDisplayName().replaceAll("§.", "");

                if (event.isLeftClick()) {
                    // Clic Gauche -> Éditer le contenu
                    player.performCommand("airdrop editloot " + type + " " + kitName);
                } else if (event.isRightClick()) {
                    // Clic Droit -> Supprimer le kit
                    player.performCommand("airdrop deletekit " + type + " " + kitName);
                    // On rafraîchit le menu
                    plugin.getLootKitListGUI().open(player, type);
                }
            }
        }
    }
}