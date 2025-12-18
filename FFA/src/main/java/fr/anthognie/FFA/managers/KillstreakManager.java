package fr.anthognie.FFA.managers;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KillstreakManager {

    private final Map<UUID, Integer> killstreaks = new HashMap<>();

    public void incrementKillstreak(Player player) {
        UUID uuid = player.getUniqueId();
        int currentStreak = getKillstreak(player);
        killstreaks.put(uuid, currentStreak + 1);
    }

    public int getKillstreak(Player player) {
        return killstreaks.getOrDefault(player.getUniqueId(), 0);
    }

    public void resetKillstreak(Player player) {
        killstreaks.put(player.getUniqueId(), 0);
    }

    public void clearPlayer(Player player) {
        killstreaks.remove(player.getUniqueId());
    }
}