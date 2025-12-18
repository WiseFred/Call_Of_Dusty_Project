package fr.anthognie.airdrops.gui;

import fr.anthognie.airdrops.Main;
import fr.anthognie.airdrops.managers.AirdropManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AirdropConfigGUI {

    public static final String GUI_TITLE = "§cAdmin - Gérer les Airdrops";
    private final AirdropManager airdropManager;

    public AirdropConfigGUI(Main plugin) {
        this.airdropManager = plugin.getAirdropManager();
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, GUI_TITLE);

        // --- Boutons Toggle ---
        inv.setItem(10, createToggleButton(
                airdropManager.areDropsEnabled(false),
                "§6Airdrops Normaux (10 min)",
                List.of("§7Activer/Désactiver les", "§7airdrops normaux automatiques.")
        ));

        inv.setItem(12, createToggleButton(
                airdropManager.areDropsEnabled(true),
                "§cAirdrops Ultimes (15 min)",
                List.of("§7Activer/Désactiver les", "§7airdrops ultimes automatiques.")
        ));

        // --- Boutons Force ---
        inv.setItem(19, createButton(Material.TNT, "§6Forcer un Airdrop Normal",
                List.of("§7Fait tomber un airdrop", "§7normal immédiatement.")
        ));

        inv.setItem(21, createButton(Material.BEACON, "§cForcer un Airdrop Ultime",
                List.of("§7Lance la séquence de 5s", "§7pour un airdrop ultime.")
        ));

        // --- Bouton de gestion du Loot ---
        inv.setItem(24, createButton(Material.CHEST, "§eGérer les Kits de Loot",
                List.of("§7Ouvre l'éditeur pour créer,", "§7modifier ou supprimer les kits.")
        ));

        inv.setItem(28, createButton(Material.TNT_MINECART, "§4§lRESET AIRDROPS",
                List.of("§cAttention ! Supprime IMMÉDIATEMENT", "§ctous les coffres d'airdrops", "§cactifs sur la map.")
        ));

        // --- Bouton de retour ---
        inv.setItem(40, createButton(Material.ARROW, "§7Retour",
                List.of("§7Retour au tableau de bord principal")));

        player.openInventory(inv);
    }

    private ItemStack createToggleButton(boolean isActive, String name, List<String> lore) {
        Material material = isActive ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK;
        String status = isActive ? "§aActivé" : "§cDésactivé";
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> finalLore = new ArrayList<>(lore);
        finalLore.add("");
        finalLore.add("§fÉtat: " + status);
        finalLore.add("§eClic pour changer");
        meta.setLore(finalLore);
        item.setItemMeta(meta);
        return item;
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