package fr.anthognie.FFA.gui;

import fr.anthognie.Core.utils.ItemBuilder;
import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class ShopGUI {

    private final Main plugin;
    public static final String TITLE = "§8Armurerie";
    public static final String ADMIN_TITLE = "§cArmurerie (Édition)";

    public ShopGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        open(player, false);
    }

    public void open(Player player, boolean adminMode) {
        String title = adminMode ? ADMIN_TITLE : TITLE;
        Inventory inv = Bukkit.createInventory(null, 54, title);
        ConfigManager config = plugin.getFfaConfigManager();

        if (config.getShopConfig().contains("items")) {
            ConfigurationSection items = config.getShopConfig().getConfigurationSection("items");
            if (items != null) {
                for (String key : items.getKeys(false)) {
                    String path = "items." + key;
                    String name = config.getShopConfig().getString(path + ".name");
                    int price = config.getShopConfig().getInt(path + ".price");
                    int slot = config.getShopConfig().getInt(path + ".slot");
                    String materialName = config.getShopConfig().getString(path + ".material");

                    Material material = Material.matchMaterial(materialName);
                    if (material == null) material = Material.BARRIER;

                    ItemStack item;
                    if (adminMode) {
                        item = ItemBuilder.create(material, name,
                                "§7Prix : §6" + price + "$",
                                "§e----------------",
                                "§b[Clic Gauche] §7Déplacer",
                                "§c[Clic Droit] §7Supprimer"
                        );
                    } else {
                        item = ItemBuilder.create(material, name, "§7Prix : §6" + price + "$", "§eClique pour acheter");
                    }
                    // Sécurité slot invalide
                    if (slot >= 0 && slot < 54) {
                        inv.setItem(slot, item);
                    }
                }
            }
        }

        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 1f, 1f);
        player.openInventory(inv);
    }

    // --- LOGIQUE D'ÉDITION ---

    // Ajouter un item depuis l'inventaire
    public void addShopItem(ItemStack item, int slot) {
        String key = item.getType().name().toLowerCase() + "_" + System.currentTimeMillis(); // ID unique
        String path = "items." + key;

        String name = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                ? item.getItemMeta().getDisplayName()
                : item.getType().name();

        ConfigManager config = plugin.getFfaConfigManager();
        config.getShopConfig().set(path + ".name", name);
        config.getShopConfig().set(path + ".material", item.getType().name());
        config.getShopConfig().set(path + ".price", 100); // Prix par défaut
        config.getShopConfig().set(path + ".slot", slot);

        // On suppose que l'item Core a le même nom pour simplifier,
        // sinon il faudra le configurer manuellement dans le fichier plus tard
        config.getShopConfig().set(path + ".item-config-path", "kits.ffa." + item.getType().name().toLowerCase());

        config.saveShopConfig();
    }

    // Supprimer un item
    public void removeShopItem(int slot) {
        ConfigManager config = plugin.getFfaConfigManager();
        String key = getKeyBySlot(slot);
        if (key != null) {
            config.getShopConfig().set("items." + key, null);
            config.saveShopConfig();
        }
    }

    // Déplacer un item (Swap)
    public void moveShopItem(int oldSlot, int newSlot) {
        ConfigManager config = plugin.getFfaConfigManager();
        String key1 = getKeyBySlot(oldSlot);
        String key2 = getKeyBySlot(newSlot); // Peut être null si slot vide

        if (key1 != null) {
            // On met à jour le slot de l'item 1
            config.getShopConfig().set("items." + key1 + ".slot", newSlot);

            // Si il y avait un item sur la destination, on l'échange (il va sur oldSlot)
            if (key2 != null) {
                config.getShopConfig().set("items." + key2 + ".slot", oldSlot);
            }

            config.saveShopConfig();
        }
    }

    // Utilitaire pour trouver la clé YML via le slot
    private String getKeyBySlot(int slot) {
        ConfigurationSection section = plugin.getFfaConfigManager().getShopConfig().getConfigurationSection("items");
        if (section == null) return null;

        for (String key : section.getKeys(false)) {
            if (section.getInt(key + ".slot") == slot) {
                return key;
            }
        }
        return null;
    }
}