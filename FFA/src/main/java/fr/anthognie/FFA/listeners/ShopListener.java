package fr.anthognie.FFA.listeners;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.gui.ShopGUI;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ShopListener implements Listener {

    private final Main plugin;

    public ShopListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ShopGUI.GUI_TITLE)) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == 49) { // Fermer
            player.closeInventory();
            return;
        }

        // Logique d'achat
        ConfigurationSection items = plugin.getShopGUI().getConfig().getConfigurationSection("items");
        if (items == null) return;

        for (String key : items.getKeys(false)) {
            if (items.getInt(key + ".slot") == slot) {
                int price = items.getInt(key + ".price");

                // 1. Vérifier l'argent
                if (plugin.getEconomyManager().hasMoney(player.getUniqueId(), price)) {

                    // 2. Retirer l'argent
                    plugin.getEconomyManager().removeMoney(player.getUniqueId(), price);

                    // 3. Donner l'item
                    // On récupère l'ID configuré (ex: "cgm:assault_rifle" ou "DIAMOND_SWORD")
                    String itemId = items.getString(key + ".item-id");
                    int amount = items.getInt(key + ".amount", 1);

                    ItemStack toGive;

                    // On essaie de récupérer via ItemConfigManager (pour les mods)
                    ItemStack modItem = plugin.getItemConfigManager().getItemStack(itemId);

                    if (modItem != null) {
                        toGive = modItem.clone();
                    } else {
                        // Sinon c'est du Vanilla
                        try {
                            toGive = new ItemStack(Material.valueOf(itemId));
                        } catch (IllegalArgumentException e) {
                            // Fallback sur le matériel d'affichage si l'ID est invalide
                            toGive = new ItemStack(Material.valueOf(items.getString(key + ".material")));
                        }
                    }

                    toGive.setAmount(amount);
                    player.getInventory().addItem(toGive);

                    player.sendMessage("§aAchat effectué ! §7(-" + price + " coins)");
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

                } else {
                    player.sendMessage("§cPas assez d'argent !");
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                }
                break;
            }
        }
    }
}