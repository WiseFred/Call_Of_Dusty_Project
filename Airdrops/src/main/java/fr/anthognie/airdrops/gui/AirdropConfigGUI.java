package fr.anthognie.airdrops.gui;

import fr.anthognie.airdrops.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class AirdropConfigGUI {

    public static final String GUI_TITLE = "§8Configuration Airdrops";
    private final Main plugin;

    public AirdropConfigGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);

        inv.setItem(11, createItem(Material.CHEST, "§eÉditer Loot Normal", "§7Modifier le contenu des coffres normaux"));
        inv.setItem(15, createItem(Material.ENDER_CHEST, "§6Éditer Loot Ultime", "§7Modifier le contenu des coffres ultimes"));

        // Boutons Force Spawn
        inv.setItem(12, createItem(Material.BEACON, "§bForce Spawn Normal", "§7Faire apparaître un drop maintenant"));
        inv.setItem(14, createItem(Material.NETHER_STAR, "§cForce Spawn Ultime", "§7Faire apparaître un drop ultime"));

        // Boutons ON/OFF
        boolean normalOn = plugin.getAirdropManager().areDropsEnabled(false);
        boolean ultiOn = plugin.getAirdropManager().areDropsEnabled(true);

        inv.setItem(20, createItem(normalOn ? Material.LIME_DYE : Material.GRAY_DYE,
                "§eDrops Normaux: " + (normalOn ? "§aON" : "§cOFF"), "§7Clic pour changer"));

        inv.setItem(24, createItem(ultiOn ? Material.LIME_DYE : Material.GRAY_DYE,
                "§6Drops Ultimes: " + (ultiOn ? "§aON" : "§cOFF"), "§7Clic pour changer"));

        inv.setItem(22, createItem(Material.TNT, "§cSupprimer Tout", "§7Retire tous les airdrops actifs"));

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