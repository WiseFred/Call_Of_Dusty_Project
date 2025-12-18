package fr.anthognie.Core.listeners;

import fr.anthognie.Core.Main; // <-- NOUVEL IMPORT
import fr.anthognie.Core.managers.BuildModeManager; // <-- NOUVEL IMPORT
import fr.anthognie.Core.managers.EconomyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveListener implements Listener {

    private final EconomyManager economyManager;
    private final BuildModeManager buildModeManager; // <-- NOUVEAU

    public PlayerLeaveListener(Main plugin) { // <-- MODIFIÉ
        this.economyManager = plugin.getEconomyManager();
        this.buildModeManager = plugin.getBuildModeManager(); // <-- NOUVEAU
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // 1. Sauvegarder l'économie
        economyManager.saveAndUnloadPlayer(player.getUniqueId());

        // 2. Retirer du mode build (sécurité)
        buildModeManager.removeFromBuildMode(player);
    }
}