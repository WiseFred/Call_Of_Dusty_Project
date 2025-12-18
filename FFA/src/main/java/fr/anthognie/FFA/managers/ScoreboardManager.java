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
    private final ConfigManager ffaConfigManager; // <-- NOUVEAU

    private final Map<UUID, BukkitTask> tasks = new HashMap<>();

    public ScoreboardManager(Main plugin, KillstreakManager killstreakManager, EconomyManager economyManager, FFAManager ffaManager) {
        this.plugin = plugin;
        this.killstreakManager = killstreakManager;
        this.economyManager = economyManager;
        this.ffaManager = ffaManager;
        this.ffaConfigManager = plugin.getFfaConfigManager(); // <-- On le récupère
    }

    public void setPlayerScoreboard(Player player) {
        // --- NOUVELLE VÉRIFICATION ---
        if (!ffaConfigManager.getConfig().getBoolean("scoreboard.enabled", true)) {
            return; // Le scoreboard est désactivé
        }
        // -----------------------------

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("ffa_board", "dummy", "§e§lCALL OF DUSTY");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // ... (Le reste de la méthode est inchangé) ...
        obj.getScore("§7(FFA Mode)").setScore(10);
        obj.getScore("§1").setScore(9);
        obj.getScore("§fKills:").setScore(8);
        Team killsTeam = board.registerNewTeam("kills");
        killsTeam.addEntry(ChatColor.RED.toString());
        killsTeam.setPrefix("  §e0");
        obj.getScore(ChatColor.RED.toString()).setScore(7);
        obj.getScore("§2").setScore(6);
        obj.getScore("§fArgent:").setScore(5);
        Team moneyTeam = board.registerNewTeam("money");
        moneyTeam.addEntry(ChatColor.AQUA.toString());
        moneyTeam.setPrefix("  §e" + economyManager.getMoney(player.getUniqueId()) + " $");
        obj.getScore(ChatColor.AQUA.toString()).setScore(4);
        obj.getScore("§3").setScore(3);
        obj.getScore("§ewww.tonserveur.com").setScore(2);
        player.setScoreboard(board);
        startUpdater(player);
    }

    // ... (Le reste de la classe est inchangé) ...
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
        tasks.put(player.getUniqueId(), task.runTaskTimer(plugin, 0L, 30L));
    }
    private void updateScoreboard(Player player) {
        Scoreboard board = player.getScoreboard();
        if (board == null) return;
        // On vérifie si le scoreboard est désactivé PENDANT que le joueur joue
        if (!ffaConfigManager.getConfig().getBoolean("scoreboard.enabled", true)) {
            removePlayerScoreboard(player); // On le supprime
            return;
        }
        int killstreak = killstreakManager.getKillstreak(player);
        int money = economyManager.getMoney(player.getUniqueId());
        Team killsTeam = board.getTeam("kills");
        if (killsTeam != null) {
            killsTeam.setPrefix("  §e" + killstreak);
        }
        Team moneyTeam = board.getTeam("money");
        if (moneyTeam != null) {
            moneyTeam.setPrefix("  §e" + money + " $");
        }
    }
}