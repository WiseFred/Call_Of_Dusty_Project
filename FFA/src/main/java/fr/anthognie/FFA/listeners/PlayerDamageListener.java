package fr.anthognie.FFA.listeners;

import fr.anthognie.FFA.Main;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
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
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!event.getEntity().getWorld().getName().equals(plugin.getFfaManager().getFFAWorldName())) return;

        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();

            // 1. EFFET DE SANG (Particules Redstone Block)
            victim.getWorld().spawnParticle(Particle.BLOCK_CRACK, victim.getLocation().add(0, 1, 0),
                    10, 0.2, 0.2, 0.2, 0.1, Material.REDSTONE_BLOCK.createBlockData());

            // Si l'attaquant est un joueur
            if (event.getDamager() instanceof Player) {
                Player attacker = (Player) event.getDamager();

                // Petit son de "Hit" pour confirmer qu'on a touché
                attacker.playSound(attacker.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 0.5f);

                // Gestion Invincibilité
                if (plugin.getFfaManager().isInvincible(victim)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    // 2. EFFET BATTEMENT DE COEUR (Santé Basse)
    @EventHandler
    public void onHealthCheck(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            // Si la vie passe en dessous de 6PV (3 coeurs)
            if (p.getHealth() - event.getFinalDamage() <= 6.0 && p.getHealth() - event.getFinalDamage() > 0) {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1f, 0.5f); // BOUM... BOUM...
                p.sendTitle("", "§4❤ DANGER ❤", 0, 10, 5);
            }
        }
    }
}