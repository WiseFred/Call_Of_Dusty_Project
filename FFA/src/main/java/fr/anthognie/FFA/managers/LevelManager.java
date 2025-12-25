package fr.anthognie.FFA.managers;

import fr.anthognie.FFA.Main;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LevelManager {

    private final Main plugin;
    private final Map<UUID, Integer> playerXp = new HashMap<>();
    private static final int XP_PER_LEVEL = 1000;

    public LevelManager(Main plugin) {
        this.plugin = plugin;
    }

    // --- GESTION XP ---

    public void addXp(Player player, int amount) {
        int currentXp = getTotalXp(player);
        int newXp = currentXp + amount;

        int oldLevel = getLevelFromXp(currentXp);
        int newLevel = getLevelFromXp(newXp);

        playerXp.put(player.getUniqueId(), newXp);

        if (newLevel > oldLevel) {
            player.sendMessage("§b§lNIVEAU SUPÉRIEUR ! §7Vous passez niveau §e" + newLevel);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        }
        updateXpBar(player);
    }

    // METHODE MANQUANTE
    public void removeXp(Player player, int amount) {
        int current = getTotalXp(player);
        setTotalXp(player, Math.max(0, current - amount));
        updateXpBar(player);
    }

    public void setTotalXp(Player player, int xp) {
        playerXp.put(player.getUniqueId(), xp);
        updateXpBar(player);
    }

    public int getTotalXp(Player player) {
        return playerXp.getOrDefault(player.getUniqueId(), 0);
    }

    public int getLevel(Player player) {
        return getLevelFromXp(getTotalXp(player));
    }

    private int getLevelFromXp(int xp) {
        return xp / XP_PER_LEVEL;
    }

    // --- VISUELS & BARRE XP ---

    // METHODE MANQUANTE : Synchronise la barre d'XP vanilla avec notre système
    public void updateXpBar(Player player) {
        int totalXp = getTotalXp(player);
        int level = getLevelFromXp(totalXp);
        int xpInLevel = totalXp % XP_PER_LEVEL;

        player.setLevel(level);
        player.setExp((float) xpInLevel / (float) XP_PER_LEVEL);
    }

    // METHODE MANQUANTE
    public void resetXpBar(Player player) {
        player.setLevel(0);
        player.setExp(0);
    }

    // METHODE MANQUANTE
    public String getChatPrefix(Player player) {
        return "§7[§e" + getLevel(player) + "§7]";
    }

    // Nouvelle barre plus propre (juste les carrés)
    public String getProgressBar(Player player) {
        int xp = getTotalXp(player);
        int percentage = (xp % XP_PER_LEVEL) * 100 / XP_PER_LEVEL;
        int bars = percentage / 10; // 10 barres total

        StringBuilder bar = new StringBuilder("§8[");
        for (int i = 0; i < 10; i++) {
            if (i < bars) bar.append("§a■");
            else bar.append("§7■");
        }
        bar.append("§8]");
        return bar.toString();
    }

    // Pour afficher le texte "500 / 1000 XP" en dessous
    public String getXpText(Player player) {
        int xp = getTotalXp(player);
        int current = xp % XP_PER_LEVEL;
        return "§e" + current + " §7/ §6" + XP_PER_LEVEL + " XP";
    }
}