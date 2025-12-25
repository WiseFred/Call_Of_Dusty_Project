package fr.anthognie.spawn.listeners;

import fr.anthognie.spawn.Main;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SpawnListener implements Listener {

    private final Main plugin;

    public SpawnListener(Main plugin) {
        this.plugin = plugin;
    }

    // --- PROTECTION NOURRITURE ---
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (player.getWorld().getName().equals("world")) {
            event.setCancelled(true);
            event.setFoodLevel(20);
            player.setFoodLevel(20);
        }
    }

    // --- JOIN / LEAVE / WORLD CHANGE ---
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.getWorld().getName().equals("world")) return;

        teleportToSpawn(player);
        setupLobbyPlayer(player);
        plugin.getScoreboardManager().setScoreboard(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getScoreboardManager().removeScoreboard(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (event.getPlayer().getWorld().getName().equals("world")) {
            Player player = event.getPlayer();
            plugin.getScoreboardManager().setScoreboard(player);

            // CORRECTION CRITIQUE : Vérifie si le joueur vient de quitter le FFA
            if (player.hasMetadata("JustLeftFFA")) {
                player.setGameMode(GameMode.ADVENTURE);
                // On NE TOUCHE PAS à l'inventaire, il a été restauré par le FFA
            } else {
                setupLobbyPlayer(player);
            }
        } else {
            plugin.getScoreboardManager().removeScoreboard(event.getPlayer());
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (event.getRespawnLocation().getWorld().getName().equals("world")) {
            event.setRespawnLocation(getSpawnLocation());
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                setupLobbyPlayer(event.getPlayer());
            }, 5L);
        }
    }

    private void setupLobbyPlayer(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.getInventory().clear();

        ItemStack selector = new ItemStack(Material.TNT);
        ItemMeta meta = selector.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lMenu des Jeux");
            List<String> lore = new ArrayList<>();
            lore.add("§7Clic droit pour choisir un jeu");
            meta.setLore(lore);
            selector.setItemMeta(meta);
        }
        player.getInventory().setItem(4, selector);
    }

    private void teleportToSpawn(Player player) {
        player.teleport(getSpawnLocation());
    }

    private Location getSpawnLocation() {
        World world = plugin.getServer().getWorld("world");
        Location configLoc = (Location) plugin.getConfig().get("spawn");
        if (configLoc != null) return configLoc;
        return world != null ? world.getSpawnLocation() : null;
    }
}