package fr.anthognie.spawn.listeners;

import fr.anthognie.spawn.Main;
import fr.anthognie.spawn.gui.AdminDashboardGUI; // <-- NOUVEL IMPORT
import fr.anthognie.spawn.gui.GameSelectorGUI;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CompassListener implements Listener {

    private final Main plugin;
    private final FileConfiguration config;
    private final GameSelectorGUI gameSelectorGUI;
    private final AdminDashboardGUI adminDashboardGUI; // <-- NOUVEAU
    private final String spawnWorldName;
    private final ItemStack compassItem;
    private final int compassSlot;

    public CompassListener(Main plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.gameSelectorGUI = plugin.getGameSelectorGUI();
        this.adminDashboardGUI = plugin.getAdminDashboardGUI(); // <-- NOUVEAU
        this.spawnWorldName = config.getString("spawn-world", "world");

        this.compassSlot = config.getInt("compass-selector.item.slot", 4);
        this.compassItem = new ItemStack(Material.COMPASS);
        ItemMeta meta = this.compassItem.getItemMeta();
        meta.setDisplayName(config.getString("compass-selector.item.name"));
        meta.setLore(config.getStringList("compass-selector.item.lore"));
        this.compassItem.setItemMeta(meta);
    }

    // ... (isCompass, onJoin, onWorldChange, giveCompass restent inchangés) ...
    private boolean isCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) return false;
        return item.hasItemMeta() && item.getItemMeta().equals(this.compassItem.getItemMeta());
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getName().equals(spawnWorldName)) {
            giveCompass(player);
        }
    }
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getName().equals(spawnWorldName)) {
            giveCompass(player);
        }
        else if (event.getFrom().getName().equals(spawnWorldName)) {
            player.getInventory().remove(this.compassItem);
        }
    }
    private void giveCompass(Player player) {
        player.getInventory().setItem(this.compassSlot, this.compassItem);
    }

    // --- 2. Ouvrir le Menu (MODIFIÉ) ---

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isInSpawn(player)) return;

        Action action = event.getAction();
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (isCompass(event.getItem())) {
                event.setCancelled(true); // Annule l'action de la boussole

                // --- NOUVELLE LOGIQUE ADMIN ---
                if (player.isOp() && player.isSneaking()) {
                    // Si Admin + Sneak: Ouvre le panneau admin
                    adminDashboardGUI.open(player);
                } else {
                    // Sinon: Ouvre le sélecteur de jeu normal
                    gameSelectorGUI.open(player);
                }
                // -----------------------------
            }
        }
    }

    // ... (onInventoryClick, onItemDrop, onHandSwap, isInSpawn restent inchangés) ...
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!isInSpawn(player) || player.getGameMode() == GameMode.CREATIVE) return;
        if (event.getSlot() == this.compassSlot &&
                event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
        }
        if (event.isShiftClick() && event.getInventory().getType() == InventoryType.PLAYER) {
            if (event.getSlot() == this.compassSlot) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (!isInSpawn(event.getPlayer()) || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if (isCompass(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onHandSwap(PlayerSwapHandItemsEvent event) {
        if (!isInSpawn(event.getPlayer()) || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if (isCompass(event.getOffHandItem()) || isCompass(event.getMainHandItem())) {
            event.setCancelled(true);
        }
    }
    private boolean isInSpawn(Player player) {
        return player.getWorld().getName().equals(spawnWorldName);
    }
}