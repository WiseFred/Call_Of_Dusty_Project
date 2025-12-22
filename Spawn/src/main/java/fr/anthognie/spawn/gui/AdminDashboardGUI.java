package fr.anthognie.spawn.gui;

import fr.anthognie.Core.utils.ItemBuilder;
import fr.anthognie.spawn.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class AdminDashboardGUI {

    // Titre harmonisé
    public static final String GUI_TITLE = "§cAdmin §8» §eTableau de Bord";

    private final Main plugin;

    public AdminDashboardGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);

        // Bouton Spawn
        inv.setItem(11, ItemBuilder.create(Material.COMPASS, "§aConfiguration Spawn",
                "§7Gérer le point de spawn,", "§7le lobby et la protection."));

        // Bouton FFA
        inv.setItem(13, ItemBuilder.create(Material.IRON_SWORD, "§cConfiguration FFA",
                "§7Gérer le mode FFA,", "§7le build mode et les dégâts."));

        // Bouton Airdrops
        inv.setItem(15, ItemBuilder.create(Material.CHEST, "§6Configuration Airdrops",
                "§7Gérer les largages,", "§7les timers et les loots."));

        // Bouton Fermer
        inv.setItem(22, ItemBuilder.create(Material.BARRIER, "§cFermer"));

        player.openInventory(inv);
    }
}