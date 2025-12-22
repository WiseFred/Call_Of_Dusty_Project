package fr.anthognie.FFA.gui;

import fr.anthognie.Core.utils.ItemBuilder;
import fr.anthognie.FFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ShopGUI {

    public static final String GUI_TITLE = "§6§lArmurerie";
    public static final String ADMIN_TITLE = "§c§lArmurerie (Édition)"; // Ajouté

    private final Main plugin;
    private final File file;
    private final FileConfiguration config;

    public ShopGUI(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "shop.yml");
        if (!file.exists()) {
            plugin.saveResource("shop.yml", false);
        }
        this.config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
    }

    // Méthode simple (Mode Joueur)
    public void open(Player player) {
        open(player, false);
    }

    // Méthode complète (Mode Admin supporté)
    public void open(Player player, boolean adminMode) {
        String title = adminMode ? ADMIN_TITLE : GUI_TITLE;
        Inventory inv = Bukkit.createInventory(null, 54, title);

        ConfigurationSection items = config.getConfigurationSection("items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                try {
                    int slot = items.getInt(key + ".slot");
                    String name = items.getString(key + ".name");
                    Material material = Material.valueOf(items.getString(key + ".material"));
                    int price = items.getInt(key + ".price");
                    int amount = items.getInt(key + ".amount", 1);

                    // Lore différent selon le mode
                    ItemStack item;
                    if (adminMode) {
                        item = ItemBuilder.create(material,
                                name.replace("&", "§"),
                                "§7Prix : §6" + price,
                                "§7ID: §f" + key,
                                "§c[Clic Droit] Supprimer");
                    } else {
                        item = ItemBuilder.create(material,
                                name.replace("&", "§"),
                                "§7Prix : §6" + price + " coins",
                                "§7Quantité : §e" + amount,
                                "",
                                "§eClic pour acheter");
                    }
                    item.setAmount(amount);

                    inv.setItem(slot, item);

                } catch (Exception e) {
                    plugin.getLogger().warning("Erreur chargement item shop: " + key);
                }
            }
        }

        // Décoration
        ItemStack vitre = ItemBuilder.create(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 48, 50, 51, 52, 53}) {
            if (inv.getItem(i) == null) inv.setItem(i, vitre);
        }

        inv.setItem(49, ItemBuilder.create(Material.BARRIER, "§cFermer"));

        player.openInventory(inv);
    }

    // --- MÉTHODES D'ÉDITION (POUR SHOPEDITLISTENER) ---

    public FileConfiguration getShopConfig() { // Alias demandé par l'erreur
        return config;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void removeShopItem(int slot) {
        ConfigurationSection items = config.getConfigurationSection("items");
        if (items == null) return;

        String keyToRemove = null;
        for (String key : items.getKeys(false)) {
            if (items.getInt(key + ".slot") == slot) {
                keyToRemove = key;
                break;
            }
        }

        if (keyToRemove != null) {
            config.set("items." + keyToRemove, null);
            saveConfig();
        }
    }

    public void setShopItem(String itemId, String materialName, int price, int slot) {
        // Génère une clé unique si nécessaire, ou utilise l'ID comme clé
        String key = "item_" + UUID.randomUUID().toString().substring(0, 8);

        // On essaie de trouver un nom sympa
        String displayName = "§f" + itemId;

        config.set("items." + key + ".name", displayName);
        config.set("items." + key + ".material", materialName);
        config.set("items." + key + ".item-id", itemId); // ID réel (ex: cgm:pistol)
        config.set("items." + key + ".price", price);
        config.set("items." + key + ".amount", 1);
        config.set("items." + key + ".slot", slot);

        saveConfig();
    }

    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}