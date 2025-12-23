package fr.anthognie.Core.listeners;

import fr.anthognie.Core.managers.EconomyManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final EconomyManager economyManager;

    public PlayerJoinListener(EconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!economyManager.hasAccount(event.getPlayer().getUniqueId())) {
            economyManager.createAccount(event.getPlayer().getUniqueId());
        }
    }
}