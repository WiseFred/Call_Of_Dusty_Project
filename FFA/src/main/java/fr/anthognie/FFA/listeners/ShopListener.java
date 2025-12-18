package fr.anthognie.FFA.listeners;

import fr.anthognie.Core.managers.EconomyManager;
import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.gui.ShopGUI;
import fr.anthognie.Core.managers.ItemConfigManager; // <-- IMPORT MODIFIÉ
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ShopListener implements Listener {

    private final Main plugin;
    private final ShopGUI shopGUI;
    private final EconomyManager economyManager;
    private final ItemConfigManager itemConfigManager;

    public ShopListener(Main plugin) {
        this.plugin = plugin;
        this.shopGUI = plugin.getShopGUI();
        this.economyManager = plugin.getEconomyManager();
        this.itemConfigManager = plugin.getItemConfigManager(); // <-- MODIFIÉ (récupère du plugin)
    }

    // ... (Le reste de la classe est 100% identique) ...
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        String shopTitle = shopGUI.getShopTitle();
        if (!title.equals(shopTitle)) {
            return;
        }
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        int slot = event.getSlot();
        if (slot == shopGUI.getShopConfig().getInt("special-items.close.slot")) {
            player.closeInventory();
            return;
        }
        if (slot == shopGUI.getShopConfig().getInt("special-items.balance.slot")) {
            return;
        }
        ConfigurationSection itemsSection = shopGUI.getShopConfig().getConfigurationSection("items");
        if (itemsSection == null) return;
        for (String key : itemsSection.getKeys(false)) {
            String path = "items." + key;
            if (itemsSection.getInt(key + ".slot") == slot) {
                handlePurchase(player, key);
                return;
            }
        }
    }

    private void handlePurchase(Player player, String itemKey) {
        String path = "items." + itemKey;
        int price = shopGUI.getShopConfig().getInt(path + ".price");
        String itemPath = shopGUI.getShopConfig().getString(path + ".item-path");
        if (economyManager.getMoney(player.getUniqueId()) < price) {
            player.sendMessage("§cVous n'avez pas assez d'argent !");
            player.closeInventory();
            return;
        }
        ItemStack itemToGive = itemConfigManager.getItemStack(itemPath);
        if (itemToGive == null) {
            player.sendMessage("§cErreur: Cet item n'est plus disponible.");
            return;
        }
        economyManager.removeMoney(player.getUniqueId(), price);
        player.getInventory().addItem(itemToGive.clone());
        player.sendMessage("§aAchat réussi ! §f(-" + price + " coins)");
        shopGUI.open(player);
    }
}