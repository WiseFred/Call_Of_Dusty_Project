package fr.anthognie.spawn.gui;

import fr.anthognie.Core.utils.ItemBuilder;
import fr.anthognie.spawn.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SpawnConfigGUI {

    public static final String GUI_TITLE = "§cAdmin §8» §aSpawn";

    private final Main plugin;

    public SpawnConfigGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, GUI_TITLE);
        FileConfiguration config = plugin.getConfig();

        // 1. Définir le Spawn
        inv.setItem(11, ItemBuilder.create(Material.BEACON, "§aDéfinir le Spawn",
                "§7Définit le point de réapparition", "§7principal à votre position."));

        // 2. Définir le Lobby (Zone d'attente)
        inv.setItem(13, ItemBuilder.create(Material.OAK_SIGN, "§eDéfinir le Lobby",
                "§7Définit la zone d'attente", "§7avant le jeu."));

        // 3. Téléportation Vide (Toggle)
        boolean voidTp = config.getBoolean("void-tp.enabled");
        Material voidMat = voidTp ? Material.LIME_DYE : Material.GRAY_DYE;
        String voidStatus = voidTp ? "§aActivé" : "§cDésactivé";

        inv.setItem(15, ItemBuilder.create(voidMat, "§bTP Vide: " + voidStatus,
                "§7Téléporte les joueurs au spawn", "§7s'ils tombent dans le vide."));

        // Bouton Retour
        inv.setItem(40, ItemBuilder.create(Material.ARROW, "§7Retour", "§7Retour au menu principal"));

        player.openInventory(inv);
    }
}