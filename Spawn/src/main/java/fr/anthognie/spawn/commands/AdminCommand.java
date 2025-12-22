package fr.anthognie.spawn.commands;

import fr.anthognie.spawn.gui.AdminDashboardGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommand implements CommandExecutor {

    private final AdminDashboardGUI adminDashboardGUI;

    public AdminCommand(AdminDashboardGUI adminDashboardGUI) {
        this.adminDashboardGUI = adminDashboardGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs.");
            return true;
        }

        adminDashboardGUI.open((Player) sender);
        return true;
    }
}