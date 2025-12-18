package fr.anthognie.airdrops;


import fr.anthognie.Core.managers.ItemConfigManager;
import fr.anthognie.airdrops.commands.AirdropCommand;
import fr.anthognie.airdrops.commands.AirdropConfigCommand;
import fr.anthognie.airdrops.gui.AirdropConfigGUI;
import fr.anthognie.airdrops.gui.LootBrowserGUI;
import fr.anthognie.airdrops.gui.LootKitListGUI;
import fr.anthognie.airdrops.listeners.AirdropConfigListener;
import fr.anthognie.airdrops.listeners.AirdropPlayerListener;
import fr.anthognie.airdrops.listeners.LootBrowserListener;
import fr.anthognie.airdrops.listeners.LootEditorListener;
import fr.anthognie.airdrops.listeners.LootKitChatListener;
import fr.anthognie.airdrops.managers.AirdropManager;
import fr.anthognie.airdrops.managers.LootManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private fr.anthognie.Core.Main corePlugin;
    private ItemConfigManager itemConfigManager;

    private AirdropManager airdropManager;
    private LootManager lootManager;
    private AirdropConfigGUI airdropConfigGUI;

    // Nouveaux GUIs et Listeners
    private LootBrowserGUI lootBrowserGUI;
    private LootKitListGUI lootKitListGUI;
    private LootKitChatListener lootKitChatListener;

    @Override
    public void onEnable() {
        instance = this;

        // Connexion au Core
        this.corePlugin = (fr.anthognie.Core.Main) Bukkit.getPluginManager().getPlugin("Core");
        if (this.corePlugin == null) {
            getLogger().severe("ERREUR CRITIQUE: Core n'a pas été trouvé !");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Récupération des managers du Core
        this.itemConfigManager = corePlugin.getItemConfigManager();
        if (this.itemConfigManager == null) {
            getLogger().severe("ERREUR CRITIQUE: ItemConfigManager n'a pas été trouvé !");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialisation (configs, puis managers)
        saveDefaultConfig(); // Sauvegarde config.yml
        initializeManagers();
        registerCommands();
        registerListeners();

        // Démarrage des timers
        this.airdropManager.startTimers();

        getLogger().info("-------------------------------------");
        getLogger().info("Module Airdrops activé !");
        getLogger().info("Connecté au Core.");
        getLogger().info("-------------------------------------");
    }

    private void initializeManagers() {
        this.lootManager = new LootManager(this);
        this.airdropManager = new AirdropManager(this, this.lootManager);
        this.airdropConfigGUI = new AirdropConfigGUI(this);

        // Initialisation des nouveaux GUIs/Listeners
        this.lootBrowserGUI = new LootBrowserGUI();
        this.lootKitListGUI = new LootKitListGUI(this.lootManager);
        this.lootKitChatListener = new LootKitChatListener(this);
    }

    private void registerCommands() {
        AirdropCommand adminCommand = new AirdropCommand(this);
        getCommand("airdrop").setExecutor(adminCommand);
        getCommand("airdrop").setTabCompleter(adminCommand);

        getCommand("airdropconfig").setExecutor(new AirdropConfigCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new AirdropPlayerListener(this, this.airdropManager), this);
        getServer().getPluginManager().registerEvents(new LootEditorListener(this), this);
        getServer().getPluginManager().registerEvents(new AirdropConfigListener(this), this);

        // Ajout des nouveaux listeners pour le GUI de loot
        getServer().getPluginManager().registerEvents(new LootBrowserListener(this), this);
        getServer().getPluginManager().registerEvents(this.lootKitChatListener, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Module Airdrops désactivé.");
        if (airdropManager != null) {
            airdropManager.cancelAllTimers(); // Arrête tous les timers proprement
        }
    }

    // --- Getters ---

    public static Main getInstance() {
        return instance;
    }

    public ItemConfigManager getItemConfigManager() {
        return itemConfigManager;
    }

    public AirdropManager getAirdropManager() {
        return airdropManager;
    }

    public LootManager getLootManager() {
        return lootManager;
    }

    public AirdropConfigGUI getAirdropConfigGUI() {
        return airdropConfigGUI;
    }

    public LootBrowserGUI getLootBrowserGUI() {
        return lootBrowserGUI;
    }

    public LootKitListGUI getLootKitListGUI() {
        return lootKitListGUI;
    }

    public LootKitChatListener getLootKitChatListener() {
        return lootKitChatListener;
    }
}