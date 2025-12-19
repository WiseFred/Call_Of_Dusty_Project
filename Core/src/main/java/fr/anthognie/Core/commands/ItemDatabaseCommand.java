package fr.anthognie.Core.commands;

import fr.anthognie.Core.Main;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemDatabaseCommand implements CommandExecutor {

    private final Main plugin;

    public ItemDatabaseCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSeul un joueur peut utiliser cette commande.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.isOp()) {
            player.sendMessage("§cVous n'avez pas la permission.");
            return true;
        }

        // Cas 1 : Commande "/itemdb add" -> Ajout rapide
        if (args.length > 0 && args[0].equalsIgnoreCase("add")) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            if (itemInHand == null || itemInHand.getType() == Material.AIR) {
                player.sendMessage("§cErreur : Vous devez tenir l'item à sauvegarder dans votre main principale !");
                return true;
            }

            // On lance directement la procédure de sauvegarde (chat)
            // Plus besoin de passer par le GUI
            plugin.getItemDatabaseChatListener().startSession(player, itemInHand.clone());

            player.sendMessage("§aItem pris en compte ! §e" + itemInHand.getType().toString());
            player.sendMessage("§aEntrez maintenant le chemin de sauvegarde dans le chat (ex: kits.ffa.pistol) :");
            return true;
        }

        // Cas 2 : Commande "/itemdb" (sans arguments) -> Ouvre le menu de consultation
        plugin.getItemDatabaseGUI().open(player, 1);
        return true;
    }
}