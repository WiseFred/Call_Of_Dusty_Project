package fr.anthognie.FFA.gui;

import fr.anthognie.FFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class FFAConfigGUI {

    public static final String GUI_TITLE = "§8Configuration FFA";
    private final Main plugin;

    public FFAConfigGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);

        inv.setItem(11, createItem(Material.EMERALD, "§aÉditer le Shop", "§7Modifier les prix"));
        inv.setItem(13, createItem(Material.PLAYER_HEAD, "§eGérer les Joueurs", "§7Stats & Reset"));
        inv.setItem(15, createItem(Material.COMPASS, "§bDéfinir Spawns", "§7Utilisez /addspawn"));

        player.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}