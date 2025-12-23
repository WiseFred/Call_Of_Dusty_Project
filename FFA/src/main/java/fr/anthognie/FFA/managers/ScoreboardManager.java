package fr.anthognie.FFA.managers;

import fr.anthognie.Core.managers.EconomyManager;
import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.game.FFAManager;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.text.DecimalFormat;
import java.util.*;

public class ScoreboardManager {

    private final Main plugin;
    private final KillstreakManager killstreakManager;
    private final EconomyManager economyManager;
    private final FFAManager ffaManager;
    private final Map<UUID, Scoreboard> boards = new HashMap<>();
    private final DecimalFormat ratioFormat = new DecimalFormat("#.##");

    public ScoreboardManager(Main plugin, KillstreakManager ks, EconomyManager eco, FFAManager ffa) {
        this.plugin = plugin;
        this.killstreakManager = ks;
        this.economyManager = eco;
        this.ffaManager = ffa;
        startUpdater();
    }

    public void setScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("cod_board", Criteria.DUMMY, "§6§lCALL OF DUSTY");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        player.setScoreboard(board);
        boards.put(player.getUniqueId(), board);

        updateBoard(player);
    }

    public void removePlayerScoreboard(Player player) {
        boards.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public void recordKill(Player killer, Player victim) {
        updateBoard(killer);
        updateBoard(victim);
    }

    // Méthode requise par le StatsListener
    public void resetDeaths(Player player) {
        player.setStatistic(Statistic.DEATHS, 0);
        killstreakManager.setTotalDeaths(player, 0);
        updateBoard(player);
    }

    // Méthode requise par le StatsListener
    public void updateBoard(Player player) {
        updateScoreboard(player, getTopKillstreakPlayers());
    }

    // --- GESTION DU TOP SÉRIES (KILLSTREAK) ---
    private List<Player> getTopKillstreakPlayers() {
        List<Player> topPlayers = new ArrayList<>();
        World ffaWorld = Bukkit.getWorld(ffaManager.getFFAWorldName());

        if (ffaWorld != null) {
            topPlayers.addAll(ffaWorld.getPlayers());
            // On trie du plus grand killstreak au plus petit
            topPlayers.sort((p1, p2) -> Integer.compare(
                    killstreakManager.getKillstreak(p2),
                    killstreakManager.getKillstreak(p1)
            ));
            // On retire ceux qui n'ont pas de série (<= 0)
            topPlayers.removeIf(p -> killstreakManager.getKillstreak(p) <= 0);
        }
        return topPlayers;
    }

    private void updateScoreboard(Player player, List<Player> topKillstreaks) {
        // Vérif monde
        if (!player.getWorld().getName().equals(ffaManager.getFFAWorldName())) {
            if (player.getScoreboard().getObjective("cod_board") != null) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
            return;
        }

        if (!boards.containsKey(player.getUniqueId()) || player.getScoreboard().getObjective("cod_board") == null) {
            setScoreboard(player);
            return;
        }

        Scoreboard board = boards.get(player.getUniqueId());
        Objective obj = board.getObjective("cod_board");
        if (obj == null) return;

        int kills = player.getStatistic(Statistic.PLAYER_KILLS);
        int deaths = player.getStatistic(Statistic.DEATHS);
        int streak = killstreakManager.getKillstreak(player);
        double money = economyManager.getMoney(player.getUniqueId());
        double ratio = (deaths == 0) ? kills : (double) kills / deaths;

        List<String> lines = new ArrayList<>();
        lines.add("§8§m--------------------");
        lines.add("§fJoueur : §7" + player.getName());
        lines.add("§1");
        lines.add("§fKills : §a" + kills);
        lines.add("§fMorts : §c" + deaths);
        lines.add("§fRatio : §e" + ratioFormat.format(ratio));
        lines.add("§2");
        lines.add("§fArgent : §6" + (int)money + "$");
        lines.add("§fSérie : §b" + streak);
        lines.add("§3");

        // --- AFFICHAGE DU TOP SÉRIES ---
        lines.add("§6§lTOP SÉRIES:");
        if (topKillstreaks.isEmpty()) {
            lines.add("§7Aucune en cours");
        } else {
            // Affiche les 3 premiers
            for (int i = 0; i < Math.min(3, topKillstreaks.size()); i++) {
                Player p = topKillstreaks.get(i);
                int s = killstreakManager.getKillstreak(p);
                lines.add("§e" + (i + 1) + ". §f" + p.getName() + " §7- §b" + s);
            }
        }

        lines.add("§4");
        lines.add("§ewww.callofdusty.fr");
        lines.add("§8§m-------------------- ");

        // Nettoyage des anciens scores pour éviter les doublons/scintillements
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        int score = lines.size();
        for (String line : lines) {
            obj.getScore(line).setScore(score);
            score--;
        }
    }

    private void startUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // On calcule le Top une seule fois par cycle
                List<Player> topPlayers = getTopKillstreakPlayers();

                for (UUID uuid : boards.keySet()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null && p.isOnline()) {
                        updateScoreboard(p, topPlayers);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Mise à jour toutes les secondes
    }
}