package fr.anthognie.FFA.commands;

import fr.anthognie.FFA.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand implements CommandExecutor {

    private final Main plugin;

    public LeaveCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSeul un joueur peut exécuter cette commande.");
            return true;
        }

        Player player = (Player) sender;

        if (player.getWorld().getName().equals(plugin.getFfaManager().getFFAWorldName())) {
            plugin.getFfaManager().leaveArena(player);
            player.sendMessage("§cVous avez quitté le FFA.");
        } else {
            player.sendMessage("§cVous n'êtes pas dans l'arène FFA.");
        }

        return true;
    }
}