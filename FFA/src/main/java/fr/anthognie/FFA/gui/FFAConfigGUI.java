package fr.anthognie.FFA.gui;

import fr.anthognie.Core.utils.ItemBuilder;
import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class FFAConfigGUI {

    public static final String GUI_TITLE = "§cAdmin §8» §cFFA";

    private final Main plugin;
    private final ConfigManager configManager;

    public FFAConfigGUI(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getFfaConfigManager();
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, GUI_TITLE);
        FileConfiguration config = configManager.getConfig();

        // 1. Spawn Protection (Invincibilité)
        boolean spawnProt = config.getBoolean("spawn-protection.enabled");
        Material protMat = spawnProt ? Material.SHIELD : Material.GRAY_DYE;
        String protStatus = spawnProt ? "§aActivée" : "§cDésactivée";

        inv.setItem(11, ItemBuilder.create(protMat, "§6Protection Spawn: " + protStatus,
                "§7Empêche les dégâts", "§7au spawn."));

        // 2. Build Mode
        boolean build = config.getBoolean("build-mode.enabled");
        Material buildMat = build ? Material.GRASS_BLOCK : Material.RED_TERRACOTTA;
        String buildStatus = build ? "§aAutorisé" : "§cInterdit";

        inv.setItem(13, ItemBuilder.create(buildMat, "§eBuild Mode: " + buildStatus,
                "§7Autorise la pose/casse", "§7de blocs."));

        // 3. Scoreboard
        boolean board = config.getBoolean("scoreboard.enabled");
        Material boardMat = board ? Material.ITEM_FRAME : Material.BARRIER;
        String boardStatus = board ? "§aVisible" : "§cCaché";

        inv.setItem(15, ItemBuilder.create(boardMat, "§bScoreboard: " + boardStatus,
                "§7Affiche les infos", "§7à droite de l'écran."));

        // Bouton Retour
        inv.setItem(40, ItemBuilder.create(Material.ARROW, "§7Retour", "§7Retour au menu principal"));

        player.openInventory(inv);
    }
}