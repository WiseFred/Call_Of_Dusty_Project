package fr.anthognie.airdrops.gui;

import fr.anthognie.Core.utils.ItemBuilder;
import fr.anthognie.airdrops.Main;
import fr.anthognie.airdrops.managers.AirdropManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AirdropConfigGUI {

    // CORRECTION : Renommé de TITLE à GUI_TITLE pour correspondre au Listener
    public static final String GUI_TITLE = "§8Gestion des Airdrops";
    private final Main plugin;

    public AirdropConfigGUI(Main plugin) {
        this.plugin = plugin;
    }

    // Constructeur sans argument pour compatibilité si nécessaire
    public AirdropConfigGUI() {
        this.plugin = Main.getInstance();
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);
        AirdropManager manager = plugin.getAirdropManager();

        boolean normalOn = manager.areDropsEnabled(false);
        boolean ultimateOn = manager.areDropsEnabled(true);

        // État Normal
        inv.setItem(10, ItemBuilder.create(Material.CHEST,
                "§eAirdrops Normaux",
                "§7État: " + (normalOn ? "§aACTIVÉ" : "§cDÉSACTIVÉ"),
                "§7Clic pour basculer"
        ));

        // État Ultime
        inv.setItem(12, ItemBuilder.create(Material.ENDER_CHEST,
                "§5Airdrops Ultimes",
                "§7État: " + (ultimateOn ? "§aACTIVÉ" : "§cDÉSACTIVÉ"),
                "§7Clic pour basculer"
        ));

        // Forcer Drop
        inv.setItem(14, ItemBuilder.create(Material.TRIPWIRE_HOOK,
                "§6Forcer un Airdrop",
                "§7Clic Gauche: §eNormal",
                "§7Clic Droit: §5Ultime"
        ));

        // Gérer les Loots
        inv.setItem(16, ItemBuilder.create(Material.BOOK,
                "§bGérer les Loots",
                "§7Modifier les kits et items",
                "§7contenus dans les airdrops"
        ));

        // Vitres
        ItemStack filler = ItemBuilder.create(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) inv.setItem(i, filler);
        }

        player.openInventory(inv);
    }
}