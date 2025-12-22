package fr.anthognie.FFA.commands;

import fr.anthognie.FFA.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FFAConfigCommand implements CommandExecutor {

    private final Main plugin;

    public FFAConfigCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.isOp()) {
                // Ouvre le menu de config interne au module FFA
                // Assure-toi que cette méthode existe dans ton Main FFA, sinon on la crée juste après
                plugin.getFfaConfigGUI().open(player);
            }
        }
        return true;
    }
}