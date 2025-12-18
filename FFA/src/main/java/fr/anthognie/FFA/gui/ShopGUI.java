package fr.anthognie.FFA.gui;

import fr.anthognie.Core.managers.EconomyManager;
import fr.anthognie.FFA.Main;
import fr.anthognie.Core.managers.ItemConfigManager; // MODIFIÉ
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException; // NOUVEL IMPORT
import java.util.ArrayList;
import java.util.List;

public class ShopGUI {

    private final Main plugin;
    private final EconomyManager economyManager;
    private final ItemConfigManager itemConfigManager;

    private File configFile; // NOUVEAU
    private FileConfiguration shopConfig;
    private String shopTitle;
    private int shopSize;

    // --- TITRES ---
    public static final String PLAYER_TITLE = "§e§lCall of Dusty Shop";
    public static final String ADMIN_TITLE = "§c§l[ÉDITION] §eShop";

    public ShopGUI(Main plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
        this.itemConfigManager = plugin.getItemConfigManager();
        loadShopConfig();
    }

    public void loadShopConfig() {
        this.configFile = new File(plugin.getDataFolder(), "shop.yml");
        if (!configFile.exists()) {
            plugin.saveResource("shop.yml", false);
        }
        this.shopConfig = YamlConfiguration.loadConfiguration(configFile);
        this.shopTitle = shopConfig.getString("title", PLAYER_TITLE);
        this.shopSize = shopConfig.getInt("size", 54);
    }

    // Surcharge pour le /shop normal
    public void open(Player player) {
        open(player, false); // false = pas en mode édition
    }

    // Méthode principale
    public void open(Player player, boolean isEditMode) {
        String title = isEditMode ? ADMIN_TITLE : this.shopTitle;
        Inventory inv = Bukkit.createInventory(null, this.shopSize, title);

        // 1. Placer les items spéciaux (Boutons)
        ConfigurationSection specialItemsSection = shopConfig.getConfigurationSection("special-items");
        if (specialItemsSection != null) {
            for (String key : specialItemsSection.getKeys(false)) {
                String path = "special-items." + key;
                int slot = shopConfig.getInt(path + ".slot");
                String matName = shopConfig.getString(path + ".item");
                String name = shopConfig.getString(path + ".name");
                List<String> lore = shopConfig.getStringList(path + ".lore");

                ItemStack item = new ItemStack(Material.getMaterial(matName, false));
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(name);

                List<String> finalLore = new ArrayList<>();
                for (String line : lore) {
                    finalLore.add(line.replace("%money%", String.valueOf(economyManager.getMoney(player.getUniqueId()))));
                }
                meta.setLore(finalLore);

                item.setItemMeta(meta);
                inv.setItem(slot, item);
            }
        }

        // 2. Placer les items à vendre
        ConfigurationSection itemsSection = shopConfig.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                String path = "items." + key;
                String itemPath = shopConfig.getString(path + ".item-path");
                int slot = shopConfig.getInt(path + ".slot");
                int price = shopConfig.getInt(path + ".price");

                ItemStack moddedItem = itemConfigManager.getItemStack(itemPath);
                if (moddedItem == null) {
                    // Si l'item n'existe pas, on met un placeholder (pour l'admin)
                    if (isEditMode) {
                        moddedItem = new ItemStack(Material.BARRIER);
                        ItemMeta meta = moddedItem.getItemMeta();
                        meta.setDisplayName("§cITEM INTROUVABLE");
                        meta.setLore(List.of("§7Path: " + itemPath));
                        moddedItem.setItemMeta(meta);
                    } else {
                        continue;
                    }
                }

                ItemStack displayItem = moddedItem.clone();
                ItemMeta meta = displayItem.getItemMeta();

                if (shopConfig.contains(path + ".name")) {
                    meta.setDisplayName(shopConfig.getString(path + ".name"));
                }

                List<String> lore = shopConfig.getStringList(path + ".lore");
                List<String> finalLore = new ArrayList<>();

                // Si on est en mode édition, on ajoute les infos admin
                if (isEditMode) {
                    finalLore.add("§8--- ADMIN ---");
                    finalLore.add("§7Key: §f" + key);
                    finalLore.add("§7Path: §f" + itemPath);
                    finalLore.add("§7Prix: §f" + price);
                    finalLore.add("§8---------------");
                }

                for (String line : lore) {
                    finalLore.add(line.replace("%price%", String.valueOf(price)));
                }
                meta.setLore(finalLore);

                displayItem.setItemMeta(meta);
                inv.setItem(slot, displayItem);
            }
        }

        player.openInventory(inv);
    }

    // --- NOUVELLES MÉTHODES POUR L'ÉDITION ---

    public void saveShopConfig() {
        try {
            shopConfig.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder shop.yml !");
            e.printStackTrace();
        }
    }

    public void setShopItem(String key, String itemPath, int slot, int price) {
        String path = "items." + key;
        shopConfig.set(path + ".item-path", itemPath);
        shopConfig.set(path + ".slot", slot);
        shopConfig.set(path + ".price", price);

        // On ajoute un lore par défaut
        List<String> defaultLore = new ArrayList<>();
        defaultLore.add("§fPrix: §e%price% coins");
        defaultLore.add("§aClic gauche pour acheter !");
        shopConfig.set(path + ".lore", defaultLore);

        saveShopConfig();
    }

    public void removeShopItem(int slot) {
        ConfigurationSection itemsSection = shopConfig.getConfigurationSection("items");
        if (itemsSection == null) return;

        String keyToRemove = null;
        for (String key : itemsSection.getKeys(false)) {
            if (itemsSection.getInt(key + ".slot") == slot) {
                keyToRemove = key;
                break;
            }
        }

        if (keyToRemove != null) {
            // On supprime l'item du shop.yml
            shopConfig.set("items." + keyToRemove, null);
            saveShopConfig();

            // On supprime aussi l'item de items.yml (dans le Core)
            String itemPath = itemsSection.getString(keyToRemove + ".item-path");
            if (itemPath != null) {
                itemConfigManager.setItemStack(itemPath, null);
                itemConfigManager.saveConfig();
            }
        }
    }

    public String getShopTitle() {
        return shopTitle;
    }

    public FileConfiguration getShopConfig() {
        return shopConfig;
    }
}