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
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cSeul un joueur peut voir son propre solde.");
                return true;
            }
            Player player = (Player) sender;
            int balance = economyManager.getMoney(player.getUniqueId());
            player.sendMessage("§aVotre solde : §e" + balance + " coins.");
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("core.admin.money")) {
                sender.sendMessage("§cVous n'avez pas la permission.");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage("§cUsage: /money give <joueur> <montant>");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            try {
                int amount = Integer.parseInt(args[2]);
                economyManager.addMoney(target.getUniqueId(), amount);
                sender.sendMessage("§aVous avez donné §e" + amount + " coins §aà " + target.getName());
                if (target.isOnline()) {
                    ((Player) target).sendMessage("§aVous avez reçu §e" + amount + " coins.");
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("§cMontant invalide.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            if (!sender.hasPermission("core.admin.money")) {
                sender.sendMessage("§cVous n'avez pas la permission.");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage("§cUsage: /money set <joueur> <montant>");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            try {
                int amount = Integer.parseInt(args[2]);
                economyManager.setMoney(target.getUniqueId(), amount);
                sender.sendMessage("§aSolde de " + target.getName() + " défini à §e" + amount + " coins.");
            } catch (NumberFormatException e) {
                sender.sendMessage("§cMontant invalide.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("take")) {
            if (!sender.hasPermission("core.admin.money")) {
                sender.sendMessage("§cVous n'avez pas la permission.");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage("§cUsage: /money take <joueur> <montant>");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            try {
                int amount = Integer.parseInt(args[2]);
                economyManager.removeMoney(target.getUniqueId(), amount);
                sender.sendMessage("§aVous avez retiré §e" + amount + " coins §aà " + target.getName());
            } catch (NumberFormatException e) {
                sender.sendMessage("§cMontant invalide.");
            }
            return true;
        }

        sender.sendMessage("§cUsage: /money [give/set/take] <joueur> <montant>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("give");
            list.add("take");
            list.add("set");
            return list;
        }
        if (args.length == 2) {
            return null; // Retourne la liste des joueurs
        }
        return Collections.emptyList();
    }
}