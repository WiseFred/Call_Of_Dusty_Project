package fr.anthognie.airdrops.gui;

import fr.anthognie.airdrops.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LootBrowserGUI {

    public static final String TITLE_NORMAL = "§8Loot: §eNormal";
    public static final String TITLE_ULTIMATE = "§8Loot: §6Ultime";

    private final Main plugin;

    public LootBrowserGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, boolean isUltimate) {
        String title = isUltimate ? TITLE_ULTIMATE : TITLE_NORMAL;
        Inventory inv = Bukkit.createInventory(null, 54, title);

        String typePath = isUltimate ? "ultimate" : "normal";
        ConfigurationSection section = plugin.getLootManager().getLootConfig().getConfigurationSection("loots." + typePath);

        if (section != null) {
            for (String key : section.getKeys(false)) {
                String path = "loots." + typePath + "." + key;
                ItemStack item = plugin.getLootManager().getLootConfig().getItemStack(path + ".item");
                int chance = plugin.getLootManager().getLootConfig().getInt(path + ".chance");
                int min = plugin.getLootManager().getLootConfig().getInt(path + ".min");
                int max = plugin.getLootManager().getLootConfig().getInt(path + ".max");

                if (item != null) {
                    ItemStack display = item.clone();
                    ItemMeta meta = display.getItemMeta();
                    if (meta != null) {
                        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                        lore.add("§8§m----------------");
                        lore.add("§7Chance: §e" + chance + "%");
                        lore.add("§7Quantité: §f" + min + "-" + max);
                        lore.add("§8§m----------------");
                        lore.add("§c[Clic Droit] Supprimer");
                        meta.setLore(lore);
                        display.setItemMeta(meta);
                    }
                    inv.addItem(display);
                }
            }
        }

        // Bouton Ajouter
        ItemStack addBtn = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta addMeta = addBtn.getItemMeta();
        addMeta.setDisplayName("§a§l+ Ajouter un Item");
        List<String> addLore = new ArrayList<>();
        addLore.add("§7Prenez un item dans votre main");
        addLore.add("§7et cliquez ici pour l'ajouter");
        addMeta.setLore(addLore);
        addBtn.setItemMeta(addMeta);
        inv.setItem(49, addBtn); // Slot du bas milieu (un peu décalé pour 54 slots)

        // Bouton Retour
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§cRetour");
        back.setItemMeta(backMeta);
        inv.setItem(45, back);

        player.openInventory(inv);
    }
}