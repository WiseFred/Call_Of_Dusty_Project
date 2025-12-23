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

    private final Main plugin;
    private final ConfigManager config;

    public ShopGUI(Main plugin) {
        this.plugin = plugin;
        this.config = plugin.getFfaConfigManager();
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§6Shop FFA");

        if (config.getShopConfig().contains("items")) {
            for (String key : config.getShopConfig().getConfigurationSection("items").getKeys(false)) {
                String path = "items." + key;
                String name = config.getShopConfig().getString(path + ".name");
                int price = config.getShopConfig().getInt(path + ".price");
                int slot = config.getShopConfig().getInt(path + ".slot");
                String materialName = config.getShopConfig().getString(path + ".material");

                Material material = Material.getMaterial(materialName);
                if (material != null) {
                    ItemStack item = new ItemStack(material);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(name != null ? name.replace("&", "§") : material.name());
                        List<String> lore = new ArrayList<>();
                        lore.add("§7Prix: §6" + price + "$");
                        lore.add("§eclic gauche pour acheter");
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                        inv.setItem(slot, item);
                    }
                }
            }
        }

        // --- LINGOT D'OR (SOLDE) ---
        double money = plugin.getEconomyManager().getMoney(player.getUniqueId());
        ItemStack balanceItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = balanceItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lVotre Solde");
            List<String> lore = new ArrayList<>();
            lore.add("§fMontant : §e" + (int)money + "$");
            meta.setLore(lore);
            balanceItem.setItemMeta(meta);
        }
        inv.setItem(49, balanceItem);

        player.openInventory(inv);
    }
}