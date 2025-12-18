package fr.anthognie.FFA.listeners;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.gui.ShopGUI;
import fr.anthognie.Core.managers.ItemConfigManager; // Corrigé
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable; // Ajout de l'import

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopEditListener implements Listener {

    private final Main plugin;
    private final ShopGUI shopGUI;
    private final ItemConfigManager itemConfigManager;

    private final Map<UUID, ShopEditSession> editSessions = new HashMap<>();

    public ShopEditListener(Main plugin) {
        this.plugin = plugin;
        this.shopGUI = plugin.getShopGUI();
        this.itemConfigManager = plugin.getItemConfigManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.equals(ShopGUI.ADMIN_TITLE)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null) return;

        int slot = event.getSlot();

        // --- CORRECTION DU BUG ---
        // On vérifie si le joueur a cliqué sur un bouton spécial
        if (slot == shopGUI.getShopConfig().getInt("special-items.close.slot") ||
                slot == shopGUI.getShopConfig().getInt("special-items.balance.slot"))
        {
            event.setCancelled(true);
            if (slot == shopGUI.getShopConfig().getInt("special-items.close.slot")) {
                player.closeInventory();
            }
            return; // On arrête tout, ce n'est pas un clic d'édition
        }
        // -------------------------

        // --- Clic Droit pour SUPPRIMER ---
        if (event.isRightClick() && clickedInv.getType() == InventoryType.CHEST) {
            event.setCancelled(true);
            shopGUI.removeShopItem(slot);
            player.sendMessage("§cItem retiré du slot " + slot + ".");
            shopGUI.open(player, true);
            return;
        }

        // --- Glisser-Déposer pour AJOUTER ---
        if (event.getCursor() != null && event.getCursor().getType() != Material.AIR &&
                clickedInv.getType() == InventoryType.CHEST) {

            event.setCancelled(true);

            ItemStack itemToSell = event.getCursor().clone();

            editSessions.put(player.getUniqueId(), new ShopEditSession(slot, itemToSell));

            player.closeInventory();
            player.sendMessage("§a---------------------------------");
            player.sendMessage("§eItem placé dans le slot " + slot + ".");
            player.sendMessage("§eVeuillez taper le PRIX de cet item dans le chat.");
            player.sendMessage("§7(Tapez 'annuler' pour annuler)");
            player.sendMessage("§a---------------------------------");
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAdminChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!editSessions.containsKey(uuid)) {
            return;
        }

        event.setCancelled(true);
        String message = event.getMessage();
        ShopEditSession session = editSessions.get(uuid);

        editSessions.remove(uuid);

        if (message.equalsIgnoreCase("annuler")) {
            player.sendMessage("§cOpération annulée.");
            return;
        }

        try {
            int price = Integer.parseInt(message);
            if (price < 0) {
                player.sendMessage("§cLe prix doit être positif.");
                return;
            }

            String itemPath = "shop.item." + session.getSlot();

            itemConfigManager.setItemStack(itemPath, session.getItemStack());
            itemConfigManager.saveConfig();

            shopGUI.setShopItem(
                    "item_slot_" + session.getSlot(),
                    itemPath,
                    session.getSlot(),
                    price
            );

            player.sendMessage("§a§lSuccès ! §fItem ajouté au shop au slot " + session.getSlot() + " pour " + price + " coins.");

            new BukkitRunnable() {
                @Override
                public void run() {
                    shopGUI.open(player, true);
                }
            }.runTaskLater(plugin, 1L);

        } catch (NumberFormatException e) {
            player.sendMessage("§c'" + message + "' n'est pas un prix valide. Opération annulée.");
        }
    }


    private static class ShopEditSession {
        private final int slot;
        private final ItemStack itemStack;

        public ShopEditSession(int slot, ItemStack itemStack) {
            this.slot = slot;
            this.itemStack = itemStack;
        }

        public int getSlot() { return slot; }
        public ItemStack getItemStack() { return itemStack; }
    }
}