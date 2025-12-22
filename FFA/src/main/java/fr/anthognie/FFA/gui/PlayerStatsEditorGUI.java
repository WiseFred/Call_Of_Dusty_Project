package fr.anthognie.FFA.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;

public class PlayerStatsEditorGUI {

    public static void open(Player admin, Player target) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Stats: " + target.getName());

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        skullMeta.setOwningPlayer(target);
        skullMeta.setDisplayName("§e" + target.getName());
        head.setItemMeta(skullMeta);
        inv.setItem(4, head);

        inv.setItem(11, createItem(Material.IRON_SWORD, "§cReset Kills", "§7Remettre les kills à 0"));
        inv.setItem(13, createItem(Material.TNT, "§4§lRESET TOTAL", "§7Kills, Morts, Argent -> 0"));
        inv.setItem(15, createItem(Material.SKELETON_SKULL, "§7Reset Morts", "§7Remettre les morts à 0"));

        admin.openInventory(inv);
    }

    private static ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}