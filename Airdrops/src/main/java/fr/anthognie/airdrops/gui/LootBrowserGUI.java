package fr.anthognie.airdrops.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class LootBrowserGUI {

    public static final String GUI_TITLE = "§cAdmin - Choisir un Type de Loot";

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE); // 3 lignes

        inv.setItem(11, createButton(Material.CHEST, "§6Kits Normaux",
                List.of("§7Gérer les kits qui tombent", "§7dans les airdrops normaux.")
        ));

        inv.setItem(15, createButton(Material.BEACON, "§cKits Ultimes",
                List.of("§7Gérer les kits qui tombent", "§7dans les airdrops ultimes.")
        ));

        inv.setItem(26, createButton(Material.ARROW, "§7Retour",
                List.of("§7Retour au menu Airdrops")
        ));

        player.openInventory(inv);
    }

    private ItemStack createButton(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}