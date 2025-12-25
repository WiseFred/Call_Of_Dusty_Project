package fr.anthognie.spawn.gui;

import fr.anthognie.spawn.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GameSelectorGUI {

    public static final String GUI_TITLE = "§8Sélecteur de Jeux";
    public static final int FFA_SLOT = 13;

    private final Main plugin;

    public GameSelectorGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);

        // Récupération du nombre de joueurs en FFA
        int ffaPlayers = 0;
        World ffaWorld = Bukkit.getWorld("ffa"); // Assure-toi que ton monde s'appelle "ffa"
        if (ffaWorld != null) {
            ffaPlayers = ffaWorld.getPlayers().size();
        }

        // ITEM TNT pour le FFA
        ItemStack ffaItem = new ItemStack(Material.TNT);
        ItemMeta ffaMeta = ffaItem.getItemMeta();
        if (ffaMeta != null) {
            ffaMeta.setDisplayName("§c§lFFA (Call of Dusty)");
            List<String> lore = new ArrayList<>();
            lore.add("§7Cliquez pour rejoindre l'arène.");
            lore.add("");
            lore.add("§7Joueurs : §b" + ffaPlayers + " en jeu"); // Ajout du compteur
            lore.add("");
            lore.add("§e>> Jouer !");
            ffaMeta.setLore(lore);
            ffaItem.setItemMeta(ffaMeta);
        }
        inv.setItem(FFA_SLOT, ffaItem);

        player.openInventory(inv);
    }

    public String getTitle() { return GUI_TITLE; }
    public int getFfaSlot() { return FFA_SLOT; }
}