package fr.anthognie.FFA.commands;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.game.FFAManager;
import fr.anthognie.FFA.managers.KillstreakManager;
import fr.anthognie.FFA.managers.ScoreboardManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand implements CommandExecutor {

    private final FFAManager ffaManager;
    private final ScoreboardManager scoreboardManager;
    private final KillstreakManager killstreakManager;

    public LeaveCommand(Main plugin) {
        this.ffaManager = plugin.getFfaManager();
        this.scoreboardManager = plugin.getScoreboardManager();
        this.killstreakManager = plugin.getKillstreakManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        player.sendMessage("§cExfiltration en cours...");

        // On force le nettoyage, même si ça échoue (try/catch silencieux)
        try { scoreboardManager.removePlayerScoreboard(player); } catch (Exception e) {}
        try { killstreakManager.clearPlayer(player); } catch (Exception e) {}

        // La méthode leaveArena gère la téléportation au Hub et le restore d'inventaire
        ffaManager.leaveArena(player);
        return true;
    }
}