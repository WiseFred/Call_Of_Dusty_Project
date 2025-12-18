package fr.anthognie.FFA.listeners;

import fr.anthognie.Core.managers.EconomyManager;
import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.game.FFAManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    // --- CORRECTION 1 : AJOUTER CES VARIABLES ---
    private final Main plugin;
    private final FFAManager ffaManager;
    private final EconomyManager economyManager;
    // ------------------------------------------

    public PlayerQuitListener(Main plugin) {
        // --- CORRECTION 2 : SAUVEGARDER LE PLUGIN ---
        this.plugin = plugin;
        // -------------------------------------------
        this.ffaManager = plugin.getFfaManager();
        this.economyManager = plugin.getEconomyManager();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (player.getWorld().getName().equals(ffaManager.getFFAWorldName())) {
            // On sauvegarde son inventaire
            economyManager.saveInventory(player.getUniqueId(), player.getInventory().getContents());

            // On nettoie tous ses timers et données
            ffaManager.clearRegenTask(player);

            // --- CORRECTION 3 : CES LIGNES VONT MARCHER ---
            plugin.getScoreboardManager().removePlayerScoreboard(player);
            plugin.getKillstreakManager().clearPlayer(player);
            // ---------------------------------------------
        }
    }
}