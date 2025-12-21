package fr.anthognie.FFA.managers;

import fr.anthognie.Core.managers.EconomyManager;
import fr.anthognie.FFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;
import java.util.UUID;

public class BountyManager {

    private final Main plugin;
    private final EconomyManager economyManager;

    private UUID currentTarget = null;
    private int rewardAmount = 500;
    private BukkitTask survivalTask;

    public BountyManager(Main plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
        startBountyCycle();
    }

    private void startBountyCycle() {
        // Lance une prime toutes les 5 minutes
        new BukkitRunnable() {
            @Override
            public void run() {
                pickRandomTarget();
            }
        }.runTaskTimer(plugin, 1200L, 6000L); // Délai initial 1min, puis toutes les 5min (6000 ticks)
    }

    private void pickRandomTarget() {
        if (survivalTask != null && !survivalTask.isCancelled()) {
            survivalTask.cancel();
        }
        currentTarget = null;

        if (plugin.getServer().getOnlinePlayers().size() < 2) return;

        Object[] players = plugin.getServer().getOnlinePlayers().toArray();
        Player target = (Player) players[new Random().nextInt(players.length)];

        if (!target.getWorld().getName().equals(plugin.getFfaManager().getFFAWorldName())) return;

        currentTarget = target.getUniqueId();

        Bukkit.broadcastMessage("§c§l☠ PRIME : §fUne tête a été mise à prix sur §e" + target.getName() + " §f!");
        Bukkit.broadcastMessage("§7Tuez-le pour gagner §6" + rewardAmount + " coins§7.");
        Bukkit.broadcastMessage("§7Si §e" + target.getName() + " §7survit 2 minutes, il gagne le double !");

        target.sendTitle("§c§lVOUS ÊTES LA CIBLE", "§fSurvivez 2 minutes !", 10, 60, 10);
        target.playSound(target.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);

        // Timer de survie (2 minutes)
        survivalTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (target.isOnline() && currentTarget != null && currentTarget.equals(target.getUniqueId())) {
                    // SUCCÈS SURVIE
                    Bukkit.broadcastMessage("§a§lPRIME ÉCHOUÉE : §e" + target.getName() + " §a a survécu !");
                    economyManager.addMoney(target.getUniqueId(), rewardAmount * 2);
                    target.sendMessage("§a+ " + (rewardAmount * 2) + " coins (Bonus Survie)");
                    currentTarget = null;
                }
            }
        }.runTaskLater(plugin, 2400L); // 120 secondes
    }

    public void checkDeath(Player victim, Player killer) {
        if (currentTarget != null && currentTarget.equals(victim.getUniqueId())) {
            // La cible est morte
            if (survivalTask != null) survivalTask.cancel();
            currentTarget = null;

            if (killer != null && killer != victim) {
                Bukkit.broadcastMessage("§6§lPRIME RÉCLAMÉE ! §e" + killer.getName() + " §7a éliminé la cible.");
                economyManager.addMoney(killer.getUniqueId(), rewardAmount);
                killer.sendMessage("§6+ " + rewardAmount + " coins (Prime)");
            } else {
                Bukkit.broadcastMessage("§7La cible est morte toute seule... La prime est perdue.");
            }
        }
    }
}