package fr.anthognie.FFA.listeners;

import fr.anthognie.FFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerJoinListener implements Listener {

    private final Main plugin;

    public PlayerJoinListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 1. Punition
        if (plugin.getFfaManager().hasPendingPenalty(player)) {
            player.sendMessage("§c§lPUNITION ! §7Déconnexion en combat détectée.");
            player.sendMessage("§c-5000$ ont été retirés de votre compte.");
            plugin.getFfaManager().removePendingPenalty(player);
        }

        // 2. Anti-Spawn FFA
        if (player.getWorld().getName().equals(plugin.getFfaManager().getFFAWorldName())) {
            World lobby = Bukkit.getWorld("world");
            if (lobby != null) player.teleport(lobby.getSpawnLocation());

            player.setGameMode(GameMode.ADVENTURE);

            ItemStack[] savedInv = plugin.getEconomyManager().loadInventory(player.getUniqueId());
            if (savedInv != null) {
                player.getInventory().setContents(savedInv);
                plugin.getEconomyManager().clearInventory(player.getUniqueId());
            } else {
                player.getInventory().clear();
            }

            // AUCUN MESSAGE DE CONFIRMATION ICI
        }

        plugin.getDataManager().loadPlayer(player);
    }
}