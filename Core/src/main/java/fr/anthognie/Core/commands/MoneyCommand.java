package fr.anthognie.Core.commands;

import fr.anthognie.Core.managers.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MoneyCommand implements CommandExecutor, TabCompleter {
    private EconomyManager economyManager;
    public MoneyCommand(EconomyManager economyManager) {
        this.economyManager = economyManager;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cCette commande ne peut être utilisée que par un joueur.");
                return true;
            }
            Player player = (Player) sender;
            int balance = economyManager.getMoney(player.getUniqueId());
            player.sendMessage("§fVotre solde : §e" + balance + " coins.");
            return true;
        }
        if (!sender.isOp()) {
            sender.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        String subCommand = args[0].toLowerCase();
        if (args.length < 2) {
            sendAdminHelp(sender);
            return true;
        }
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[1]);
        if (!targetPlayer.hasPlayedBefore()) {
            sender.sendMessage("§cErreur : Le joueur '" + args[1] + "' n'a jamais été vu sur ce serveur.");
            return true;
        }
        switch (subCommand) {
            case "give": {
                if (args.length < 3) return sendAdminHelp(sender);
                try {
                    int amount = Integer.parseInt(args[2]);
                    if (amount <= 0) {
                        sender.sendMessage("§cLe montant doit être positif.");
                        return true;
                    }
                    economyManager.addMoney(targetPlayer.getUniqueId(), amount);
                    sender.sendMessage("§aVous avez donné §e" + amount + " coins§a à " + targetPlayer.getName() + ".");
                    if(targetPlayer.isOnline()) {
                        targetPlayer.getPlayer().sendMessage("§aVous avez reçu §e" + amount + " coins§a.");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c'" + args[2] + "' n'est pas un montant valide.");
                }
                break;
            }
            case "set": {
                if (args.length < 3) return sendAdminHelp(sender);
                try {
                    int amount = Integer.parseInt(args[2]);
                    economyManager.setMoney(targetPlayer.getUniqueId(), amount);
                    sender.sendMessage("§aLe solde de " + targetPlayer.getName() + " a été défini à §e" + amount + " coins§a.");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c'" + args[2] + "' n'est pas un montant valide.");
                }
                break;
            }
            case "remove": {
                if (args.length < 3) return sendAdminHelp(sender);
                try {
                    int amount = Integer.parseInt(args[2]);
                    if (amount <= 0) {
                        sender.sendMessage("§cLe montant doit être positif.");
                        return true;
                    }
                    economyManager.removeMoney(targetPlayer.getUniqueId(), amount);
                    sender.sendMessage("§aVous avez retiré §e" + amount + " coins§a à " + targetPlayer.getName() + ".");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c'" + args[2] + "' n'est pas un montant valide.");
                }
                break;
            }
            case "clear": {
                economyManager.setMoney(targetPlayer.getUniqueId(), 0);
                sender.sendMessage("§aLe solde de " + targetPlayer.getName() + " a été réinitialisé à §e0 coins§a.");
                break;
            }
            default:
                sender.sendMessage("§cCommande inconnue. /money <give|set|remove|clear> <joueur> [montant]");
                break;
        }
        return true;
    }
    private boolean sendAdminHelp(CommandSender sender) {
        sender.sendMessage("§cUtilisation incorrecte. Syntaxes :");
        sender.sendMessage("§e/money <give|set|remove> <joueur> <montant>");
        sender.sendMessage("§e/money <clear> <joueur>");
        return true;
    }
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.isOp()) {
            return new ArrayList<>();
        }
        if (args.length == 1) {
            List<String> subCommands = List.of("give", "set", "remove", "clear");
            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 3 && !args[0].equalsIgnoreCase("clear")) {
            return List.of("<montant>");
        }
        return new ArrayList<>();
    }
}