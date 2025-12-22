package fr.anthognie.Core.listeners;

import fr.anthognie.Core.Main;
import fr.anthognie.Core.managers.BuildModeManager;
import fr.anthognie.Core.managers.EconomyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveListener implements Listener {

    private final EconomyManager economyManager;
    private final BuildModeManager buildModeManager;

    public PlayerLeaveListener(Main plugin) {
        this.economyManager = plugin.getEconomyManager();
        this.buildModeManager = plugin.getBuildModeManager();
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // 1. Sauvegarder l'économie
        economyManager.savePlayer(player.getUniqueId()); // Correction ici

        // 2. Retirer du mode build (sécurité)
        buildModeManager.removeFromBuildMode(player);
    }
}