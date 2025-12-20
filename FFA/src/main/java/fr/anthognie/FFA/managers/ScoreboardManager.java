package fr.anthognie.FFA.managers;

import fr.anthognie.Core.managers.EconomyManager;
import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.game.FFAManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private final Main plugin;
    private final KillstreakManager killstreakManager;
    private final EconomyManager economyManager;
    private final FFAManager ffaManager;
    private final ConfigManager ffaConfigManager;

    private final Map<UUID, BukkitTask> tasks = new HashMap<>();

    public ScoreboardManager(Main plugin, KillstreakManager killstreakManager, EconomyManager economyManager, FFAManager ffaManager) {
        this.plugin = plugin;
        this.killstreakManager = killstreakManager;
        this.economyManager = economyManager;
        this.ffaManager = ffaManager;
        this.ffaConfigManager = plugin.getFfaConfigManager();
    }

    public void setPlayerScoreboard(Player player) {
        if (!ffaConfigManager.getConfig().getBoolean("scoreboard.enabled", true)) {
            return;
        }

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("ffa_board", "dummy", "§e§lCALL OF DUSTY");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Lignes fixes
        obj.getScore("§7(FFA Mode)").setScore(15);
        obj.getScore("§1").setScore(14);

        // Stats Joueur
        obj.getScore("§fKills:").setScore(13);
        Team killsTeam = board.registerNewTeam("kills");
        killsTeam.addEntry(ChatColor.RED.toString());
        killsTeam.setPrefix("§e0");
        obj.getScore(ChatColor.RED.toString()).setScore(12);

        obj.getScore("§fArgent:").setScore(11);
        Team moneyTeam = board.registerNewTeam("money");
        moneyTeam.addEntry(ChatColor.AQUA.toString());
        moneyTeam.setPrefix("§e" + economyManager.getMoney(player.getUniqueId()) + " $");
        obj.getScore(ChatColor.AQUA.toString()).setScore(10);

        obj.getScore("§2").setScore(9);

        // Leaderboard (Top 3)
        obj.getScore("§6§lTOP 3 Kills:").setScore(8);
        Team top1 = board.registerNewTeam("top1");
        top1.addEntry(ChatColor.GOLD.toString());
        top1.setPrefix("§7Personne");
        obj.getScore(ChatColor.GOLD.toString()).setScore(7);

        Team top2 = board.registerNewTeam("top2");
        top2.addEntry(ChatColor.GRAY.toString());
        top2.setPrefix("§7Personne");
        obj.getScore(ChatColor.GRAY.toString()).setScore(6);

        Team top3 = board.registerNewTeam("top3");
        top3.addEntry(ChatColor.LIGHT_PURPLE.toString());
        top3.setPrefix("§7Personne");
        obj.getScore(ChatColor.LIGHT_PURPLE.toString()).setScore(5);

        obj.getScore("§3").setScore(4);
        obj.getScore("§ewww.tonserveur.com").setScore(3);

        player.setScoreboard(board);
        startUpdater(player);
    }

    public void removePlayerScoreboard(Player player) {
        if (tasks.containsKey(player.getUniqueId())) {
            tasks.get(player.getUniqueId()).cancel();
            tasks.remove(player.getUniqueId());
        }
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    private void startUpdater(Player player) {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !player.getWorld().getName().equals(ffaManager.getFFAWorldName())) {
                    this.cancel();
                    removePlayerScoreboard(player);
                    return;
                }
                updateScoreboard(player);
            }
        };
        tasks.put(player.getUniqueId(), task.runTaskTimer(plugin, 0L, 20L)); // Update toutes les secondes
    }

    private void updateScoreboard(Player player) {
        Scoreboard board = player.getScoreboard();
        if (board == null) return;

        // Update Stats Perso
        // Note: On affiche ici les Kills Totaux de la session, pas juste le streak
        int sessionKills = killstreakManager.getSessionKills(player);
        int money = economyManager.getMoney(player.getUniqueId());

        Team killsTeam = board.getTeam("kills");
        if (killsTeam != null) killsTeam.setPrefix("  §e" + sessionKills);

        Team moneyTeam = board.getTeam("money");
        if (moneyTeam != null) moneyTeam.setPrefix("  §e" + money + " $");

        // Update Leaderboard
        Map<String, Integer> top = killstreakManager.getTopKillers(3);
        int i = 1;
        for (Map.Entry<String, Integer> entry : top.entrySet()) {
            Team team = board.getTeam("top" + i);
            if (team != null) {
                team.setPrefix(" §e" + i + ". §f" + entry.getKey() + " §7- §a" + entry.getValue());
            }
            i++;
        }
        // Nettoyer les lignes vides si moins de 3 joueurs
        for (; i <= 3; i++) {
            Team team = board.getTeam("top" + i);
            if (team != null) team.setPrefix(" §e" + i + ". §7---");
        }
    }
}