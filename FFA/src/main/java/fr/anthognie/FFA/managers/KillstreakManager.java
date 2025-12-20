package fr.anthognie.FFA.managers;

import fr.anthognie.FFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class KillstreakManager {

    private final Main plugin;

    // Kills consécutifs (reset à la mort) -> POUR LE LEADERBOARD
    private final Map<UUID, Integer> killstreaks = new HashMap<>();

    // Kills totaux de la session (ne reset pas) -> POUR LES STATS PERSO
    private final Map<UUID, Integer> sessionKills = new HashMap<>();

    public KillstreakManager(Main plugin) {
        this.plugin = plugin;
    }

    public void incrementKillstreak(Player player) {
        UUID uuid = player.getUniqueId();
        killstreaks.put(uuid, getKillstreak(player) + 1);
        sessionKills.put(uuid, getSessionKills(player) + 1);
    }

    public int getKillstreak(Player player) {
        return killstreaks.getOrDefault(player.getUniqueId(), 0);
    }

    public int getSessionKills(Player player) {
        return sessionKills.getOrDefault(player.getUniqueId(), 0);
    }

    public void resetKillstreak(Player player) {
        // On ne reset que la série, pas le total
        killstreaks.put(player.getUniqueId(), 0);
    }

    public void clearPlayer(Player player) {
        killstreaks.remove(player.getUniqueId());
        sessionKills.remove(player.getUniqueId());
    }

    // --- CORRECTION ICI : On utilise 'killstreaks' pour le classement ---
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