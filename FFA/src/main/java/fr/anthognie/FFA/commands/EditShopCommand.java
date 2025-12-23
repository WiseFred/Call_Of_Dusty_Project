package fr.anthognie.FFA.commands;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.gui.ShopGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EditShopCommand implements CommandExecutor {

    private final Main plugin;

    public EditShopCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSeul un joueur peut exécuter cette commande.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ffa.admin.editshop")) {
            player.sendMessage("§cVous n'avez pas la permission.");
            return true;
        }

        ShopGUI shopGUI = plugin.getShopGUI();
        shopGUI.open(player, true); // Ouvre en mode ADMIN (true)
        player.sendMessage("§eMode édition du shop activé.");
        player.sendMessage("§7- Clic inventaire: Ajouter item");
        player.sendMessage("§7- Clic gauche: Déplacer");
        player.sendMessage("§7- Clic droit: Supprimer");

        return true;
    }
}