package fr.anthognie.FFA.listeners;

import fr.anthognie.Core.managers.BuildModeManager;
import fr.anthognie.FFA.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class FFAProtectionListener implements Listener {

    private final Main plugin;
    private final BuildModeManager buildModeManager;

    public FFAProtectionListener(Main plugin) {
        this.plugin = plugin;
        this.buildModeManager = fr.anthognie.Core.Main.getInstance().getBuildModeManager();
    }

    // 1. EMPÊCHER LA FAIM DE DESCENDRE (Correction Saturation)
    @EventHandler
    public void onHungerLoss(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            // Vérifie bien que le monde est "ffa"
            if (player.getWorld().getName().equals(plugin.getFfaManager().getFFAWorldName())) {
                event.setCancelled(true);
                player.setFoodLevel(20);
                player.setSaturation(20f); // CRITIQUE : Empêche la barre de trembler
            }
        }
    }

    // 2. EMPÊCHER LA RÉGÉNÉRATION NATURELLE
    @EventHandler
    public void onVanillaRegen(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (player.getWorld().getName().equals(plugin.getFfaManager().getFFAWorldName())) {
                // On annule si ça vient de la nourriture (SATIATED) ou regen naturelle (REGEN)
                if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED
                        || event.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN) {
                    event.setCancelled(true);
                }
            }
        }
    }

    // --- AUTRES PROTECTIONS ---
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getName().equals(plugin.getFfaManager().getFFAWorldName())
                && !buildModeManager.isInBuildMode(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getName().equals(plugin.getFfaManager().getFFAWorldName())
                && !buildModeManager.isInBuildMode(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getName().equals(plugin.getFfaManager().getFFAWorldName())
                && !buildModeManager.isInBuildMode(player)) {
            event.setCancelled(true);
        }
    }
}