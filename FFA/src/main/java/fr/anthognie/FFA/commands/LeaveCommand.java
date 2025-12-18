package fr.anthognie.FFA.commands;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.game.FFAManager;
import fr.anthognie.FFA.managers.KillstreakManager;
import fr.anthognie.FFA.managers.ScoreboardManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.getWorld().getName().equals(ffaManager.getFFAWorldName())) {
            player.sendMessage("§cVous n'êtes pas dans l'arène FFA.");
            return true;
        }

        scoreboardManager.removePlayerScoreboard(player);
        killstreakManager.clearPlayer(player);
        ffaManager.leaveArena(player);
        return true;
    }
}