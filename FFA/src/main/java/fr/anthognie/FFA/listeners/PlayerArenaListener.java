package fr.anthognie.FFA.listeners;

import fr.anthognie.FFA.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;

public class PlayerArenaListener implements Listener {

    private final Main plugin;

    public PlayerArenaListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();

        player.setLevel(0);
        player.setExp(0);

        plugin.getScoreboardManager().setScoreboard(player);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String toWorld = player.getWorld().getName();
        String ffaWorld = plugin.getFfaManager().getFFAWorldName();

        plugin.getScoreboardManager().setScoreboard(player);

        if (toWorld.equals(ffaWorld)) {
            plugin.getLevelManager().updateXpBar(player);
        } else {
            plugin.getLevelManager().resetXpBar(player);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getName().equals(plugin.getFfaManager().getFFAWorldName())) {
            String prefix = plugin.getLevelManager().getChatPrefix(player);
            event.setFormat(prefix + event.getFormat());
        }
    }
}