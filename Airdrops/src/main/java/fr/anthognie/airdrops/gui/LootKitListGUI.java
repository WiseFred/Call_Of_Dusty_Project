package fr.anthognie.airdrops.gui;

import fr.anthognie.airdrops.managers.LootManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class LootKitListGUI {

    public static final String GUI_TITLE_PREFIX = "§cAdmin - Kits "; // "§cAdmin - Kits Normal"
    private final LootManager lootManager;

    public LootKitListGUI(LootManager lootManager) {
        this.lootManager = lootManager;
    }

    public void open(Player player, String type) {
        String title = GUI_TITLE_PREFIX + type;
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // --- Boutons de contrôle ---
        ItemStack placeholder = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta phMeta = placeholder.getItemMeta();
        phMeta.setDisplayName("§r");
        placeholder.setItemMeta(phMeta);
        for (int i = 45; i < 54; i++) inv.setItem(i, placeholder);

        inv.setItem(48, createButton(Material.EMERALD, "§aCréer un Nouveau Kit",
                List.of("§7Vous demandera le nom et la chance", "§7dans le chat.")
        ));
        inv.setItem(50, createButton(Material.ARROW, "§7Retour",
                List.of("§7Retour au choix du type")
        ));

        // --- Remplir avec les kits existants ---
        ConfigurationSection kitsSection = lootManager.getConfig().getConfigurationSection(type + ".kits");
        if (kitsSection != null) {
            int slot = 0;
            for (String key : kitsSection.getKeys(false)) {
                if (slot >= 45) break; // Limite

                int chance = kitsSection.getInt(key + ".chance");
                ItemStack item = createButton(Material.BOOK, "§e" + key,
                        List.of(
                                "§fType: §b" + type,
                                "§fChance: §b" + chance + "%",
                                "",
                                "§aClic Gauche: Éditer le contenu",
                                "§cClic Droit: Supprimer ce kit"
                        ));
                inv.setItem(slot, item);
                slot++;
            }
        }

        player.openInventory(inv);
    }

    private ItemStack createButton(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}