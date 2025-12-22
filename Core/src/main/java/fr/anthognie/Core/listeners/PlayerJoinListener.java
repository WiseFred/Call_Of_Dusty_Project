package fr.anthognie.Core.listeners;

import fr.anthognie.Core.managers.EconomyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private EconomyManager economyManager;

    public PlayerJoinListener(EconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        economyManager.loadPlayer(playerUUID); // Correction ici
        player.sendMessage("§aBienvenue ! §fVous avez §e" + economyManager.getMoney(playerUUID) + " coins.");
    }
}