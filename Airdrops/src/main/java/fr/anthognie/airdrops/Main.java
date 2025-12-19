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
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private fr.anthognie.Core.Main corePlugin;
    private ItemConfigManager itemConfigManager;

    private LootManager lootManager;
    private AirdropManager airdropManager;

    // GUIs
    private AirdropConfigGUI airdropConfigGUI;
    private LootBrowserGUI lootBrowserGUI;
    private LootKitListGUI lootKitListGUI;

    // Listeners Chat
    private LootKitChatListener lootKitChatListener;

    @Override
    public void onEnable() {
        instance = this;
        this.corePlugin = fr.anthognie.Core.Main.getInstance();

        if (this.corePlugin == null) {
            getLogger().severe("ERREUR CRITIQUE: Core n'a pas été trouvé !");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.itemConfigManager = corePlugin.getItemConfigManager();

        saveResource("config.yml", false);
        saveResource("loot.yml", false);

        initializeManagers();
        registerCommands();
        registerListeners();

        // Lancement des timers (Drops auto)
        this.airdropManager.startTimers();

        getLogger().info("Call of Dusty [Airdrops] activé.");
    }

    private void initializeManagers() {
        this.lootManager = new LootManager(this);
        // CORRECTION : On utilise le constructeur qui prend juste 'Main'
        this.airdropManager = new AirdropManager(this);

        this.airdropConfigGUI = new AirdropConfigGUI(this);
        this.lootBrowserGUI = new LootBrowserGUI(this);
        this.lootKitListGUI = new LootKitListGUI(this);

        this.lootKitChatListener = new LootKitChatListener(this);
    }

    private void registerCommands() {
        getCommand("airdrop").setExecutor(new AirdropCommand(this));
        getCommand("airdropconfig").setExecutor(new AirdropConfigCommand(this));
    }

    private void registerListeners() {
        // CORRECTION : On utilise le constructeur qui prend juste 'Main'
        getServer().getPluginManager().registerEvents(new AirdropPlayerListener(this), this);

        getServer().getPluginManager().registerEvents(new LootEditorListener(this), this);
        getServer().getPluginManager().registerEvents(this.lootKitChatListener, this);
        getServer().getPluginManager().registerEvents(new AirdropConfigListener(this), this);
        getServer().getPluginManager().registerEvents(new LootBrowserListener(this), this);
    }

    @Override
    public void onDisable() {
        if (airdropManager != null) {
            airdropManager.cancelAllTimers(); // Arrête les timers proprement
            airdropManager.resetAllAirdrops(); // Supprime les coffres physiques
        }
        getLogger().info("Call of Dusty [Airdrops] désactivé.");
    }

    public static Main getInstance() { return instance; }
    public LootManager getLootManager() { return lootManager; }
    public AirdropManager getAirdropManager() { return airdropManager; }
    public ItemConfigManager getItemConfigManager() { return itemConfigManager; }

    public AirdropConfigGUI getAirdropConfigGUI() { return airdropConfigGUI; }
    public LootBrowserGUI getLootBrowserGUI() { return lootBrowserGUI; }
    public LootKitListGUI getLootKitListGUI() { return lootKitListGUI; }

    public LootKitChatListener getLootKitChatListener() { return lootKitChatListener; }
}