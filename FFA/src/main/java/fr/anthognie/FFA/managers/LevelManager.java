package fr.anthognie.FFA.managers;

import fr.anthognie.FFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LevelManager {

    private final Main plugin;
    private final Map<UUID, Integer> totalXp = new HashMap<>();
    private final int XP_PER_KILL = 10;

    public LevelManager(Main plugin) {
        this.plugin = plugin;
    }

    public void addXp(Player player, int amount) {
        setTotalXp(player, getTotalXp(player) + amount);
    }

    public void removeXp(Player player, int amount) {
        int newXp = getTotalXp(player) - amount;
        if (newXp < 0) newXp = 0;
        setTotalXp(player, newXp);
    }

    // Méthode centrale pour modifier l'XP
    public void setTotalXp(Player player, int xp) {
        UUID uuid = player.getUniqueId();
        int oldXp = totalXp.getOrDefault(uuid, 0);

        // Cap max (500 niveaux approx)
        if (xp > 1500000) xp = 1500000;

        totalXp.put(uuid, xp);

        int oldLevel = calculateLevel(oldXp);
        int newLevel = calculateLevel(xp);

        // Feedback Level Up uniquement si on monte
        if (newLevel > oldLevel) {
            // On ne joue le son que si c'est un gain naturel (addXp),
            // mais ici on généralise pour que la commande set joue aussi le son
            // si on veut éviter le spam lors d'un gros /xp set, on peut commenter la boucle
            player.sendMessage("§b§lLEVEL UP ! §7Vous êtes passé niveau " + getPrestigeColor(newLevel) + newLevel);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        }

        updateXpBar(player);
    }

    public int getTotalXp(Player player) {
        return totalXp.getOrDefault(player.getUniqueId(), 0);
    }

    // --- LOGIQUE CALCUL & BARRE ---

    private int[] getLevelDetails(Player player) {
        int currentTotalXp = getTotalXp(player);
        int level = 0;
        int xpCursor = currentTotalXp;
        int xpNeeded = getXpNeededForLevel(0);

        while (xpCursor >= xpNeeded) {
            xpCursor -= xpNeeded;
            level++;
            xpNeeded = getXpNeededForLevel(level);
            if (level >= 500) break;
        }
        return new int[]{level, xpCursor, xpNeeded};
    }

    public void updateXpBar(Player player) {
        int[] details = getLevelDetails(player);
        int xpCurrent = details[1];
        int xpNeeded = details[2];

        float progress = (float) xpCurrent / (float) xpNeeded;
        if (progress > 1f) progress = 1f;
        if (progress < 0f) progress = 0f;

        player.setLevel(details[0]);
        player.setExp(progress);
    }

    // --- PROGRESSION & STATS ---

    public String getProgressBar(Player player) {
        int[] details = getLevelDetails(player);
        int xpCurrent = details[1];
        int xpNeeded = details[2];

        int totalBars = 20;
        float percent = (float) xpCurrent / (float) xpNeeded;
        int filledBars = (int) (totalBars * percent);

        StringBuilder bar = new StringBuilder("§8[");
        for (int i = 0; i < totalBars; i++) {
            bar.append(i < filledBars ? "§a|" : "§7|");
        }
        bar.append("§8]");

        return bar.toString() + " §7" + xpCurrent + "§8/§7" + xpNeeded + " XP";
    }

    public int getRemainingKills(Player player) {
        int[] details = getLevelDetails(player);
        int xpMissing = details[2] - details[1];
        return (int) Math.ceil((double) xpMissing / (double) XP_PER_KILL);
    }

    // --- FORMULES ---

    private int getXpNeededForLevel(int level) {
        if (level >= 500) return 1000000;
        return 100 + (level * 25);
    }

    private int calculateLevel(int totalXp) {
        int level = 0;
        int cost = getXpNeededForLevel(0);
        int xp = totalXp;
        while (xp >= cost) {
            xp -= cost;
            level++;
            cost = getXpNeededForLevel(level);
            if (level >= 500) return 500;
        }
        return level;
    }

    public void resetXpBar(Player player) {
        player.setLevel(0);
        player.setExp(0);
    }

    public int getLevel(Player player) {
        return calculateLevel(getTotalXp(player));
    }

    public String getPrestigeColor(int level) {
        if (level >= 500) return "§5§l";
        if (level >= 400) return "§a";
        if (level >= 300) return "§b";
        if (level >= 200) return "§6";
        if (level >= 100) return "§f";
        return "§7";
    }

    public String getChatPrefix(Player player) {
        int lvl = getLevel(player);
        return "§8[" + getPrestigeColor(lvl) + lvl + "§8] ";
    }
}