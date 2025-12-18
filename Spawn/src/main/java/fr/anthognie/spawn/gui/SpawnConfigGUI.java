package fr.anthognie.spawn.gui;

import fr.anthognie.spawn.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SpawnConfigGUI {

    public static final String GUI_TITLE = "§cAdmin - Gérer le Spawn";
    private final Main plugin;

    public SpawnConfigGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, GUI_TITLE);
        FileConfiguration config = plugin.getConfig();

        // --- Création des boutons "toggle" ---

        inv.setItem(10, createToggleButton(
                config.getBoolean("protection.invincible"),
                "§aInvincibilité",
                List.of("§7Les joueurs sont-ils invincibles", "§7dans le spawn ?")
        ));

        inv.setItem(12, createToggleButton(
                config.getBoolean("protection.no-hunger"),
                "§eAnti-Faim",
                List.of("§7La faim des joueurs", "§7descend-elle dans le spawn ?")
        ));

        inv.setItem(14, createToggleButton(
                config.getBoolean("protection.no-break"),
                "§cAnti-Casse",
                List.of("§7Les joueurs (non-OP)", "§7peuvent-ils casser des blocs ?")
        ));

        inv.setItem(16, createToggleButton(
                config.getBoolean("protection.no-place"),
                "§cAnti-Pose",
                List.of("§7Les joueurs (non-OP)", "§7peuvent-ils poser des blocs ?")
        ));

        inv.setItem(28, createToggleButton(
                config.getBoolean("protection.no-drop"),
                "§bAnti-Drop",
                List.of("§7Les joueurs (non-OP)", "§7peuvent-ils jeter des items ?")
        ));

        inv.setItem(30, createToggleButton(
                config.getBoolean("protection.no-pickup"),
                "§bAnti-Pickup",
                List.of("§7Les joueurs (non-OP)", "§7peuvent-ils ramasser des items ?")
        ));

        inv.setItem(34, createToggleButton(
                config.getBoolean("force-gamemode.enabled"),
                "§dForcer le Gamemode",
                List.of("§7Force le Gamemode Aventure", "§7à l'entrée du spawn.")
        ));

        // --- Bouton de retour ---
        // LA CORRECTION EST ICI (s'assurer que la méthode existe)
        inv.setItem(40, createButton(Material.ARROW, "§7Retour",
                List.of("§7Retour au menu principal")));

        player.openInventory(inv);
    }

    /**
     * Crée un bouton qui change de couleur (Vert/Rouge)
     */
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

    /**
     * Crée un bouton simple (C'est la méthode qui était "en rouge")
     */
    private ItemStack createButton(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}