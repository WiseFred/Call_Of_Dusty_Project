package fr.anthognie.airdrops.listeners;

import fr.anthognie.airdrops.Main;
import fr.anthognie.airdrops.gui.LootKitListGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LootKitChatListener implements Listener {

    private final Main plugin;
    private final Map<UUID, String> sessions = new HashMap<>(); // UUID, KitType (normal/ultimate)

    public LootKitChatListener(Main plugin) {
        this.plugin = plugin;
    }

    public void startSession(Player player, String kitType) {
        sessions.put(player.getUniqueId(), kitType);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAdminChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!sessions.containsKey(uuid)) {
            return;
        }

        event.setCancelled(true);
        String message = event.getMessage();
        String type = sessions.get(uuid);
        sessions.remove(uuid);

        if (message.equalsIgnoreCase("annuler")) {
            player.sendMessage("§cOpération annulée.");
            return;
        }

        String[] parts = message.split(" ");
        if (parts.length != 2) {
            player.sendMessage("§cFormat invalide. Tapez: <nom_du_kit> <chance>");
            player.sendMessage("§eExemple: kit_armes 40");
            return;
        }

        String kitName = parts[0];
        int chance;
        try {
            chance = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cLa chance '" + parts[1] + "' n'est pas un nombre.");
            return;
        }

        player.performCommand("airdrop createkit " + type + " " + kitName + " " + chance);

        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getLootKitListGUI().open(player, type);
            }
        }.runTaskLater(plugin, 1L);
    }
}