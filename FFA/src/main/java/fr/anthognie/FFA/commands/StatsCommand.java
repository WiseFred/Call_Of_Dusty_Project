package fr.anthognie.FFA.commands;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.managers.KillstreakManager;
import fr.anthognie.FFA.managers.LevelManager;
import org.bukkit.Sound;
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
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        KillstreakManager stats = plugin.getKillstreakManager();
        LevelManager lvlManager = plugin.getLevelManager(); // Ajout du LevelManager

        player.sendMessage("§8§m--------------------------------");
        player.sendMessage("   §6§lSTATISTIQUES DU JOUEUR");
        player.sendMessage("§8§m--------------------------------");

        // Section Niveau (Ajoutée)
        player.sendMessage("§b§lNIVEAU : " + lvlManager.getLevel(player));
        player.sendMessage("§7Progression : " + lvlManager.getProgressBar(player));

        player.sendMessage("");

        player.sendMessage("§e§lSESSION ACTUELLE :");
        player.sendMessage(" §7• Kills : §a" + stats.getSessionKills(player));
        player.sendMessage(" §7• Série en cours : §b" + stats.getKillstreak(player) + " 🔥");

        player.sendMessage("");

        player.sendMessage("§6§lGLOBAL :");
        player.sendMessage(" §7• Kills Totaux : §a" + stats.getTotalKills(player));
        player.sendMessage(" §7• Morts Totales : §c" + stats.getDeaths(player));
        player.sendMessage(" §7• Ratio K/D : §e" + stats.getKdRatio(player));
        player.sendMessage("§8§m--------------------------------");

        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);

        return true;
    }
}