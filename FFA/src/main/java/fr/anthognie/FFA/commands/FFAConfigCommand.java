package fr.anthognie.FFA.commands;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.gui.FFAConfigGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FFAConfigCommand implements CommandExecutor {

    private final FFAConfigGUI ffaConfigGUI;

    public FFAConfigCommand(Main plugin) {
        this.ffaConfigGUI = plugin.getFfaConfigGUI();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs.");
            return true;
        }

        ffaConfigGUI.open((Player) sender);
        return true;
    }
}