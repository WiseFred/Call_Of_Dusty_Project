package fr.anthognie.Core.commands;

import fr.anthognie.Core.Main;
import fr.anthognie.Core.gui.ItemDatabaseGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ItemDatabaseCommand implements CommandExecutor {

    private final ItemDatabaseGUI gui;

    public ItemDatabaseCommand(Main plugin) {
        this.gui = plugin.getItemDatabaseGUI();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs.");
            return true;
        }

        gui.open((Player) sender, 1);
        return true;
    }
}