package fr.anthognie.FFA.listeners;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.game.FFAManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class PlayerRegenListener implements Listener {

    private final FFAManager ffaManager;

    public PlayerRegenListener(Main plugin) {
        this.ffaManager = plugin.getFfaManager();
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            // Si on est dans le monde FFA
            if (player.getWorld().getName().equals(ffaManager.getFFAWorldName())) {
                event.setCancelled(true); // Empêche le changement
                player.setFoodLevel(20);  // Nourriture max
                player.setSaturation(20f); // Saturation max (CRITIQUE : Empêche la barre de descendre visuellement)
                player.setExhaustion(0f); // Empêche l'épuisement par la course
            }
        }
    }

    @EventHandler
    public void onHealthRegen(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (player.getWorld().getName().equals(ffaManager.getFFAWorldName())) {
                // On bloque la régénération naturelle (C'est ton script CoD qui gère la vie)
                if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED
                        || event.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN) {
                    event.setCancelled(true);
                }
            }
        }
    }
}