package fr.anthognie.FFA.commands;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.managers.KillstreakManager;
import fr.anthognie.FFA.managers.LevelManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {

    private final Main plugin;

    public StatsCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Cible : Soi-même ou un autre joueur
        Player target;
        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cJoueur introuvable.");
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cLa console doit spécifier un joueur.");
                return true;
            }
            target = (Player) sender;
        }

        LevelManager lvlManager = plugin.getLevelManager();
        KillstreakManager ksManager = plugin.getKillstreakManager();

        int level = lvlManager.getLevel(target);
        String prestige = lvlManager.getPrestigeColor(level) + "■";
        String progressBar = lvlManager.getProgressBar(target);
        int nextKills = lvlManager.getRemainingKills(target);

        int kills = ksManager.getSessionKills(target);
        int deaths = ksManager.getDeaths(target);
        String ratio = ksManager.getKdRatio(target);
        int money = plugin.getEconomyManager().getMoney(target.getUniqueId());

        sender.sendMessage("");
        sender.sendMessage("§8§m--------§r §6§lSTATISTIQUES §8§m--------");
        sender.sendMessage("§7Joueur: §e" + target.getName());
        sender.sendMessage("");
        sender.sendMessage("§7Niveau: " + lvlManager.getPrestigeColor(level) + level + " §8(Prestige: " + prestige + "§8)");
        sender.sendMessage("§7Progression: " + progressBar);
        sender.sendMessage("§7Prochain niveau dans: §b" + nextKills + " kills");
        sender.sendMessage("");
        sender.sendMessage("§7Kills: §a" + kills + " §7| Morts: §c" + deaths + " §7| K/D: §e" + ratio);
        sender.sendMessage("§7Argent: §6" + money + " coins");
        sender.sendMessage("§8§m------------------------------");

        return true;
    }
}