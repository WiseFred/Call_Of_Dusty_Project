package fr.anthognie.Core.commands;

import fr.anthognie.Core.managers.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MoneyCommand implements CommandExecutor, TabCompleter {

    private final EconomyManager economyManager;

    public MoneyCommand(EconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) return true;
            Player player = (Player) sender;
            int balance = (int) economyManager.getMoney(player.getUniqueId());
            player.sendMessage("§aVotre solde : §e" + balance + " coins.");
            return true;
        }

        String sub = args[0];
        if (args.length >= 3 && (sub.equalsIgnoreCase("give") || sub.equalsIgnoreCase("set") || sub.equalsIgnoreCase("take"))) {
            if (!sender.hasPermission("core.admin.money")) {
                sender.sendMessage("§cPas de permission.");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            try {
                double amount = Double.parseDouble(args[2]);
                if (sub.equalsIgnoreCase("give")) economyManager.addMoney(target.getUniqueId(), amount);
                if (sub.equalsIgnoreCase("take")) economyManager.removeMoney(target.getUniqueId(), amount);
                if (sub.equalsIgnoreCase("set")) economyManager.setMoney(target.getUniqueId(), amount);
                sender.sendMessage("§aOpération réussie sur " + target.getName());
            } catch (NumberFormatException e) {
                sender.sendMessage("§cMontant invalide.");
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("give"); list.add("take"); list.add("set");
            return list;
        }
        return null;
    }
}