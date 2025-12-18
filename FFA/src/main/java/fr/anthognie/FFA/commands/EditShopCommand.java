package fr.anthognie.FFA.commands;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.gui.ShopGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EditShopCommand implements CommandExecutor {

    private final ShopGUI shopGUI;

    public EditShopCommand(Main plugin) {
        this.shopGUI = plugin.getShopGUI();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs.");
            return true;
        }

        Player player = (Player) sender;

        shopGUI.open(player, true);

        player.sendMessage("§eVous êtes en mode édition du shop.");
        player.sendMessage("§f- §aGlissez-déposez un item§f pour l'ajouter/modifier.");
        player.sendMessage("§f- §cClic droit sur un item§f pour le supprimer.");

        return true;
    }
}