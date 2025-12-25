package fr.anthognie.FFA.listeners;

import fr.anthognie.FFA.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerArenaListener implements Listener {

    private final Main plugin;

    public PlayerArenaListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String ffaWorldName = plugin.getFfaManager().getFFAWorldName();

        // Si le joueur entre dans le monde FFA
        if (player.getWorld().getName().equals(ffaWorldName)) {
            // Active le Scoreboard FFA
            plugin.getScoreboardManager().setScoreboard(player);

            // Met à jour la barre d'XP
            plugin.getLevelManager().updateXpBar(player);
        }
        // Si le joueur quitte le monde FFA
        else if (event.getFrom().getName().equals(ffaWorldName)) {
            // Retire le Scoreboard FFA
            plugin.getScoreboardManager().removePlayerScoreboard(player);
            // Reset XP visuelle
            plugin.getLevelManager().resetXpBar(player);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getName().equals(plugin.getFfaManager().getFFAWorldName())) {
            plugin.getScoreboardManager().setScoreboard(player);
            plugin.getLevelManager().updateXpBar(player);
        }
    }
}