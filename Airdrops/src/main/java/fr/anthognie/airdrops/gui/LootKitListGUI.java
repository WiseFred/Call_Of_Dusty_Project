package fr.anthognie.airdrops.gui;

import fr.anthognie.airdrops.Main;
import fr.anthognie.airdrops.managers.LootManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LootKitListGUI {

    // Constante publique pour le Listener
    public static final String GUI_TITLE_PREFIX = "§8Kits: ";

    private final Main plugin;
    private final LootManager lootManager;

    public LootKitListGUI(Main plugin) {
        this.plugin = plugin;
        this.lootManager = plugin.getLootManager();
    }

    public void open(Player player, String type) { // type = "normal" ou "ultimate"
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE_PREFIX + type);

        // Récupération des kits depuis loot.yml
        ConfigurationSection section = lootManager.getConfig().getConfigurationSection(type + ".kits");

        if (section != null) {
            int slot = 0;
            for (String kitName : section.getKeys(false)) {
                int chance = section.getInt(kitName + ".chance");

                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("§e" + kitName);
                List<String> lore = new ArrayList<>();
                lore.add("§7Chance: §f" + chance + "%");
                lore.add("");
                lore.add("§aClic Gauche: §7Éditer le contenu");
                lore.add("§cClic Droit: §7Supprimer le kit");
                meta.setLore(lore);
                item.setItemMeta(meta);

                inv.setItem(slot++, item);
            }
        }

        // Bouton Retour
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§cRetour");
        back.setItemMeta(backMeta);
        inv.setItem(49, back);

        // Bouton Nouveau Kit
        ItemStack newKit = new ItemStack(Material.NETHER_STAR);
        ItemMeta newMeta = newKit.getItemMeta();
        newMeta.setDisplayName("§aCréer un nouveau Kit");
        newKit.setItemMeta(newMeta);
        inv.setItem(53, newKit);

        player.openInventory(inv);
    }
}