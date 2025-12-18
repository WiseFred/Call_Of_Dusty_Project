package fr.anthognie.airdrops.commands;

import fr.anthognie.airdrops.Main;
import fr.anthognie.airdrops.gui.AirdropConfigGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AirdropConfigCommand implements CommandExecutor {

    private final AirdropConfigGUI airdropConfigGUI;

    public AirdropConfigCommand(Main plugin) {
        this.airdropConfigGUI = plugin.getAirdropConfigGUI();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs.");
            return true;
        }

        airdropConfigGUI.open((Player) sender);
        return true;
    }
}