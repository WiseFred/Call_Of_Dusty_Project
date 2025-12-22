package fr.anthognie.FFA.commands;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.managers.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AddSpawnCommand implements CommandExecutor {

    private final Main plugin;
    private final ConfigManager configManager;

    public AddSpawnCommand(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getFfaConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSeul un joueur peut exécuter cette commande.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ffa.admin")) {
            player.sendMessage("§cVous n'avez pas la permission.");
            return true;
        }

        // Utilise la méthode du ConfigManager qui écrit dans ffa.yml
        configManager.addSpawn(player.getLocation());

        player.sendMessage("§a§lFFA §8» §fPoint de spawn ajouté avec succès dans §effa.yml §f!");
        return true;
    }
}