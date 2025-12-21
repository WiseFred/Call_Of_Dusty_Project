package fr.anthognie.FFA.managers;

import fr.anthognie.FFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class KillstreakManager {

    private final Main plugin;

    private final Map<UUID, Integer> killstreaks = new HashMap<>(); // Série (reset mort)
    private final Map<UUID, Integer> sessionKills = new HashMap<>(); // Total Kills (Persistent)
    private final Map<UUID, Integer> deaths = new HashMap<>(); // Total Morts (Persistent)

    public KillstreakManager(Main plugin) {
        this.plugin = plugin;
    }

    public void incrementKillstreak(Player player) {
        UUID uuid = player.getUniqueId();
        int newStreak = getKillstreak(player) + 1;

        killstreaks.put(uuid, newStreak);
        sessionKills.put(uuid, getSessionKills(player) + 1);

        checkRewards(player, newStreak);
    }

    public void incrementDeaths(Player player) {
        UUID uuid = player.getUniqueId();
        deaths.put(uuid, getDeaths(player) + 1);
    }

    private void checkRewards(Player player, int streak) {
        // (Ton code Drône/Nuke est ici)
    }

    // --- GETTERS ---
    public int getKillstreak(Player player) {
        return killstreaks.getOrDefault(player.getUniqueId(), 0);
    }

    public int getSessionKills(Player player) {
        return sessionKills.getOrDefault(player.getUniqueId(), 0);
    }

    public int getDeaths(Player player) {
        return deaths.getOrDefault(player.getUniqueId(), 0);
    }

    public String getKdRatio(Player player) {
        int k = getSessionKills(player);
        int d = getDeaths(player);
        if (d == 0) return String.valueOf(k);
        double ratio = (double) k / (double) d;
        return new DecimalFormat("0.00").format(ratio);
    }

    // --- SETTERS POUR LA SAUVEGARDE ---
    public void setTotalKills(Player player, int amount) {
        sessionKills.put(player.getUniqueId(), amount);
    }

    public void setTotalDeaths(Player player, int amount) {
        deaths.put(player.getUniqueId(), amount);
    }

    public void resetKillstreak(Player player) {
        killstreaks.put(player.getUniqueId(), 0);
    }

    public void resetTotalKills(Player player) {
        sessionKills.put(player.getUniqueId(), 0);
        deaths.put(player.getUniqueId(), 0);
    }

    public void clearPlayer(Player player) {
        killstreaks.remove(player.getUniqueId());
        sessionKills.remove(player.getUniqueId());
        deaths.remove(player.getUniqueId());
    }

    public Map<String, Integer> getTopKillers(int limit) {
        return killstreaks.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        e -> {
                            Player p = Bukkit.getPlayer(e.getKey());
                            return (p != null) ? p.getName() : "Unknown";
                        },
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}