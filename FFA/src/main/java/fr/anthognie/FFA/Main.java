package fr.anthognie.FFA;

import fr.anthognie.Core.managers.EconomyManager;
import fr.anthognie.Core.managers.ItemConfigManager;
import fr.anthognie.FFA.commands.*;
import fr.anthognie.FFA.game.FFAManager;
import fr.anthognie.FFA.gui.FFAConfigGUI;
import fr.anthognie.FFA.gui.ShopGUI;
import fr.anthognie.FFA.listeners.*;
import fr.anthognie.FFA.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private fr.anthognie.Core.Main core;
    private ConfigManager configManager;
    private FFAManager ffaManager;
    private KillstreakManager killstreakManager;
    private LevelManager levelManager;
    private ScoreboardManager scoreboardManager;
    private DataManager dataManager;
    private BountyManager bountyManager;
    private ShopGUI shopGUI;
    private FFAConfigGUI ffaConfigGUI;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.core = (fr.anthognie.Core.Main) getServer().getPluginManager().getPlugin("Core");
        if (this.core == null) {
            getLogger().severe("ERREUR: Le plugin Core n'est pas installé ou chargé !");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.configManager = new ConfigManager(this);
        this.dataManager = new DataManager(this);
        this.shopGUI = new ShopGUI(this);
        this.ffaConfigGUI = new FFAConfigGUI(this);
        this.killstreakManager = new KillstreakManager(this);
        this.levelManager = new LevelManager(this);
        this.ffaManager = new FFAManager(this, core.getItemConfigManager(), configManager);
        this.bountyManager = new BountyManager(this);
        this.scoreboardManager = new ScoreboardManager(this, killstreakManager, core.getEconomyManager(), ffaManager);

        registerCommands();
        registerListeners();

        getLogger().info("FFA Plugin activé !");
    }

    @Override
    public void onDisable() {
        if(dataManager != null) {
            dataManager.saveAllData();
        }
    }

    private void registerCommands() {
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("addspawn").setExecutor(new AddSpawnCommand(this));
        getCommand("ffaconfig").setExecutor(new FFAConfigCommand(this));
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("joinffa").setExecutor(new JoinCommand(this));
        getCommand("nuke").setExecutor(new NukeCommand(this));
        getCommand("screamer").setExecutor(new ScreamerCommand(this));
        getCommand("leave").setExecutor(new LeaveCommand(this));
        getCommand("editshop").setExecutor(new EditShopCommand(this));
        getCommand("resetkills").setExecutor(new ResetKillsCommand(this));
        getCommand("xp").setExecutor(new XpCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerArenaListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopEditListener(this), this);
        getServer().getPluginManager().registerEvents(new FFAConfigListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDataListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new KillstreakListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerRegenListener(this), this);
        getServer().getPluginManager().registerEvents(new FFAProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new StatsListener(this), this);

        // --- C'EST LA LIGNE QUI MANQUAIT POUR LE SPAWN LOBBY ---
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    }

    public FFAManager getFfaManager() { return ffaManager; }
    public ConfigManager getFfaConfigManager() { return configManager; }
    public ShopGUI getShopGUI() { return shopGUI; }
    public FFAConfigGUI getFfaConfigGUI() { return ffaConfigGUI; }
    public KillstreakManager getKillstreakManager() { return killstreakManager; }
    public LevelManager getLevelManager() { return levelManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public DataManager getDataManager() { return dataManager; }
    public BountyManager getBountyManager() { return bountyManager; }
    public fr.anthognie.Core.Main getCore() { return core; }
    public EconomyManager getEconomyManager() { return core.getEconomyManager(); }
    public ItemConfigManager getItemConfigManager() { return core.getItemConfigManager(); }
}