package fr.anthognie.airdrops.gui;

import fr.anthognie.airdrops.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class LootBrowserGUI {

    // Constante publique pour le Listener
    public static final String GUI_TITLE = "§8Airdrops - Menu Principal";

    private final Main plugin;

    public LootBrowserGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);

        ItemStack normal = new ItemStack(Material.CHEST);
        ItemMeta normalMeta = normal.getItemMeta();
        normalMeta.setDisplayName("§eLoot Normal");
        normalMeta.setLore(Collections.singletonList("§7Gérer les kits des airdrops normaux (10m)"));
        normal.setItemMeta(normalMeta);

        ItemStack ultimate = new ItemStack(Material.ENDER_CHEST);
        ItemMeta ultMeta = ultimate.getItemMeta();
        ultMeta.setDisplayName("§6Loot Ultime");
        ultMeta.setLore(Collections.singletonList("§7Gérer les kits des airdrops ultimes (15m)"));
        ultimate.setItemMeta(ultMeta);

        inv.setItem(11, normal);
        inv.setItem(15, ultimate);

        player.openInventory(inv);
    }
}