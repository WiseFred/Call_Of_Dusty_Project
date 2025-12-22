package fr.anthognie.FFA.listeners;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.gui.PlayerStatsEditorGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class StatsListener implements Listener {

    private final Main plugin;

    public StatsListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSelectPlayer(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§8Sélectionner un joueur")) return;
        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() != Material.PLAYER_HEAD) return;

        String playerName = event.getCurrentItem().getItemMeta().getDisplayName().substring(2);
        Player target = Bukkit.getPlayer(playerName);

        if (target != null) {
            PlayerStatsEditorGUI.open((Player) event.getWhoClicked(), target);
        } else {
            event.getWhoClicked().sendMessage("§cJoueur introuvable.");
        }
    }

    @EventHandler
    public void onEditStats(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith("§8Stats: ")) return;
        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

        Player admin = (Player) event.getWhoClicked();
        String targetName = event.getView().getTitle().replace("§8Stats: ", "");
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            admin.closeInventory();
            return;
        }

        Material mat = event.getCurrentItem().getType();

        if (mat == Material.IRON_SWORD) {
            plugin.getKillstreakManager().resetKills(target); // Assure-toi que cette méthode existe
            admin.sendMessage("§aKills reset.");
        }
        else if (mat == Material.SKELETON_SKULL) {
            plugin.getScoreboardManager().resetDeaths(target);
            admin.sendMessage("§aMorts reset.");
        }
        else if (mat == Material.TNT) {
            plugin.getKillstreakManager().resetKills(target);
            plugin.getScoreboardManager().resetDeaths(target);
            plugin.getEconomyManager().setMoney(target.getUniqueId(), 0);
            admin.sendMessage("§cTout reset pour " + target.getName());
        }

        plugin.getScoreboardManager().updateBoard(target);
        admin.closeInventory();
    }
}