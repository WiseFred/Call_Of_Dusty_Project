package fr.anthognie.FFA.listeners;

import fr.anthognie.Core.managers.BuildModeManager;
import fr.anthognie.Core.managers.EconomyManager;
import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.game.FFAManager;
import fr.anthognie.FFA.managers.KillstreakManager;
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

    private final FFAManager ffaManager;
    private final EconomyManager economyManager;
    private final KillstreakManager killstreakManager;
    private final BuildModeManager buildModeManager;
    private final Main plugin;

    public PlayerDamageListener(Main plugin) {
        this.plugin = plugin;
        this.ffaManager = plugin.getFfaManager();
        this.economyManager = plugin.getEconomyManager();
        this.killstreakManager = plugin.getKillstreakManager();
        this.buildModeManager = plugin.getCore().getBuildModeManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.isCancelled()) return;

        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();

        if (!victim.getWorld().getName().equals(ffaManager.getFFAWorldName())) return;

        if (ffaManager.isInvincible(victim)) {
            event.setCancelled(true);
            return;
        }

        // Si le joueur ne meurt pas
        if (victim.getHealth() - event.getFinalDamage() > 0) {
            ffaManager.handlePlayerDamage(victim);
            return;
        }

        // --- MORT DU JOUEUR ---

        event.setCancelled(true);
        economyManager.clearInventory(victim.getUniqueId());
        killstreakManager.resetKillstreak(victim);

        // --- DÉTECTION TUEUR ---
        Player killer = null;

        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
            Entity damager = entityEvent.getDamager();

            if (damager instanceof Player) {
                killer = (Player) damager;
            }
            else if (damager instanceof Projectile) {
                Projectile proj = (Projectile) damager;
                if (proj.getShooter() instanceof Player) {
                    killer = (Player) proj.getShooter();
                }
            }
            else {
                // Modded Projectile (CGM)
                killer = getShooterCustom(damager);
            }
        }

        if (killer != null && killer != victim) {
            // Récompense standard
            int reward = 10;

            killer.sendMessage("§a+10 coins §fpour avoir tué §e" + victim.getName());
            victim.sendMessage("§cVous avez été tué par §e" + killer.getName());

            economyManager.addMoney(killer.getUniqueId(), reward);
            killstreakManager.incrementKillstreak(killer);

            ffaManager.startRespawnSequence(victim, killer);

        } else {
            victim.sendMessage("§cVous êtes mort.");
            ffaManager.startRespawnSequence(victim);
        }
    }

    private Player getShooterCustom(Entity damager) {
        try {
            Method getHandleMethod = damager.getClass().getMethod("getHandle");
            Object nmsEntity = getHandleMethod.invoke(damager);

            Object nmsOwner = null;
            try {
                // Méthode spécifique CGM qu'on a trouvée dans tes logs
                Method m = nmsEntity.getClass().getMethod("getShooter");
                nmsOwner = m.invoke(nmsEntity);
            } catch (NoSuchMethodException e) {
                // Fallback standard au cas où
                try {
                    Method m = nmsEntity.getClass().getMethod("getOwner");
                    nmsOwner = m.invoke(nmsEntity);
                } catch (NoSuchMethodException e2) {
                    // Ignoré
                }
            }

            if (nmsOwner != null) {
                Method getBukkitEntity = nmsOwner.getClass().getMethod("getBukkitEntity");
                Object bukkitOwner = getBukkitEntity.invoke(nmsOwner);
                if (bukkitOwner instanceof Player) {
                    return (Player) bukkitOwner;
                }
            }
        } catch (Exception e) {
            // Ignorer silencieusement
        }
        return null;
    }
}