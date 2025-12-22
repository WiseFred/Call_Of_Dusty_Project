package fr.anthognie.spawn.gui;

import fr.anthognie.Core.utils.ItemBuilder;
import fr.anthognie.spawn.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AdminDashboardGUI {

    // TITRE UNIQUE HARMONISÉ
    public static final String GUI_TITLE = "§c§lTableau de Bord Admin";
    private final Main plugin;

    public AdminDashboardGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, GUI_TITLE);

        // Configuration
        inv.setItem(10, ItemBuilder.create(Material.COMPASS, "§eGérer le Spawn", "§7Définir spawn/lobby"));
        inv.setItem(12, ItemBuilder.create(Material.DIAMOND_SWORD, "§cGérer le FFA", "§7Spawns, Kits, Stats..."));
        inv.setItem(14, ItemBuilder.create(Material.CHEST, "§bGérer les Airdrops", "§7Kits & Loots"));
        inv.setItem(16, ItemBuilder.create(Material.BOOK, "§aBase de Données Items", "§7Modifier les items moddés"));

        // Outils
        inv.setItem(26, ItemBuilder.create(Material.GOLDEN_AXE, "§6Mode Build", "§7Activer/Désactiver la construction"));

        // Fermer
        inv.setItem(31, ItemBuilder.create(Material.BARRIER, "§cFermer"));

        // Vitres
        ItemStack vitre = ItemBuilder.create(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 36; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, vitre);
        }

        player.openInventory(inv);
    }
}