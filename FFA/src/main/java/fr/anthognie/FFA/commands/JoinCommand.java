package fr.anthognie.FFA.commands;

import fr.anthognie.FFA.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinCommand implements CommandExecutor {

    private final Main plugin;

    public JoinCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            plugin.getFfaManager().joinArena((Player) sender);
        }
        return true;
    }
}