package fr.anthognie.FFA.listeners;

import fr.anthognie.Core.managers.EconomyManager;
import fr.anthognie.Core.managers.ItemConfigManager;
import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.gui.ShopGUI;
import fr.anthognie.FFA.managers.ConfigManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ShopListener implements Listener {

    private final Main plugin;

    public ShopListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ShopGUI.TITLE)) return;
        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);

        int slot = event.getSlot();
        ConfigManager config = plugin.getFfaConfigManager();
        EconomyManager economy = plugin.getEconomyManager();
        ItemConfigManager itemConfig = plugin.getItemConfigManager();

        String itemKey = null;
        if (config.getShopConfig().contains("items")) {
            for (String key : config.getShopConfig().getConfigurationSection("items").getKeys(false)) {
                if (config.getShopConfig().getInt("items." + key + ".slot") == slot) {
                    itemKey = key;
                    break;
                }
            }
        }

        if (itemKey != null) {
            int price = config.getShopConfig().getInt("items." + itemKey + ".price");
            UUID uuid = player.getUniqueId();

            // CORRECTION ECONOMY : Utilisation de getMoney()
            // On suppose que si le joueur est connecté, il a un compte (gestion par défaut)
            if (economy.getMoney(uuid) >= price) {
                economy.removeMoney(uuid, price);

                String kitItemPath = config.getShopConfig().getString("items." + itemKey + ".item-config-path");
                ItemStack itemToGive = null;

                // Sécurité si le path est null
                if (kitItemPath != null) {
                    itemToGive = itemConfig.getItemStack(kitItemPath);
                }

                if (itemToGive != null) {
                    player.getInventory().addItem(itemToGive);
                    player.sendMessage("§aAchat effectué !");
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                } else {
                    player.sendMessage("§cErreur: Item non configuré (Path: " + kitItemPath + ").");
                    economy.addMoney(uuid, price); // Remboursement
                }
            } else {
                player.sendMessage("§cPas assez d'argent !");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            }
            player.closeInventory();
        }
    }
}