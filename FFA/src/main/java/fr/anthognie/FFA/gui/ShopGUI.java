package fr.anthognie.FFA.gui;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopGUI {

    public static final String TITLE = "§6Shop FFA";
    public static final String ADMIN_TITLE = "§cÉdition Shop FFA";

    private final Main plugin;
    private final ConfigManager config;

    public ShopGUI(Main plugin) {
        this.plugin = plugin;
        this.config = plugin.getFfaConfigManager();
    }

    public void open(Player player) {
        open(player, false);
    }

    public void open(Player player, boolean adminMode) {
        String title = adminMode ? ADMIN_TITLE : TITLE;
        Inventory inv = Bukkit.createInventory(null, 54, title);

        if (config.getShopConfig().contains("items")) {
            for (String key : config.getShopConfig().getConfigurationSection("items").getKeys(false)) {
                String path = "items." + key;
                String matName = config.getShopConfig().getString(path + ".material");
                int price = config.getShopConfig().getInt(path + ".price");
                int slot = config.getShopConfig().getInt(path + ".slot");
                String name = config.getShopConfig().getString(path + ".name");

                Material mat = Material.getMaterial(matName);
                if (mat != null) {
                    ItemStack item = new ItemStack(mat);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(name != null ? name.replace("&", "§") : mat.name());
                        List<String> lore = new ArrayList<>();
                        lore.add("§7Prix: §6" + price + "$");
                        if (adminMode) {
                            lore.add("§e[Clic G] Déplacer");
                            lore.add("§c[Clic D] Supprimer");
                        } else {
                            lore.add("§eClic pour acheter");
                        }
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                        inv.setItem(slot, item);
                    }
                }
            }
        }

        if (!adminMode) {
            double money = plugin.getEconomyManager().getMoney(player.getUniqueId());
            ItemStack gold = new ItemStack(Material.GOLD_INGOT);
            ItemMeta meta = gold.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§6§lVotre Solde");
                List<String> lore = new ArrayList<>();
                lore.add("§fMontant : §e" + (int)money + "$");
                meta.setLore(lore);
                gold.setItemMeta(meta);
            }
            inv.setItem(49, gold);
        }

        player.openInventory(inv);
    }

    public void addShopItem(ItemStack item, int slot) {
        String key = "item_" + System.currentTimeMillis();
        String path = "items." + key;
        config.getShopConfig().set(path + ".material", item.getType().name());
        config.getShopConfig().set(path + ".price", 100);
        config.getShopConfig().set(path + ".slot", slot);
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            config.getShopConfig().set(path + ".name", item.getItemMeta().getDisplayName());
        }
        config.saveShopConfig();
    }

    public void removeShopItem(int slot) {
        if (!config.getShopConfig().contains("items")) return;
        for (String key : config.getShopConfig().getConfigurationSection("items").getKeys(false)) {
            if (config.getShopConfig().getInt("items." + key + ".slot") == slot) {
                config.getShopConfig().set("items." + key, null);
                config.saveShopConfig();
                break;
            }
        }
    }

    public void moveShopItem(int oldSlot, int newSlot) {
        if (!config.getShopConfig().contains("items")) return;
        for (String key : config.getShopConfig().getConfigurationSection("items").getKeys(false)) {
            if (config.getShopConfig().getInt("items." + key + ".slot") == oldSlot) {
                config.getShopConfig().set("items." + key + ".slot", newSlot);
                config.saveShopConfig();
                break;
            }
        }
    }
}