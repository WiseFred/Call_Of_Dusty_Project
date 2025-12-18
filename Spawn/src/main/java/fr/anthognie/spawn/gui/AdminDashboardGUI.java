package fr.anthognie.spawn.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class AdminDashboardGUI {

    public static final String GUI_TITLE = "§c§lTableau de Bord Admin";

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, GUI_TITLE); // 4 lignes

        // --- Boutons de Gestion ---

        // 1. Gérer le Spawn
        inv.setItem(10, createButton(Material.BEACON, "§aGérer le Spawn",
                List.of("§7Modifier les protections du spawn", "§7(invincibilité, faim, casse...)")));

        // 2. Gérer le FFA
        inv.setItem(12, createButton(Material.DIAMOND_SWORD, "§cGérer le FFA",
                List.of("§7Modifier le shop, les spawns,", "§7et les règles du FFA.")));

        // 3. Gérer les Airdrops
        inv.setItem(14, createButton(Material.CHEST, "§6Gérer les Airdrops",
                List.of("§7Modifier les timers, les kits de loot,", "§7et forcer les drops.")));

        // 4. Base de données d'Items
        inv.setItem(16, createButton(Material.ITEM_FRAME, "§bBase de Données d'Items",
                List.of("§7Enregistrer/Supprimer les items", "§7moddés (remplace /registeritem).")));

        inv.setItem(26, createButton(Material.COMMAND_BLOCK, "§d§lMode Build",
                List.of("§7Active/Désactive le mode build", "§7pour les admins.")));

        // 5. Bouton de fermeture
        inv.setItem(31, createButton(Material.BARRIER, "§cQuitter",
                List.of("§7Ferme le panneau d'administration.")));

        player.openInventory(inv);
    }

    // Petite méthode pour créer les boutons
    private ItemStack createButton(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}