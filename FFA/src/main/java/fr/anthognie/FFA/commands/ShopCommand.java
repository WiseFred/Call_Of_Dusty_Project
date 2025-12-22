package fr.anthognie.FFA.commands;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.game.FFAManager;
import fr.anthognie.FFA.gui.ShopGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {

    private Main plugin;
    private FFAManager ffaManager;
    private ShopGUI shopGUI;

    public ShopCommand(Main plugin) {
        this.plugin = plugin;
        this.ffaManager = plugin.getFfaManager();
        this.shopGUI = plugin.getShopGUI();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.getWorld().getName().equals(ffaManager.getFFAWorldName())) {
            player.sendMessage("§cLe shop n'est disponible que dans l'arène FFA.");
            return true;
        }

        shopGUI.open(player);
        return true;
    }
}