package fr.anthognie.Core.listeners;

import fr.anthognie.Core.Main;
import fr.anthognie.Core.gui.ItemDatabaseGUI;
import fr.anthognie.Core.managers.ItemConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemDatabaseChatListener implements Listener {

    private final Main plugin;
    private final ItemConfigManager itemConfigManager;
    private final ItemDatabaseGUI gui;

    private final Map<UUID, ItemStack> sessions = new HashMap<>();

    public ItemDatabaseChatListener(Main plugin) {
        this.plugin = plugin;
        this.itemConfigManager = plugin.getItemConfigManager();
        this.gui = plugin.getItemDatabaseGUI();
    }

    public void startSession(Player player, ItemStack item) {
        sessions.put(player.getUniqueId(), item);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAdminChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!sessions.containsKey(uuid)) {
            return;
        }

        event.setCancelled(true);
        String path = event.getMessage();
        ItemStack item = sessions.get(uuid);

        sessions.remove(uuid);

        if (path.equalsIgnoreCase("annuler")) {
            player.sendMessage("§cOpération annulée.");
            return;
        }

        if (path.contains(" ") || path.isEmpty()) {
            player.sendMessage("§cLe path ne peut pas contenir d'espaces ou être vide. Opération annulée.");
            return;
        }

        itemConfigManager.setItemStack(path, item);
        itemConfigManager.saveConfig();

        player.sendMessage("§a§lSuccès ! §fItem sauvegardé sous le path:");
        player.sendMessage("§e" + path);

        new BukkitRunnable() {
            @Override
            public void run() {
                gui.open(player, 1);
            }
        }.runTaskLater(plugin, 1L);
    }
}