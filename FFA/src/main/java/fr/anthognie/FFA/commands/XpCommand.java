package fr.anthognie.FFA.commands;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.managers.LevelManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class XpCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public XpCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§cVous n'avez pas la permission.");
            return true;
        }

        // Structure: /xp <gamemode> <action> <player> [amount]
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /xp <ffa> <give|take|set|reset> <joueur> [montant]");
            return true;
        }

        String mode = args[0].toLowerCase();
        String action = args[1].toLowerCase();
        String playerName = args[2];

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("§cJoueur introuvable.");
            return true;
        }

        if (!mode.equals("ffa")) {
            sender.sendMessage("§cMode inconnu. Seul 'ffa' est disponible pour l'instant.");
            return true;
        }

        LevelManager lm = plugin.getLevelManager();
        int amount = 0;

        // Récupération montant (sauf pour reset)
        if (!action.equals("reset")) {
            if (args.length < 4) {
                sender.sendMessage("§cVeuillez spécifier un montant.");
                return true;
            }
            try {
                amount = Integer.parseInt(args[3]);
                if (amount < 0) {
                    sender.sendMessage("§cLe montant doit être positif.");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("§cMontant invalide.");
                return true;
            }
        }

        switch (action) {
            case "give":
                lm.addXp(target, amount);
                sender.sendMessage("§aDonné §e" + amount + " XP §a(FFA) à " + target.getName());
                break;
            case "take":
            case "remove":
                lm.removeXp(target, amount);
                sender.sendMessage("§aRetiré §e" + amount + " XP §a(FFA) à " + target.getName());
                break;
            case "set":
                lm.setTotalXp(target, amount);
                sender.sendMessage("§aDéfini l'XP (FFA) de " + target.getName() + " à §e" + amount);
                break;
            case "reset":
                lm.setTotalXp(target, 0);
                sender.sendMessage("§aXP (FFA) de " + target.getName() + " réinitialisé.");
                break;
            default:
                sender.sendMessage("§cAction inconnue. Utilisez: give, take, set, reset.");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) return new ArrayList<>();

        // Arg 0: Mode
        if (args.length == 1) {
            return Arrays.asList("ffa");
        }
        // Arg 1: Action
        if (args.length == 2) {
            return Arrays.asList("give", "take", "set", "reset");
        }
        // Arg 2: Joueur
        if (args.length == 3) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        // Arg 3: Montant (Suggestion)
        if (args.length == 4 && !args[1].equalsIgnoreCase("reset")) {
            return Arrays.asList("10", "100", "1000", "5000");
        }

        return new ArrayList<>();
    }
}