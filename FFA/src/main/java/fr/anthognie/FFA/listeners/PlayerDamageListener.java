package fr.anthognie.FFA.listeners;

import fr.anthognie.FFA.Main;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerDamageListener implements Listener {

    private final Main plugin;

    public PlayerDamageListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();

        // 1. Vérif Monde FFA
        if (!victim.getWorld().getName().equals(plugin.getFfaManager().getFFAWorldName())) {
            return;
        }

        // 2. Invincibilité (Spawn Protection)
        if (plugin.getFfaManager().isInvincible(victim)) {
            event.setCancelled(true);
            return;
        }

        // 3. Gestion de la Mort "Custom" (Pour éviter l'écran de mort)
        // On vérifie si ce coup va tuer le joueur
        if (victim.getHealth() - event.getFinalDamage() <= 0) {
            event.setCancelled(true); // ON ANNULE LA MORT VANILLA

            Player killer = null;

            // On essaie de trouver le tueur
            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
                if (subEvent.getDamager() instanceof Player) {
                    killer = (Player) subEvent.getDamager();
                } else if (subEvent.getDamager() instanceof Projectile) {
                    Projectile proj = (Projectile) subEvent.getDamager();
                    if (proj.getShooter() instanceof Player) {
                        killer = (Player) proj.getShooter();
                    }
                }
            }

            // On lance la séquence de respawn custom
            plugin.getFfaManager().startRespawnSequence(victim, killer);
            return;
        }

        // 4. Si pas mort, on lance la regen (Style CoD)
        plugin.getFfaManager().handlePlayerDamage(victim);
    }
}