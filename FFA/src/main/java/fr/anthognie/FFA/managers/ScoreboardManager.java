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

    // CORRECTION: La méthode s'appelle bien setScoreboard pour correspondre au Listener
    public void setScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("cod_board", Criteria.DUMMY, "§6§lCALL OF DUSTY");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        player.setScoreboard(board);
        boards.put(player.getUniqueId(), board);

        // Mise à jour immédiate à la connexion
        updateScoreboard(player, getTopKillstreakPlayers());
    }

    public void removePlayerScoreboard(Player player) {
        boards.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    // CORRECTION : J'ai remis la méthode recordKill qui manquait !
    public void recordKill(Player killer, Player victim) {
        // On recalcule le top pour être à jour
        List<Player> top = getTopKillstreakPlayers();
        updateScoreboard(killer, top);
        updateScoreboard(victim, top);
    }

    // Méthode utilitaire pour avoir le Top 3
    private List<Player> getTopKillstreakPlayers() {
        List<Player> topPlayers = new ArrayList<>();
        World ffaWorld = Bukkit.getWorld(ffaManager.getFFAWorldName());

        if (ffaWorld != null) {
            topPlayers.addAll(ffaWorld.getPlayers());
            // Tri décroissant par killstreak
            topPlayers.sort((p1, p2) -> Integer.compare(killstreakManager.getKillstreak(p2), killstreakManager.getKillstreak(p1)));
            // On retire ceux qui ont 0 ou moins
            topPlayers.removeIf(p -> killstreakManager.getKillstreak(p) <= 0);
        }
        return topPlayers;
    }

    private void updateScoreboard(Player player, List<Player> topKillstreaks) {
        // Si le joueur n'est pas en FFA, on lui retire son scoreboard personnalisé
        if (!player.getWorld().getName().equals(ffaManager.getFFAWorldName())) {
            if (player.getScoreboard().getObjective("cod_board") != null) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
            return;
        }

        // Si le joueur est en FFA mais a perdu son board (ex: déco/reco rapide), on le remet
        if (!boards.containsKey(player.getUniqueId()) || player.getScoreboard().getObjective("cod_board") == null) {
            setScoreboard(player);
            return;
        }

        Scoreboard board = boards.get(player.getUniqueId());
        Objective obj = board.getObjective("cod_board");
        if (obj == null) return;

        // Données
        int kills = player.getStatistic(Statistic.PLAYER_KILLS);
        int deaths = player.getStatistic(Statistic.DEATHS);
        int streak = killstreakManager.getKillstreak(player);
        double money = economyManager.getMoney(player.getUniqueId()); // getMoney corrigé
        double ratio = (deaths == 0) ? kills : (double) kills / deaths;

        // Lignes
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

        // Section TOP 3
        lines.add("§6§lTOP SÉRIES:");
        if (topKillstreaks.isEmpty()) {
            lines.add("§7Aucune en cours");
        } else {
            for (int i = 0; i < Math.min(3, topKillstreaks.size()); i++) {
                Player p = topKillstreaks.get(i);
                int s = killstreakManager.getKillstreak(p);
                lines.add("§e" + (i + 1) + ". §f" + p.getName() + " §7- §b" + s);
            }
        }

        lines.add("§4");
        lines.add("§ewww.callofdusty.fr");
        lines.add("§8§m-------------------- ");

        // Nettoyage et Application
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
                // On calcule le top 3 une seule fois pour tout le monde
                List<Player> topPlayers = getTopKillstreakPlayers();

                for (UUID uuid : boards.keySet()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null && p.isOnline()) {
                        updateScoreboard(p, topPlayers);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // 1 seconde
    }
}