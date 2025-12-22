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
            sender.sendMessage("§cSeul un joueur peut faire ça.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("ffa.admin")) {
            player.sendMessage("§cVous n'avez pas la permission.");
            return true;
        }

        ShopGUI shopGUI = plugin.getShopGUI();
        // Ouvre en mode Admin (true)
        shopGUI.open(player, true);

        player.sendMessage("§eMode Édition du Shop activé.");
        return true;
    }
}