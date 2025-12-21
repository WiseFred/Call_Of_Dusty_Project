package fr.anthognie.FFA.listeners;

import fr.anthognie.Core.managers.BuildModeManager;
import fr.anthognie.Core.managers.EconomyManager;
import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.game.FFAManager;
import fr.anthognie.FFA.managers.BountyManager;
import fr.anthognie.FFA.managers.KillstreakManager;
import fr.anthognie.FFA.managers.LevelManager;
import fr.anthognie.FFA.managers.ScoreboardManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.lang.reflect.Method;

public class PlayerDamageListener implements Listener {
    // ... (Déclarations inchangées) ...
    private final FFAManager ffaManager;
    private final EconomyManager economyManager;
    private final KillstreakManager killstreakManager;
    private final LevelManager levelManager;
    private final BountyManager bountyManager;
    private final ScoreboardManager scoreboardManager;
    private final BuildModeManager buildModeManager;
    private final Main plugin;

    public PlayerDamageListener(Main plugin) {
        this.plugin = plugin;
        this.ffaManager = plugin.getFfaManager();
        this.economyManager = plugin.getEconomyManager();
        this.killstreakManager = plugin.getKillstreakManager();
        this.levelManager = plugin.getLevelManager();
        this.bountyManager = plugin.getBountyManager();
        this.scoreboardManager = plugin.getScoreboardManager();
        this.buildModeManager = plugin.getCore().getBuildModeManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageEvent event) {
        // ... (Checks inchangés) ...
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();

        if (!victim.getWorld().getName().equals(ffaManager.getFFAWorldName())) return;
        if (ffaManager.isInvincible(victim)) {
            event.setCancelled(true);
            return;
        }

        if (victim.getHealth() - event.getFinalDamage() > 0) {
            ffaManager.handlePlayerDamage(victim);
            return;
        }

        // --- MORT DU JOUEUR ---
        event.setCancelled(true);
        economyManager.clearInventory(victim.getUniqueId());
        killstreakManager.resetKillstreak(victim);

        // NOUVEAU : On compte la mort
        killstreakManager.incrementDeaths(victim);

        bountyManager.checkDeath(victim, null);

        // --- DÉTECTION TUEUR ---
        Player killer = null;
        if (event instanceof EntityDamageByEntityEvent) {
            // ... (Logique detection pistolet/autre inchangée) ...
            EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
            Entity damager = entityEvent.getDamager();

            if (damager instanceof Player) killer = (Player) damager;
            else if (damager instanceof Projectile) {
                Projectile proj = (Projectile) damager;
                if (proj.getShooter() instanceof Player) killer = (Player) proj.getShooter();
            } else {
                killer = getShooterCustom(damager);
            }
        }

        if (killer != null && killer != victim) {
            int reward = 10;

            killer.sendMessage("§a+10 coins §fpour avoir tué §e" + victim.getName());
            victim.sendMessage("§cVous avez été tué par §e" + killer.getName());

            economyManager.addMoney(killer.getUniqueId(), reward);
            killstreakManager.incrementKillstreak(killer);
            levelManager.addXp(killer, 10);
            bountyManager.checkDeath(victim, killer);
            scoreboardManager.recordKill(killer, victim);

            ffaManager.startRespawnSequence(victim, killer);
        } else {
            victim.sendMessage("§cVous êtes mort.");
            ffaManager.startRespawnSequence(victim);
        }
    }

    private Player getShooterCustom(Entity damager) {
        // ... (Même code réflexion que l'étape précédente) ...
        try {
            Method getHandleMethod = damager.getClass().getMethod("getHandle");
            Object nmsEntity = getHandleMethod.invoke(damager);
            Method m = nmsEntity.getClass().getMethod("getShooter");
            Object nmsOwner = m.invoke(nmsEntity);
            if (nmsOwner != null) {
                Method getBukkitEntity = nmsOwner.getClass().getMethod("getBukkitEntity");
                return (Player) getBukkitEntity.invoke(nmsOwner);
            }
        } catch (Exception e) {}
        return null;
    }
}