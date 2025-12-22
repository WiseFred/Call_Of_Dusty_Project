package fr.anthognie.FFA.commands;

import fr.anthognie.FFA.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NukeCommand implements CommandExecutor {

    private final Main plugin;

    public NukeCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSeul un joueur peut lancer la Nuke.");
            return true;
        }

        Player player = (Player) sender;

        // Permission Admin
        if (!player.hasPermission("ffa.admin") && !player.isOp()) {
            sender.sendMessage("§cVous n'avez pas la permission.");
            return true;
        }

        plugin.getFfaManager().triggerNuke(player);
        return true;
    }
}