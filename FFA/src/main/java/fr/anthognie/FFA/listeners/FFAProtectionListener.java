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
        // On récupère le BuildModeManager via l'instance statique du Core
        this.buildModeManager = fr.anthognie.Core.Main.getInstance().getBuildModeManager();
    }

    // 1. EMPÊCHER LA FAIM DE DESCENDRE (Barre toujours pleine)
    @EventHandler
    public void onHungerLoss(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (player.getWorld().getName().equals("ffa")) { // Vérifie le nom de ton monde
                event.setCancelled(true);
                player.setFoodLevel(20);
            }
        }
    }

    // 2. EMPÊCHER LA RÉGÉNÉRATION NATURELLE (Vanilla)
    // C'est CRUCIAL : Si la faim est à 20, Minecraft va essayer de soigner le joueur.
    // On annule ça pour que SEUL ton système "Call of Duty" (FFAManager) soigne le joueur.
    @EventHandler
    public void onVanillaRegen(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (player.getWorld().getName().equals("ffa")) {

                // Si la raison du soin est la nourriture (SATIATED) ou la régénération naturelle (REGEN)
                if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED
                        || event.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getName().equals("ffa") && !buildModeManager.isInBuildMode(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getName().equals("ffa") && !buildModeManager.isInBuildMode(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        // On empêche de jeter des items en FFA pour ne pas perdre son arme par erreur
        if (player.getWorld().getName().equals("ffa") && !buildModeManager.isInBuildMode(player)) {
            event.setCancelled(true);
        }
    }
}