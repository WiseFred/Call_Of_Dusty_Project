package fr.anthognie.airdrops.gui;

import fr.anthognie.Core.utils.ItemBuilder;
import fr.anthognie.airdrops.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class LootBrowserGUI {

    public static final String GUI_TITLE = "§cAdmin §8» §6Airdrops (Loot)";

    private final Main plugin;

    public LootBrowserGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);

        inv.setItem(11, ItemBuilder.create(Material.CHEST, "§eLoot Normal",
                "§7Gérer les kits des", "§7airdrops normaux (10m)."));

        inv.setItem(15, ItemBuilder.create(Material.ENDER_CHEST, "§6Loot Ultime",
                "§7Gérer les kits des", "§7airdrops ultimes (15m)."));

        // Bouton Retour (Revient au menu Airdrop)
        inv.setItem(22, ItemBuilder.create(Material.ARROW, "§7Retour", "§7Retour à la config Airdrop"));

        player.openInventory(inv);
    }
}