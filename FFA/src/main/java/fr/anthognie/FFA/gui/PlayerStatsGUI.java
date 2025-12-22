package fr.anthognie.FFA.gui;

import fr.anthognie.FFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;

public class PlayerStatsGUI {

    private final Main plugin;

    public PlayerStatsGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player admin) {
        int size = 54;
        Inventory inv = Bukkit.createInventory(null, size, "§8Sélectionner un joueur");

        for (Player p : Bukkit.getOnlinePlayers()) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(p);
                meta.setDisplayName("§e" + p.getName());
                ArrayList<String> lore = new ArrayList<>();
                lore.add("§7Clique pour modifier les stats");
                meta.setLore(lore);
                head.setItemMeta(meta);
            }
            inv.addItem(head);
        }
        admin.openInventory(inv);
    }
}