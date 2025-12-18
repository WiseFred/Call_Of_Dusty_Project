package fr.anthognie.FFA.gui;

import fr.anthognie.FFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class FFAConfigGUI {

    public static final String GUI_TITLE = "§cAdmin - Gérer le FFA";
    private final Main plugin;

    public FFAConfigGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, GUI_TITLE);
        FileConfiguration config = plugin.getFfaConfigManager().getConfig();

        // --- Boutons de gestion ---

        // --- NOUVEAU BOUTON ---
        inv.setItem(10, createToggleButton(
                config.getBoolean("game-enabled", true),
                "§aJeu Activé",
                List.of("§7Si désactivé, les joueurs", "§7ne peuvent plus rejoindre le FFA.")
        ));

        inv.setItem(12, createButton(Material.CHEST, "§6Modifier le Shop",
                List.of("§7Ouvre le GUI d'édition du shop", "§7(identique à /editshop).")
        ));

        inv.setItem(14, createButton(Material.ARMOR_STAND, "§bGérer les Spawns",
                List.of("§7Utilisez /addspawn en jeu pour", "§7ajouter des points de spawn.")
        ));

        inv.setItem(16, createToggleButton(
                config.getBoolean("scoreboard.enabled", true),
                "§eScoreboard",
                List.of("§7Le scoreboard est-il affiché", "§7pour les joueurs dans l'arène ?")
        ));

        inv.setItem(40, createButton(Material.BARRIER, "§cQuitter",
                List.of("§7Ferme ce menu.")));

        player.openInventory(inv);
    }

    private ItemStack createToggleButton(boolean isActive, String name, List<String> lore) {
        Material material = isActive ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK;
        String status = isActive ? "§aActivé" : "§cDésactivé";
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> finalLore = new ArrayList<>(lore);
        finalLore.add("");
        finalLore.add("§fÉtat: " + status);
        finalLore.add("§eClic pour changer");
        meta.setLore(finalLore);
        item.setItemMeta(meta);
        return item;
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