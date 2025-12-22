package fr.anthognie.airdrops.commands;

import fr.anthognie.airdrops.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AirdropConfigCommand implements CommandExecutor {

    private final Main plugin;

    public AirdropConfigCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && sender.isOp()) {
            plugin.getAirdropConfigGUI().open((Player) sender);
        }
        return true;
    }
}