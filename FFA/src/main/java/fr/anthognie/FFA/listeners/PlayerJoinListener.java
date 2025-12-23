package fr.anthognie.FFA.listeners;

import fr.anthognie.FFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final Main plugin;

    public PlayerJoinListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 1. GESTION PUNITION ANTI-DÉCO
        if (plugin.getFfaManager().hasPendingPenalty(player)) {
            player.sendMessage("§c§lPUNITION ! §7Déconnexion en combat détectée.");
            player.sendMessage("§c-5000$ ont été retirés de votre compte.");
            plugin.getFfaManager().removePendingPenalty(player);
        }

        // 2. ANTI-SPAWN EN FFA (Force le retour au Lobby)
        // Si le joueur se connecte alors qu'il est dans le monde FFA, on le renvoie au spawn global
        if (player.getWorld().getName().equals(plugin.getFfaManager().getFFAWorldName())) {
            World lobby = Bukkit.getWorld("world"); // Assure-toi que "world" est bien le nom de ton monde lobby
            if (lobby != null) {
                player.teleport(lobby.getSpawnLocation());
            }
            player.setGameMode(GameMode.ADVENTURE);
            player.getInventory().clear();
            player.sendMessage("§eVous avez été renvoyé au Lobby.");
        }

        // Chargement des données FFA
        plugin.getDataManager().loadPlayer(player);
    }
}