package fr.anthognie.FFA.listeners;

import fr.anthognie.FFA.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDataListener implements Listener {

    private final Main plugin;

    public PlayerDataListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST) // On charge en premier
    public void onJoin(PlayerJoinEvent event) {
        plugin.getDataManager().loadPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR) // On sauvegarde en dernier
    public void onQuit(PlayerQuitEvent event) {
        plugin.getDataManager().savePlayer(event.getPlayer());
        // On nettoie la mémoire RAM pour éviter les fuites
        plugin.getKillstreakManager().clearPlayer(event.getPlayer());
    }
}