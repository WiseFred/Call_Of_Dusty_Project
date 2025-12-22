package fr.anthognie.airdrops.commands;

import fr.anthognie.airdrops.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AirdropConfigCommand implements CommandExecutor {

    private final Main plugin;

    public AirdropConfigCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSeul un joueur peut utiliser cette commande.");
            return true;
        }

        Player player = (Player) sender;

        // Vérification Permission
        if (!player.hasPermission("callofdusty.admin") && !player.isOp()) {
            player.sendMessage("§cVous n'avez pas la permission (callofdusty.admin).");
            return true;
        }

        try {
            if (plugin.getAirdropConfigGUI() == null) {
                player.sendMessage("§cErreur: Le GUI n'est pas initialisé !");
                return true;
            }
            plugin.getAirdropConfigGUI().open(player);
        } catch (Exception e) {
            player.sendMessage("§cUne erreur est survenue lors de l'ouverture du menu.");
            e.printStackTrace();
        }
        return true;
    }
}