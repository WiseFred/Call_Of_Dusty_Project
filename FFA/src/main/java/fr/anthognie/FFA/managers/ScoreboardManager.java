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

    private final Map<UUID, BukkitTask> tasks = new HashMap<>();
    private final Map<UUID, String> lastKillers = new HashMap<>();
    private final Map<UUID, String> lastVictims = new HashMap<>();

    public ScoreboardManager(Main plugin, KillstreakManager killstreakManager, EconomyManager economyManager, FFAManager ffaManager) {
        this.plugin = plugin;
        this.killstreakManager = killstreakManager;
        this.economyManager = economyManager;
        this.ffaManager = ffaManager;
    }

    public void recordKill(Player killer, Player victim) {
        lastVictims.put(killer.getUniqueId(), victim.getName());
        lastKillers.put(victim.getUniqueId(), killer.getName());
    }

    public void setScoreboard(Player player) {
        removePlayerScoreboard(player);

        boolean inFFA = player.getWorld().getName().equals(ffaManager.getFFAWorldName());

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("cod_board", "dummy", "§e§lCALL OF DUSTY");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        if (inFFA) {
            setupFFABoard(board, obj, player);
        } else {
            setupSpawnBoard(board, obj, player);
        }

        // On passe 'inFFA' pour savoir si on doit cacher les pseudos
        updateNametags(board, inFFA);

        player.setScoreboard(board);
        startUpdater(player);
    }

    // Met à jour les équipes sur un scoreboard spécifique
    public void updateNametags(Scoreboard board, boolean hideNames) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            String teamName = p.getName();
            Team team = board.getTeam(teamName);
            if (team == null) {
                team = board.registerNewTeam(teamName);
            }

            // --- GESTION DE LA VISIBILITÉ ---
            if (hideNames) {
                // En FFA : On cache le pseudo au-dessus de la tête
                // Mais le préfixe restera visible dans la TABLIST
                team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            } else {
                // Au Spawn : On affiche tout
                team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            }

            String prefix = plugin.getLevelManager().getChatPrefix(p);
            team.setPrefix(prefix);

            if (!team.hasEntry(p.getName())) {
                team.addEntry(p.getName());
            }
        }
    }

    // Met à jour le tag d'un joueur cible sur TOUS les scoreboards (ex: lors d'un Level Up)
    public void refreshTagForEveryone(Player target) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getScoreboard() != null) {
                Scoreboard board = p.getScoreboard();
                String teamName = target.getName();
                Team team = board.getTeam(teamName);
                if (team == null) {
                    team = board.registerNewTeam(teamName);
                }

                // On vérifie si LE JOUEUR QUI REGARDE (p) est en FFA
                boolean viewerInFFA = p.getWorld().getName().equals(ffaManager.getFFAWorldName());

                if (viewerInFFA) {
                    team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
                } else {
                    team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
                }

                String prefix = plugin.getLevelManager().getChatPrefix(target);
                team.setPrefix(prefix);

                if (!team.hasEntry(target.getName())) {
                    team.addEntry(target.getName());
                }
            }
        }
    }

    private void setupSpawnBoard(Scoreboard board, Objective obj, Player player) {
        obj.getScore("§7(Lobby)").setScore(10);
        obj.getScore("§1").setScore(9);

        Team money = board.registerNewTeam("money");
        money.addEntry("§aCoins: ");
        money.setSuffix("§f" + economyManager.getMoney(player.getUniqueId()));
        obj.getScore("§aCoins: ").setScore(8);

        obj.getScore("§2").setScore(7);

        Team online = board.registerNewTeam("online");
        online.addEntry("§bJoueurs: ");
        online.setSuffix("§f" + Bukkit.getOnlinePlayers().size());
        obj.getScore("§bJoueurs: ").setScore(6);

        Team ping = board.registerNewTeam("ping");
        ping.addEntry("§7Ping: ");
        ping.setSuffix("§f" + player.getPing() + "ms");
        obj.getScore("§7Ping: ").setScore(5);

        obj.getScore("§3").setScore(4);
        obj.getScore("§ewww.tonserveur.com").setScore(3);
    }

    private void setupFFABoard(Scoreboard board, Objective obj, Player player) {
        obj.getScore("§7(FFA Mode)").setScore(15);
        obj.getScore("§1").setScore(14);

        Team streak = board.registerNewTeam("streak");
        streak.addEntry("§cSérie: ");
        streak.setSuffix("§f" + killstreakManager.getKillstreak(player));
        obj.getScore("§cSérie: ").setScore(13);

        Team kills = board.registerNewTeam("kills");
        kills.addEntry("§fKills: ");
        kills.setSuffix("§e" + killstreakManager.getSessionKills(player));
        obj.getScore("§fKills: ").setScore(12);

        obj.getScore("§2").setScore(11);

        Team victim = board.registerNewTeam("lastVictim");
        victim.addEntry("§aVictime: ");
        victim.setSuffix("§7Aucune");
        obj.getScore("§aVictime: ").setScore(10);

        Team killer = board.registerNewTeam("lastKiller");
        killer.addEntry("§cTueur: ");
        killer.setSuffix("§7Aucun");
        obj.getScore("§cTueur: ").setScore(9);

        obj.getScore("§3").setScore(8);

        obj.getScore("§6§lTOP 3 Séries:").setScore(7);
        for(int i=1; i<=3; i++) {
            Team top = board.registerNewTeam("top" + i);
            top.addEntry(ChatColor.values()[i].toString());
            top.setPrefix("§7" + i + ". ---");
            obj.getScore(ChatColor.values()[i].toString()).setScore(7-i);
        }
    }

    public void removePlayerScoreboard(Player player) {
        if (tasks.containsKey(player.getUniqueId())) {
            tasks.get(player.getUniqueId()).cancel();
            tasks.remove(player.getUniqueId());
        }
    }

    private void startUpdater(Player player) {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }

                if (player.getScoreboard().getObjective("cod_board") == null) {
                    setScoreboard(player);
                    return;
                }

                boolean inFFA = player.getWorld().getName().equals(ffaManager.getFFAWorldName());
                updateBoard(player, inFFA);
            }
        };
        tasks.put(player.getUniqueId(), task.runTaskTimer(plugin, 0L, 20L));
    }

    private void updateBoard(Player player, boolean inFFA) {
        Scoreboard board = player.getScoreboard();

        if (inFFA) {
            board.getTeam("streak").setSuffix("§f" + killstreakManager.getKillstreak(player));
            board.getTeam("kills").setSuffix("§e" + killstreakManager.getSessionKills(player));

            String lVictim = lastVictims.getOrDefault(player.getUniqueId(), "§7Aucune");
            board.getTeam("lastVictim").setSuffix("§f" + (lVictim.length() > 10 ? lVictim.substring(0,10) : lVictim));

            String lKiller = lastKillers.getOrDefault(player.getUniqueId(), "§7Aucun");
            board.getTeam("lastKiller").setSuffix("§f" + (lKiller.length() > 10 ? lKiller.substring(0,10) : lKiller));

            Map<String, Integer> top = killstreakManager.getTopKillers(3);
            int i = 1;
            for (Map.Entry<String, Integer> entry : top.entrySet()) {
                Team team = board.getTeam("top" + i);
                if (team != null) team.setPrefix("§e" + i + ". §f" + entry.getKey() + " §7- §c" + entry.getValue());
                i++;
            }
        } else {
            board.getTeam("money").setSuffix("§f" + economyManager.getMoney(player.getUniqueId()));
            board.getTeam("online").setSuffix("§f" + Bukkit.getOnlinePlayers().size());
            board.getTeam("ping").setSuffix("§f" + player.getPing() + "ms");
        }
    }
}