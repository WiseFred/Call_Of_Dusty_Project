package fr.anthognie.airdrops.commands;

import fr.anthognie.airdrops.Main;
import fr.anthognie.airdrops.managers.AirdropManager;
import fr.anthognie.airdrops.managers.LootManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class AirdropCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;
    private final AirdropManager airdropManager;
    private final LootManager lootManager;

    public static final String LOOT_EDIT_TITLE_PREFIX = "§c[ÉDITION] Kit: ";

    public AirdropCommand(Main plugin) {
        this.plugin = plugin;
        this.airdropManager = plugin.getAirdropManager();
        this.lootManager = plugin.getLootManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§cVous n'avez pas la permission.");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }
        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "force":
                handleForce(sender, args);
                break;
            case "toggle":
                handleToggle(sender, args);
                break;
            case "state":
                handleState(sender);
                break;
            case "createkit":
                handleCreateKit(sender, args);
                break;
            case "deletekit":
                handleDeleteKit(sender, args);
                break;
            case "editloot":
                handleEditLoot(sender, args);
                break;
            // --- NOUVELLE COMMANDE ---
            case "reset":
                airdropManager.resetAllAirdrops();
                sender.sendMessage("§aTous les coffres d'airdrops actifs ont été supprimés.");
                break;
            default:
                sendHelp(sender, label);
                break;
        }
        return true;
    }

    private void handleForce(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUtilisation: /airdrop force <normal|ultimate>");
            return;
        }
        if (args[1].equalsIgnoreCase("ultimate")) {
            airdropManager.forceAirdrop(true);
            sender.sendMessage("§aForçage de la séquence d'airdrop ULTIME (5s).");
        } else {
            airdropManager.forceAirdrop(false);
            // Le message de broadcast est géré par le manager
        }
    }

    private void handleToggle(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUtilisation: /airdrop toggle <normal|ultimate> <on|off>");
            return;
        }
        boolean isUltimate = args[1].equalsIgnoreCase("ultimate");
        boolean enabled = args[2].equalsIgnoreCase("on");
        airdropManager.setDropsEnabled(isUltimate, enabled);
        sender.sendMessage("§aAirdrops " + (isUltimate ? "ULTIMES" : "NORMAUX") + " " + (enabled ? "ACTIVÉS" : "DÉSACIVÉS") + ".");
    }

    private void handleState(CommandSender sender) {
        boolean normalOn = airdropManager.areDropsEnabled(false);
        boolean ultimateOn = airdropManager.areDropsEnabled(true);
        sender.sendMessage("§e--- État des Airdrops ---");
        sender.sendMessage("§fAirdrops Normaux: " + (normalOn ? "§aON" : "§cOFF"));
        sender.sendMessage("§fAirdrops Ultimes: " + (ultimateOn ? "§aON" : "§cOFF"));
    }

    private void handleCreateKit(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cUtilisation: /airdrop createkit <normal|ultimate> <nom_du_kit> <chance>");
            return;
        }
        String type = args[1].toLowerCase();
        if (!type.equals("normal") && !type.equals("ultimate")) {
            sender.sendMessage("§cLe type doit être 'normal' ou 'ultimate'.");
            return;
        }
        String kitName = args[2];
        int chance;
        try {
            chance = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cLa chance doit être un nombre (ex: 40).");
            return;
        }

        FileConfiguration lootConfig = lootManager.getConfig();
        String path = type + ".kits." + kitName;
        if (lootConfig.contains(path)) {
            sender.sendMessage("§cCe kit existe déjà. Utilisez /airdrop editloot pour le modifier.");
            return;
        }

        lootConfig.set(path + ".chance", chance);
        lootManager.saveConfig();

        sender.sendMessage("§aKit '" + kitName + "' (" + type + ") créé avec " + chance + "% de chance.");
        sender.sendMessage("§eUtilisez /airdrop editloot " + type + " " + kitName + " §epour ajouter des items.");
    }

    private void handleDeleteKit(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUtilisation: /airdrop deletekit <normal|ultimate> <nom_du_kit>");
            return;
        }
        String type = args[1].toLowerCase();
        String kitName = args[2];
        FileConfiguration lootConfig = lootManager.getConfig();
        String path = type + ".kits." + kitName;

        if (!lootConfig.contains(path)) {
            sender.sendMessage("§cCe kit n'existe pas.");
            return;
        }

        lootConfig.set(path, null);
        lootManager.saveConfig();

        String itemPath = "airdrops.loot." + type + "." + kitName;
        plugin.getItemConfigManager().setItemStack(itemPath, null);
        plugin.getItemConfigManager().saveConfig();

        sender.sendMessage("§aKit '" + kitName + "' supprimé (items et config).");
    }

    private void handleEditLoot(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs.");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("§cUtilisation: /airdrop editloot <normal|ultimate> <nom_du_kit>");
            return;
        }
        String type = args[1].toLowerCase();
        String kitName = args[2];
        FileConfiguration lootConfig = lootManager.getConfig();
        String path = type + ".kits." + kitName;

        if (!lootConfig.contains(path)) {
            sender.sendMessage("§cCe kit n'existe pas. Créez-le d'abord avec /airdrop createkit.");
            return;
        }

        Player player = (Player) sender;

        String title = LOOT_EDIT_TITLE_PREFIX + type + ":" + kitName;
        Inventory inv = Bukkit.createInventory(null, 54, title);

        String itemPath = "airdrops.loot." + type + "." + kitName;
        String base64data = plugin.getItemConfigManager().getConfig().getString(itemPath);
        if (base64data != null && !base64data.isEmpty()) {
            try {
                inv.setContents(fr.anthognie.Core.utils.InventorySerializer.itemStackArrayFromBase64(base64data));
            } catch (Exception e) {
                sender.sendMessage("§cErreur lors du chargement de ce kit. L'inventaire est peut-être corrompu.");
            }
        }

        player.openInventory(inv);
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage("§e--- Aide Airdrops Admin (/airdrop) ---");
        sender.sendMessage("§f/" + label + " state");
        sender.sendMessage("§f/" + label + " force <normal|ultimate>");
        sender.sendMessage("§f/" + label + " toggle <normal|ultimate> <on|off>");
        sender.sendMessage("§f/" + label + " createkit <type> <nom> <chance>");
        sender.sendMessage("§f/" + label + " deletekit <type> <nom>");
        sender.sendMessage("§f/" + label + " editloot <type> <nom>");
        sender.sendMessage("§f/" + label + " reset §7- Supprime tous les coffres actifs.");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        FileConfiguration lootConfig = lootManager.getConfig();

        if (args.length == 1) {
            return List.of("force", "toggle", "state", "createkit", "deletekit", "editloot", "reset").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("force") || args[0].equalsIgnoreCase("toggle") ||
                    args[0].equalsIgnoreCase("createkit") || args[0].equalsIgnoreCase("deletekit") ||
                    args[0].equalsIgnoreCase("editloot")) {

                return List.of("normal", "ultimate").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("toggle")) {
                return List.of("on", "off").stream()
                        .filter(s -> s.startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args[0].equalsIgnoreCase("deletekit") || args[0].equalsIgnoreCase("editloot")) {
                String type = args[1].toLowerCase();
                if (lootConfig.contains(type + ".kits")) {
                    return lootConfig.getConfigurationSection(type + ".kits").getKeys(false).stream()
                            .filter(s -> s.startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
        }
        return null;
    }
}