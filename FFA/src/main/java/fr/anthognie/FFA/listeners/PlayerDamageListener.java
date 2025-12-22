package fr.anthognie.FFA.listeners;

import fr.anthognie.FFA.Main;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerDamageListener implements Listener {

    private final Main plugin;

    public PlayerDamageListener(Main plugin) {
        this.plugin = plugin;
    }

    // Priorité la plus haute pour être sûr de passer avant les autres plugins
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        // On vérifie qu'on est bien dans le monde FFA
        if (!player.getWorld().getName().equals(plugin.getFfaManager().getFFAWorldName())) return;

        // Si le joueur est invincible ou déjà spectateur, on annule tout dégât
        if (plugin.getFfaManager().isInvincible(player) || player.getGameMode() == GameMode.SPECTATOR) {
            event.setCancelled(true);
            return;
        }

        // --- GESTION DE LA MORT PERSONNALISÉE ---
        // Si le coup reçu est fatal (Vie actuelle - Dégâts <= 0)
        if (player.getHealth() - event.getFinalDamage() <= 0) {
            event.setCancelled(true); // ON ANNULE LA VRAIE MORT MINECRAFT

            // On cherche qui a tué (si c'est un joueur)
            Player killer = null;
            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent) event;
                if (edbe.getDamager() instanceof Player) {
                    killer = (Player) edbe.getDamager();
                }
            }

            // On lance notre séquence de mort (spectateur, titre, respawn...)
            plugin.getFfaManager().startRespawnSequence(player, killer);
            return; // On arrête là pour ne pas jouer les sons de blessure
        }

        // --- GESTION DES DÉGÂTS NORMAUX (NON MORTELS) ---

        // Particules de "sang" (Redstone Block Crack)
        player.getWorld().spawnParticle(Particle.BLOCK_CRACK, player.getLocation().add(0, 1, 0),
                10, 0.2, 0.2, 0.2, 0.1, Material.REDSTONE_BLOCK.createBlockData());

        // On signale au Manager que le joueur a été touché (pour reset le timer de régénération)
        plugin.getFfaManager().handlePlayerDamage(player);

        // Petit son pour l'attaquant pour confirmer la touche (Hitmarker sonore)
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent) event;
            if (edbe.getDamager() instanceof Player) {
                Player attacker = (Player) edbe.getDamager();
                attacker.playSound(attacker.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 0.5f);
            }
        }
    }
}