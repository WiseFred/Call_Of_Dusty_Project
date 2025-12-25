package fr.anthognie.spawn.managers;

import fr.anthognie.Core.managers.EconomyManager;
import fr.anthognie.spawn.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private final Main plugin;
    private final EconomyManager economyManager;
    private final Map<UUID, Scoreboard> boards = new HashMap<>();

    public ScoreboardManager(Main plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getCore().getEconomyManager();
        startUpdater();
    }

    public void setScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("spawn_board", Criteria.DUMMY, "§6§lCALL OF DUSTY");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        player.setScoreboard(board);
        boards.put(player.getUniqueId(), board);
        updateScoreboard(player);
    }

    public void removeScoreboard(Player player) {
        boards.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    private void updateScoreboard(Player player) {
        if (!boards.containsKey(player.getUniqueId())) return;

        Scoreboard board = boards.get(player.getUniqueId());
        Objective obj = board.getObjective("spawn_board");
        if (obj == null) return;

        double money = economyManager.getMoney(player.getUniqueId());
        int ping = getPing(player);
        int onlineCount = Bukkit.getOnlinePlayers().size();
        String rank = getRank(player);

        // Construction du Scoreboard "Lobby"
        String[] lines = {
                "§8§m--------------------",
                "§fJoueur : §7" + player.getName(),
                "§fGrade : " + rank,
                "§1",
                "§fArgent : §6" + (int)money + "$",
                "§fPing : §e" + ping + " ms",
                "§2",
                "§fEn ligne : §b" + onlineCount,
                "§3",
                "§7IP: §ecallofdusty.fr",
                "§8§m-------------------- "
        };

        for (String entry : board.getEntries()) board.resetScores(entry);

        int score = lines.length;
        for (String line : lines) {
            obj.getScore(line).setScore(score);
            score--;
        }
    }

    // Détection simple du grade (à adapter selon tes permissions)
    private String getRank(Player player) {
        if (player.isOp()) return "§cAdmin"; // OP = Admin
        if (player.hasPermission("core.admin")) return "§cAdmin";
        if (player.hasPermission("core.mod")) return "§9Modo";
        if (player.hasPermission("core.builder")) return "§eBuilder";
        if (player.hasPermission("core.vip")) return "§6VIP";
        return "§7Joueur";
    }

    // Récupération du Ping (Compatible 1.16+)
    private int getPing(Player player) {
        try {
            // Méthode Spigot standard récente
            return player.getPing();
        } catch (NoSuchMethodError e) {
            // Fallback pour vieilles versions (renvoie 0 si pas trouvé)
            return 0;
        }
    }

    private void startUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getWorld().getName().equals("world")) {
                        if (!boards.containsKey(p.getUniqueId())) setScoreboard(p);
                        updateScoreboard(p);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 40L); // Mise à jour toutes les 2s
    }
}