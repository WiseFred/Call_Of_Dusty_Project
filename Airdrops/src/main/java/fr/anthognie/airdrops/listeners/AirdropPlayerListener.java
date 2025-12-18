package fr.anthognie.airdrops.listeners;

import fr.anthognie.airdrops.Main;
import fr.anthognie.airdrops.managers.AirdropManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AirdropPlayerListener implements Listener {

    private final AirdropManager airdropManager;
    private final String ffaWorldName;

    public AirdropPlayerListener(Main plugin, AirdropManager airdropManager) {
        this.airdropManager = airdropManager;
        this.ffaWorldName = plugin.getConfig().getString("world", "ffa");
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        String toWorld = player.getWorld().getName();

        String fromWorld = event.getFrom().getName();

        if (toWorld.equals(ffaWorldName)) {
            airdropManager.showBossBar(player);
        } else if (fromWorld.equals(ffaWorldName)) {
            airdropManager.hideBossBar(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getName().equals(ffaWorldName)) {
            airdropManager.showBossBar(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        airdropManager.hideBossBar(event.getPlayer());
    }
}