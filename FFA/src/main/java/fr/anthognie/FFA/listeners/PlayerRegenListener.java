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
            if (player.getWorld().getName().equals(ffaManager.getFFAWorldName())) {
                event.setCancelled(true);
                player.setFoodLevel(20);
                player.setSaturation(20f);  // Empêche la barre de bouger visuellement
                player.setExhaustion(0f);   // Empêche la consommation due à la course
            }
        }
    }

    @EventHandler
    public void onHealthRegen(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (player.getWorld().getName().equals(ffaManager.getFFAWorldName())) {
                // On annule la regen naturelle pour laisser le système CoD gérer
                if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED
                        || event.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN) {
                    event.setCancelled(true);
                }
            }
        }
    }
}