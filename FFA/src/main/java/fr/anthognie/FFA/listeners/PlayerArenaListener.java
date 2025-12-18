package fr.anthognie.FFA.listeners;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.game.FFAManager;
import fr.anthognie.FFA.managers.ScoreboardManager;
import org.bukkit.Bukkit; // NOUVEL IMPORT
import org.bukkit.Location; // NOUVEL IMPORT
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerArenaListener implements Listener {

    private final Main plugin; // NOUVEAU
    private final FFAManager ffaManager;
    private final ScoreboardManager scoreboardManager;

    public PlayerArenaListener(Main plugin) {
        this.plugin = plugin; // MODIFIÉ
        this.ffaManager = plugin.getFfaManager();
        this.scoreboardManager = plugin.getScoreboardManager();
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String ffaWorldName = ffaManager.getFFAWorldName();
        String newWorldName = player.getWorld().getName();
        String fromWorldName = event.getFrom().getName();

        if (newWorldName.equals(ffaWorldName) && !fromWorldName.equals(ffaWorldName)) {

            if (!plugin.getFfaConfigManager().getConfig().getBoolean("game-enabled", true) && !player.isOp()) {
                player.sendMessage("§cLe mode de jeu FFA est actuellement désactivé.");
                // On le renvoie au spawn
                Location spawn = new Location(Bukkit.getWorld("world"), 0.5, 100.0, 0.5);
                player.teleport(spawn);
                return;
            }
            // -----------------------------

            // Le joueur ENTRE dans l'arène
            ffaManager.joinArena(player);
            scoreboardManager.setPlayerScoreboard(player);
        }
    }
}