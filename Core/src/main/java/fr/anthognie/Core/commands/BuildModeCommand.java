package fr.anthognie.Core.commands;

import fr.anthognie.Core.Main;
import fr.anthognie.Core.managers.BuildModeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuildModeCommand implements CommandExecutor {

    private final BuildModeManager buildModeManager;

    public BuildModeCommand(Main plugin) {
        this.buildModeManager = plugin.getBuildModeManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSeul un joueur peut utiliser cette commande.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("core.admin.build")) {
            player.sendMessage("§cVous n'avez pas la permission.");
            return true;
        }

        buildModeManager.toggleBuildMode(player);
        return true;
    }
}