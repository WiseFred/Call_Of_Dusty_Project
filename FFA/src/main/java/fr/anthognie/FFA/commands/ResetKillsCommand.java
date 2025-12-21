package fr.anthognie.FFA.commands;

import fr.anthognie.FFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResetKillsCommand implements CommandExecutor {

    private final Main plugin;

    public ResetKillsCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§cVous n'avez pas la permission.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§cUsage: /resetkills <joueur>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cJoueur introuvable.");
            return true;
        }

        // On reset le total (fixe)
        plugin.getKillstreakManager().resetTotalKills(target);

        // On reset aussi la série en cours pour être propre (optionnel)
        plugin.getKillstreakManager().resetKillstreak(target);

        sender.sendMessage("§aLes kills de §e" + target.getName() + " §aont été remis à 0.");
        target.sendMessage("§eVos statistiques de kills ont été réinitialisées par un administrateur.");

        return true;
    }
}