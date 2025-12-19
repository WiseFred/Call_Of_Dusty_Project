package fr.anthognie.Core.gui;

import fr.anthognie.Core.managers.ItemConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ItemDatabaseGUI {

    public static final String GUI_TITLE_PREFIX = "§cAdmin - DB Items (Page ";
    private final ItemConfigManager itemConfigManager;

    public ItemDatabaseGUI(ItemConfigManager itemConfigManager) {
        this.itemConfigManager = itemConfigManager;
    }

    public void open(Player player, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE_PREFIX + page + ")");

        // --- Boutons de contrôle ---
        ItemStack placeholder = createButton(Material.GRAY_STAINED_GLASS_PANE, "§r", null);
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, placeholder);
        }

        inv.setItem(45, createButton(Material.PAPER, "§aPage Précédente", null));

        // J'ai retiré le bouton Nether Star "Ajouter" ici pour simplifier

        inv.setItem(49, createButton(Material.BARRIER, "§cFermer", List.of("§7Utilisez §e/itemdb add", "§7pour ajouter un item.")));
        inv.setItem(53, createButton(Material.PAPER, "§aPage Suivante", null));

        // --- Remplir avec les items ---
        Set<String> allPaths = itemConfigManager.getConfig().getKeys(true);
        List<String> itemPaths = new ArrayList<>();

        for (String path : allPaths) {
            if (itemConfigManager.getConfig().isString(path)) {
                itemPaths.add(path);
            }
        }

        int maxPage = (int) Math.ceil(itemPaths.size() / 45.0);
        if (page > maxPage) page = maxPage;
        if (page < 1) page = 1;

        int startIndex = (page - 1) * 45;
        for (int i = 0; i < 45; i++) {
            int itemIndex = startIndex + i;
            if (itemIndex >= itemPaths.size()) break;

            String path = itemPaths.get(itemIndex);
            ItemStack item = itemConfigManager.getItemStack(path);
            if (item == null) continue;

            ItemStack displayItem = item.clone();
            ItemMeta meta = displayItem.getItemMeta();
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add("§8---------------");
            lore.add("§7Path: §f" + path);
            lore.add("§cClic Droit pour SUPPRIMER");
            meta.setLore(lore);
            displayItem.setItemMeta(meta);

            inv.setItem(i, displayItem);
        }

        player.openInventory(inv);
    }

    private ItemStack createButton(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}