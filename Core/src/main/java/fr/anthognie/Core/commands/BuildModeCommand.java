package fr.anthognie.Core.commands;

import fr.anthognie.Core.Main;
import fr.anthognie.Core.managers.BuildModeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BuildModeCommand implements CommandExecutor {

    private final BuildModeManager buildModeManager;

    public BuildModeCommand(Main plugin) {
        this.buildModeManager = plugin.getBuildModeManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.isOp()) {
            sender.sendMessage("§cVous n'avez pas la permission.");
            return true;
        }

        boolean newState = buildModeManager.toggleBuildMode(player);

        if (newState) {
            player.sendMessage("§a§l[!] Mode Build ACTIVÉ. §fToutes les restrictions sont levées.");
        } else {
            player.sendMessage("§c§l[!] Mode Build DÉSACTIVÉ. §fLes restrictions s'appliquent à nouveau.");
        }
        return true;
    }
}