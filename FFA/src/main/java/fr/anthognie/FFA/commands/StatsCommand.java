package fr.anthognie.FFA.commands;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.managers.KillstreakManager;
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

        player.sendMessage("§8§m------------------------");
        player.sendMessage("§6§lVOS STATISTIQUES :");
        player.sendMessage("§7Kills Totaux : §a" + stats.getTotalKills(player));
        player.sendMessage("§7Morts Totales : §c" + stats.getDeaths(player));
        player.sendMessage("§7Ratio K/D : §e" + stats.getKdRatio(player));
        player.sendMessage("§7Série Actuelle : §b" + stats.getKillstreak(player));
        player.sendMessage("§8§m------------------------");

        // SON OUVERTURE
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);

        return true;
    }
}