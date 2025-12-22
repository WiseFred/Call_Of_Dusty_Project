package fr.anthognie.airdrops.gui;

import fr.anthognie.Core.utils.ItemBuilder;
import fr.anthognie.airdrops.Main;
import fr.anthognie.airdrops.managers.AirdropManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class AirdropConfigGUI {

    public static final String GUI_TITLE = "§cAdmin §8» §6Airdrops";

    private final Main plugin;
    private final AirdropManager airdropManager;

    public AirdropConfigGUI(Main plugin) {
        this.plugin = plugin;
        this.airdropManager = plugin.getAirdropManager();
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, GUI_TITLE);

        // --- NORMAL ---
        boolean normalEnabled = airdropManager.areDropsEnabled(false);
        String normalStatus = normalEnabled ? "§aActivés" : "§cDésactivés";
        Material normalMat = normalEnabled ? Material.LIME_DYE : Material.GRAY_DYE;

        inv.setItem(11, ItemBuilder.create(normalMat, "§eDrops Normaux: " + normalStatus,
                "§7Clic pour changer l'état"));

        inv.setItem(13, ItemBuilder.create(Material.CHEST, "§eForcer Drop Normal",
                "§7Fait apparaître un drop", "§7normal immédiatement."));


        // --- ULTIMATE ---
        boolean ultEnabled = airdropManager.areDropsEnabled(true);
        String ultStatus = ultEnabled ? "§aActivés" : "§cDésactivés";
        Material ultMat = ultEnabled ? Material.ORANGE_DYE : Material.GRAY_DYE;

        inv.setItem(15, ItemBuilder.create(Material.ENDER_CHEST, "§6Forcer Drop Ultime",
                "§7Fait apparaître un drop", "§7ultime immédiatement."));

        inv.setItem(17, ItemBuilder.create(ultMat, "§6Drops Ultimes: " + ultStatus,
                "§7Clic pour changer l'état"));


        // --- GESTION LOOTS ---
        inv.setItem(31, ItemBuilder.create(Material.WRITABLE_BOOK, "§aGérer les Loots",
                "§7Modifier le contenu", "§7des coffres."));

        // Bouton Retour
        inv.setItem(40, ItemBuilder.create(Material.ARROW, "§7Retour", "§7Retour au menu principal"));

        player.openInventory(inv);
    }
}