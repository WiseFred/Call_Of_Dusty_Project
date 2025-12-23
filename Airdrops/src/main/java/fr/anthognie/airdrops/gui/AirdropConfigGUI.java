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

    private final Main plugin;

    public AirdropConfigGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Config Airdrops");

        // Bouton Loots Normaux
        inv.setItem(10, createItem(Material.CHEST, "§eLoots Normaux", "§7Modifier les items des", "§7airdrops classiques (10min)"));

        // Bouton Loots Ultimes
        inv.setItem(12, createItem(Material.ENDER_CHEST, "§6Loots Ultimes", "§7Modifier les items des", "§7airdrops ultimes (15min)"));

        inv.setItem(14, createItem(Material.BEACON, "§bForce Spawn", "§7Faire apparaître un airdrop maintenant"));
        inv.setItem(16, createItem(Material.TNT, "§c§lSupprimer Tout", "§7Retire tous les airdrops actifs"));

        player.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}