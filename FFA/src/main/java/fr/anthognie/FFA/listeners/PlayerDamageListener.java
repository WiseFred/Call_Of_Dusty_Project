package fr.anthognie.FFA.listeners;

import fr.anthognie.Core.managers.BuildModeManager;
import fr.anthognie.FFA.Main;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerDamageListener implements Listener {

    private final Main plugin;
    private final BuildModeManager buildModeManager;

    public PlayerDamageListener(Main plugin) {
        this.plugin = plugin;
        // On récupère le BuildModeManager depuis le Core pour gérer les protections
        this.buildModeManager = fr.anthognie.Core.Main.getInstance().getBuildModeManager();
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        // On vérifie si on est dans le monde FFA
        if (player.getWorld().getName().equals(plugin.getFfaManager().getFFAWorldName())) {

            // Si le joueur est invincible (protection de spawn)
            if (plugin.getFfaManager().isInvincible(player)) {
                event.setCancelled(true);
                return;
            }

            // Si le coup est fatal
            if (player.getHealth() - event.getFinalDamage() <= 0) {
                event.setCancelled(true); // On annule la mort Vanilla

                // Détection du tueur
                Player killer = null;
                if (event instanceof EntityDamageByEntityEvent) {
                    EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
                    if (entityEvent.getDamager() instanceof Player) {
                        killer = (Player) entityEvent.getDamager();
                    } else if (entityEvent.getDamager() instanceof Projectile && ((Projectile) entityEvent.getDamager()).getShooter() instanceof Player) {
                        killer = (Player) ((Projectile) entityEvent.getDamager()).getShooter();
                    }
                }

                // Lancement de la séquence de mort custom
                plugin.getFfaManager().startRespawnSequence(player, killer);
            } else {
                // S'il survit -> On déclenche le délai de régénération CoD
                plugin.getFfaManager().handlePlayerDamage(player);
            }
        }
    }
}