package fr.anthognie.airdrops.listeners;

import fr.anthognie.Core.utils.InventorySerializer;
import fr.anthognie.Core.utils.ItemBuilder;
import fr.anthognie.airdrops.Main;
import fr.anthognie.airdrops.gui.AirdropConfigGUI;
import fr.anthognie.airdrops.managers.AirdropManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class AirdropConfigListener implements Listener {

    private final Main plugin;
    // Titres des menus intermédiaires
    private static final String TITLE_SELECT_TYPE = "§8Sélecteur de Type";
    private static final String TITLE_SELECT_KIT_NORMAL = "§8Kits: Normal";
    private static final String TITLE_SELECT_KIT_ULTIMATE = "§8Kits: Ultimate";
    private static final String PREFIX_EDIT = "§c[ÉDITION] Kit: ";

    public AirdropConfigListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        Player player = (Player) event.getWhoClicked();

        // 1. MENU PRINCIPAL
        if (title.equals(AirdropConfigGUI.GUI_TITLE)) {
            event.setCancelled(true);
            handleMainMenu(event, player);
            return;
        }

        // 2. SÉLECTEUR DE TYPE (Normal ou Ultimate)
        if (title.equals(TITLE_SELECT_TYPE)) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;

            if (event.getSlot() == 11) openKitSelector(player, "normal");
            if (event.getSlot() == 15) openKitSelector(player, "ultimate");
            return;
        }

        // 3. SÉLECTEUR DE KIT (Liste des kits)
        if (title.equals(TITLE_SELECT_KIT_NORMAL) || title.equals(TITLE_SELECT_KIT_ULTIMATE)) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            // Le nom du kit est sur l'item
            String kitName = event.getCurrentItem().getItemMeta().getDisplayName().replace("§e", "");
            String type = title.contains("Normal") ? "normal" : "ultimate";

            openKitEditor(player, type, kitName);
        }

        // 4. MENU ÉDITION (On laisse le joueur modifier, pas de event.setCancelled(true))
        // Rien à faire ici, la sauvegarde se fait au onClose
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();

        // Si on ferme un menu d'édition (titre commence par le prefix)
        if (title.startsWith(PREFIX_EDIT)) {
            // Format: "§c[ÉDITION] Kit: type:nom"
            String raw = title.replace(PREFIX_EDIT, ""); // "type:nom"
            String[] parts = raw.split(":");
            if (parts.length < 2) return;

            String type = parts[0];
            String kitName = parts[1];

            Player player = (Player) event.getPlayer();

            // Sauvegarde
            String itemPath = "airdrops.loot." + type + "." + kitName;
            String base64 = InventorySerializer.itemStackArrayToBase64(event.getInventory().getContents());

            plugin.getItemConfigManager().getConfig().set(itemPath, base64);
            plugin.getItemConfigManager().saveConfig();

            player.sendMessage("§aKit '" + kitName + "' (" + type + ") sauvegardé avec succès !");

            // On rouvre le menu principal pour être fluide
            plugin.getAirdropConfigGUI().open(player);
        }
    }

    // --- LOGIQUE INTERNE ---

    private void handleMainMenu(InventoryClickEvent event, Player player) {
        AirdropManager manager = plugin.getAirdropManager();
        int slot = event.getSlot();

        if (slot == 10) { // Toggle Normal
            manager.setDropsEnabled(false, !manager.areDropsEnabled(false));
            plugin.getAirdropConfigGUI().open(player);
        }
        if (slot == 12) { // Toggle Ultimate
            manager.setDropsEnabled(true, !manager.areDropsEnabled(true));
            plugin.getAirdropConfigGUI().open(player);
        }
        if (slot == 14) { // Force
            player.closeInventory();
            if (event.isLeftClick()) manager.forceAirdrop(false);
            else if (event.isRightClick()) manager.forceAirdrop(true);
            player.sendMessage("§aAirdrop lancé !");
        }
        if (slot == 16) { // Ouvrir Selecteur
            openTypeSelector(player);
        }
    }

    private void openTypeSelector(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE_SELECT_TYPE);
        inv.setItem(11, ItemBuilder.create(Material.CHEST, "§eNormal", "§7Cliquez pour voir les kits"));
        inv.setItem(15, ItemBuilder.create(Material.ENDER_CHEST, "§5Ultimate", "§7Cliquez pour voir les kits"));
        player.openInventory(inv);
    }

    private void openKitSelector(Player player, String type) {
        String title = type.equals("normal") ? TITLE_SELECT_KIT_NORMAL : TITLE_SELECT_KIT_ULTIMATE;
        Inventory inv = Bukkit.createInventory(null, 54, title);

        FileConfiguration config = plugin.getLootManager().getConfig();
        if (config.contains(type + ".kits")) {
            ConfigurationSection section = config.getConfigurationSection(type + ".kits");
            for (String key : section.getKeys(false)) {
                int chance = section.getInt(key + ".chance");
                inv.addItem(ItemBuilder.create(Material.PAPER, "§e" + key, "§7Chance: " + chance + "%", "§aCliquez pour éditer le contenu"));
            }
        }
        player.openInventory(inv);
    }

    private void openKitEditor(Player player, String type, String kitName) {
        String title = PREFIX_EDIT + type + ":" + kitName;
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Charger items existants
        String itemPath = "airdrops.loot." + type + "." + kitName;
        String base64data = plugin.getItemConfigManager().getConfig().getString(itemPath);

        if (base64data != null && !base64data.isEmpty()) {
            try {
                inv.setContents(InventorySerializer.itemStackArrayFromBase64(base64data));
            } catch (Exception e) {
                player.sendMessage("§cErreur chargement items.");
            }
        }

        player.openInventory(inv);
        player.sendMessage("§eModifiez le contenu et §lfermez§e l'inventaire pour sauvegarder.");
    }
}