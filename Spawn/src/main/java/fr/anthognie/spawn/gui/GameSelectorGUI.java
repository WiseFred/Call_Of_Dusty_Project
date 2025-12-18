package fr.anthognie.spawn.gui;

import fr.anthognie.Core.managers.ItemConfigManager;
import fr.anthognie.spawn.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GameSelectorGUI {

    private final Main plugin;
    private final ItemConfigManager itemConfigManager;
    private final FileConfiguration config;

    private final String title;
    private final int size;

    public GameSelectorGUI(Main plugin) {
        this.plugin = plugin;
        this.itemConfigManager = plugin.getCore().getItemConfigManager();
        this.config = plugin.getConfig();

        this.title = config.getString("compass-selector.gui.title", "§8Sélecteur de Jeux");
        this.size = config.getInt("compass-selector.gui.size", 27);
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, this.size, this.title);

        // --- Placer l'item FFA ---
        String itemPath = config.getString("compass-selector.gui.ffa-item.item-path");
        int slot = config.getInt("compass-selector.gui.ffa-item.slot");

        ItemStack ffaItem = itemConfigManager.getItemStack(itemPath);
        if (ffaItem == null) {
            // Item par défaut si la grenade n'est pas enregistrée
            ffaItem = new ItemStack(Material.TNT);
        }

        ItemMeta meta = ffaItem.getItemMeta();
        meta.setDisplayName(config.getString("compass-selector.gui.ffa-item.name"));
        meta.setLore(config.getStringList("compass-selector.gui.ffa-item.lore"));
        ffaItem.setItemMeta(meta);

        inv.setItem(slot, ffaItem);

        // TODO: Ajouter d'autres items (1v1, Team Deathmatch...)

        player.openInventory(inv);
    }

    // Getters pour les Listeners
    public String getTitle() {
        return title;
    }

    public int getFfaSlot() {
        return config.getInt("compass-selector.gui.ffa-item.slot");
    }
}