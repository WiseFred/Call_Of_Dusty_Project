package fr.anthognie.FFA.managers;

import fr.anthognie.FFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class KillstreakManager {

    private final Main plugin;

    private final Map<UUID, Integer> streaks = new HashMap<>();
    private final Map<UUID, Integer> sessionKills = new HashMap<>();
    private final Map<UUID, Integer> totalKills = new HashMap<>();
    private final Map<UUID, Integer> totalDeaths = new HashMap<>();

    public KillstreakManager(Main plugin) {
        this.plugin = plugin;
    }

    public void handleKill(Player killer) {
        UUID id = killer.getUniqueId();
        int currentStreak = streaks.getOrDefault(id, 0) + 1;
        streaks.put(id, currentStreak);
        sessionKills.put(id, sessionKills.getOrDefault(id, 0) + 1);
        totalKills.put(id, getTotalKills(killer) + 1);

        applyBonuses(killer, currentStreak);
    }

    public void handleDeath(Player player) {
        UUID id = player.getUniqueId();
        int lostStreak = streaks.getOrDefault(id, 0);
        if (lostStreak >= 5) {
            player.sendMessage("§cVous avez perdu votre série de " + lostStreak + " kills.");
            if (lostStreak >= 10) Bukkit.broadcastMessage("§e" + player.getName() + " a été stoppé à " + lostStreak + " kills !");
        }
        streaks.remove(id);
        totalDeaths.put(id, getDeaths(player) + 1);
    }

    // AJOUT : Méthode requise par StatsListener
    public void resetKills(Player player) {
        UUID id = player.getUniqueId();
        streaks.remove(id);
        sessionKills.put(id, 0);
        totalKills.put(id, 0);
        player.setStatistic(Statistic.PLAYER_KILLS, 0);
    }

    public int getKillstreak(Player player) { return streaks.getOrDefault(player.getUniqueId(), 0); }
    public int getSessionKills(Player player) { return sessionKills.getOrDefault(player.getUniqueId(), 0); }
    public int getTotalKills(Player player) { return totalKills.getOrDefault(player.getUniqueId(), 0); }
    public int getDeaths(Player player) { return totalDeaths.getOrDefault(player.getUniqueId(), 0); }

    public String getKdRatio(Player player) {
        int k = getTotalKills(player);
        int d = getDeaths(player);
        if (d == 0) return String.valueOf(k);
        double ratio = (double) k / (double) d;
        return new DecimalFormat("0.00").format(ratio);
    }

    public void setTotalKills(Player player, int kills) { totalKills.put(player.getUniqueId(), kills); }
    public void setTotalDeaths(Player player, int deaths) { totalDeaths.put(player.getUniqueId(), deaths); }
    public void resetTotalKills(Player player) { totalKills.put(player.getUniqueId(), 0); }
    public void resetKillstreak(Player player) { streaks.remove(player.getUniqueId()); }

    public void clearPlayer(Player player) {
        UUID id = player.getUniqueId();
        streaks.remove(id);
        sessionKills.remove(id);
        totalKills.remove(id);
        totalDeaths.remove(id);
    }

    public Map<String, Integer> getTopKillers(int limit) {
        return sessionKills.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        e -> { String name = Bukkit.getOfflinePlayer(e.getKey()).getName(); return (name != null) ? name : "Inconnu"; },
                        Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new
                ));
    }

    private void applyBonuses(Player player, int streak) {
        if (streak == 3) {
            player.sendMessage("§e§lSÉRIE DE 3 ! §bUAV (Radar) activé !");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            int count = 0;
            for (Entity e : player.getNearbyEntities(50, 50, 50)) {
                if (e instanceof Player) {
                    Player target = (Player) e;
                    if (!target.getGameMode().name().contains("SPECTATOR")) {
                        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0, false, false));
                        count++;
                    }
                }
            }
            player.sendMessage("§7" + count + " ennemis détectés.");
        }
        else if (streak == 7) {
            player.sendMessage("§6§lSÉRIE DE 7 ! §cChiens d'attaque prêts !");
            player.playSound(player.getLocation(), Sound.ENTITY_WOLF_HOWL, 1f, 1f);
            ItemStack bone = new ItemStack(Material.BONE);
            ItemMeta meta = bone.getItemMeta();
            meta.setDisplayName("§c§lAPPEL DES CHIENS");
            meta.setLore(Arrays.asList("§7Clic droit pour lancer", "§73 chiens d'attaque."));
            bone.setItemMeta(meta);
            player.getInventory().addItem(bone);
        }
        else if (streak == 25) {
            plugin.getFfaManager().triggerNuke(player);
        }
    }
}