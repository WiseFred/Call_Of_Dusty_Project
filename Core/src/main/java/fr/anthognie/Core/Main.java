package fr.anthognie.Core;

import fr.anthognie.Core.commands.BuildModeCommand;
import fr.anthognie.Core.commands.ItemDatabaseCommand;
import fr.anthognie.Core.commands.MoneyCommand;
import fr.anthognie.Core.gui.ItemDatabaseGUI;
import fr.anthognie.Core.listeners.ItemDatabaseChatListener;
import fr.anthognie.Core.listeners.ItemDatabaseListener;
import fr.anthognie.Core.listeners.PlayerJoinListener;
import fr.anthognie.Core.listeners.PlayerLeaveListener;
import fr.anthognie.Core.managers.BuildModeManager;
import fr.anthognie.Core.managers.EconomyManager;
import fr.anthognie.Core.managers.ItemConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private EconomyManager economyManager;
    private ItemConfigManager itemConfigManager;
    private BuildModeManager buildModeManager;

    private ItemDatabaseGUI itemDatabaseGUI;
    private ItemDatabaseChatListener itemDatabaseChatListener;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Call of Dusty [CORE]... Activation.");

        this.economyManager = new EconomyManager(this);
        // initializeDatabase() retiré car on utilise des fichiers maintenant
        getLogger().info("EconomyManager initialisé.");

        this.itemConfigManager = new ItemConfigManager(this);
        getLogger().info("ItemConfigManager initialisé.");

        initializeManagers();
        registerListeners();
        registerCommands();

        getLogger().info("Call of Dusty [CORE] activé !");
    }

    private void initializeManagers() {
        this.itemDatabaseGUI = new ItemDatabaseGUI(itemConfigManager);
        this.itemDatabaseChatListener = new ItemDatabaseChatListener(this);
        this.buildModeManager = new BuildModeManager();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(economyManager), this);
        getServer().getPluginManager().registerEvents(new PlayerLeaveListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemDatabaseListener(this), this);
        getServer().getPluginManager().registerEvents(this.itemDatabaseChatListener, this);
        getLogger().info("Listeners enregistrés.");
    }

    private void registerCommands() {
        MoneyCommand moneyCommand = new MoneyCommand(economyManager);
        getCommand("money").setExecutor(moneyCommand);
        getCommand("money").setTabCompleter(moneyCommand);
        getLogger().info("Commande /money enregistrée.");

        getCommand("itemdb").setExecutor(new ItemDatabaseCommand(this));
        getLogger().info("Commande /itemdb enregistrée.");

        getCommand("buildmode").setExecutor(new BuildModeCommand(this));
        getLogger().info("Commande /buildmode enregistrée.");
    }

    @Override
    public void onDisable() {
        if (economyManager != null) {
            economyManager.saveAllData(); // Nouvelle méthode
            // closeDatabase() retiré
        }
        getLogger().info("Call of Dusty [CORE] désactivé.");
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public ItemConfigManager getItemConfigManager() {
        return itemConfigManager;
    }

    public BuildModeManager getBuildModeManager() {
        return buildModeManager;
    }

    public ItemDatabaseGUI getItemDatabaseGUI() {
        return itemDatabaseGUI;
    }

    public ItemDatabaseChatListener getItemDatabaseChatListener() {
        return itemDatabaseChatListener;
    }

    public static Main getInstance() {
        return instance;
    }
}