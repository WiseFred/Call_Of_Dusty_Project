package fr.anthognie.FFA.listeners;

import fr.anthognie.Core.managers.BuildModeManager;
import fr.anthognie.Core.managers.EconomyManager;
import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.game.FFAManager;
import fr.anthognie.FFA.managers.KillstreakManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerDamageListener implements Listener {

    private final FFAManager ffaManager;
    private final EconomyManager economyManager;
    private final KillstreakManager killstreakManager;
    private final BuildModeManager buildModeManager;
    private final Main plugin;
    private final int headshotBonus;

    public PlayerDamageListener(Main plugin) {
        this.plugin = plugin;
        this.ffaManager = plugin.getFfaManager();
        this.economyManager = plugin.getEconomyManager();
        this.killstreakManager = plugin.getKillstreakManager();
        this.buildModeManager = plugin.getCore().getBuildModeManager();
        this.headshotBonus = plugin.getFfaConfigManager().getConfig().getInt("headshot-bonus.bonus-coins", 10);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player victim = (Player) event.getEntity();

        if (buildModeManager.isInBuildMode(victim)) {
            return;
        }
        if (!victim.getWorld().getName().equals(ffaManager.getFFAWorldName())) {
            return;
        }
        if (ffaManager.isInvincible(victim)) {
            event.setCancelled(true);
            return;
        }

        ffaManager.handlePlayerDamage(victim);

        if (victim.getHealth() - event.getFinalDamage() > 0) {
            return;
        }

        event.setCancelled(true);
        economyManager.clearInventory(victim.getUniqueId());
        killstreakManager.resetKillstreak(victim);

        Player killer = victim.getKiller();

        if (killer != null && killer != victim) {

            boolean wasHeadshot = ffaManager.checkAndConsumeHeadshot(killer);

            if (wasHeadshot) {
                int total = 10 + headshotBonus;
                economyManager.addMoney(killer.getUniqueId(), total);
                killer.sendMessage("§b§lHEADSHOT ! §a+" + total + " coins §fpour avoir tué §e" + victim.getName());
                victim.sendMessage("§cVous avez été tué par (Headshot) §e" + killer.getName());
            } else {
                economyManager.addMoney(killer.getUniqueId(), 10);
                killer.sendMessage("§a+10 coins §fpour avoir tué §e" + victim.getName());
                victim.sendMessage("§cVous avez été tué par §e" + killer.getName());
            }

            killstreakManager.incrementKillstreak(killer);
            ffaManager.startRespawnSequence(victim, killer);

        } else {
            victim.sendMessage("§cVous êtes mort.");
            ffaManager.startRespawnSequence(victim);
        }
    }
}