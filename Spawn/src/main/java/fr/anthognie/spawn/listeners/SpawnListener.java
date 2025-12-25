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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player && event.getEntity().getWorld().getName().equals("world")) {
            event.setCancelled(true);
            event.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.getWorld().getName().equals("world")) return;

        teleportToSpawn(player);
        // Au join, on setup le lobby, SAUF si le joueur avait quitté en plein FFA (géré par FFA JoinListener)
        // Mais par sécurité ici on setup.
        setupLobbyPlayer(player);
        plugin.getScoreboardManager().setScoreboard(player);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        // Si on arrive au Spawn ("world")
        if (player.getWorld().getName().equals("world")) {
            plugin.getScoreboardManager().setScoreboard(player);

            // LOGIQUE INVENTAIRE CRITIQUE :
            // Si on vient du monde "ffa", on ne touche à rien ! Le FFAManager a déjà rendu le stuff.
            if (event.getFrom().getName().equals("ffa")) {
                player.setGameMode(GameMode.ADVENTURE);
                // On s'arrête là. Pas de clear, pas de TNT.
            } else {
                // Sinon (vient du Nether, de l'End, d'un autre jeu), on met le stuff Lobby
                setupLobbyPlayer(player);
            }
        } else {
            plugin.getScoreboardManager().removeScoreboard(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getScoreboardManager().removeScoreboard(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (event.getRespawnLocation().getWorld().getName().equals("world")) {
            event.setRespawnLocation(getSpawnLocation());
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> setupLobbyPlayer(event.getPlayer()), 5L);
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