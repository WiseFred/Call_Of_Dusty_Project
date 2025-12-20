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
import java.util.UUID;
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

        // --- CORRECTION CIBLAGE JOUEUR ---
        String targetName = args[1];
        UUID targetUUID = null;
        String realName = targetName;

        // 1. Essayer de trouver un joueur en ligne (Priorité)
        Player onlineTarget = Bukkit.getPlayer(targetName);
        if (onlineTarget != null) {
            targetUUID = onlineTarget.getUniqueId();
            realName = onlineTarget.getName();
        } else {
            // 2. Sinon, chercher dans les données offline
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
            if (offlineTarget.hasPlayedBefore()) {
                targetUUID = offlineTarget.getUniqueId();
                realName = offlineTarget.getName();
            }
        }

        if (targetUUID == null) {
            sender.sendMessage("§cErreur : Le joueur '" + targetName + "' est introuvable ou n'a jamais joué.");
            return true;
        }
        // ---------------------------------

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "give": {
                if (args.length < 3) return sendAdminHelp(sender);
                try {
                    int amount = Integer.parseInt(args[2]);
                    if (amount <= 0) {
                        sender.sendMessage("§cLe montant doit être positif.");
                        return true;
                    }
                    economyManager.addMoney(targetUUID, amount);
                    sender.sendMessage("§aVous avez donné §e" + amount + " coins§a à " + realName + ".");

                    Player targetP = Bukkit.getPlayer(targetUUID);
                    if(targetP != null) {
                        targetP.sendMessage("§aVous avez reçu §e" + amount + " coins§a.");
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
                    economyManager.setMoney(targetUUID, amount);
                    sender.sendMessage("§aLe solde de " + realName + " a été défini à §e" + amount + " coins§a.");
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
                    economyManager.removeMoney(targetUUID, amount);
                    sender.sendMessage("§aVous avez retiré §e" + amount + " coins§a à " + realName + ".");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c'" + args[2] + "' n'est pas un montant valide.");
                }
                break;
            }
            case "clear": {
                economyManager.setMoney(targetUUID, 0);
                sender.sendMessage("§aLe solde de " + realName + " a été réinitialisé à §e0 coins§a.");
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
        return new ArrayList<>();
    }
}