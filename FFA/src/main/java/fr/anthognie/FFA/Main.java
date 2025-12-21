package fr.anthognie.FFA;

import fr.anthognie.Core.managers.EconomyManager;
import fr.anthognie.Core.managers.ItemConfigManager;
import fr.anthognie.FFA.commands.*;
import fr.anthognie.FFA.game.FFAManager;
import fr.anthognie.FFA.gui.FFAConfigGUI;
import fr.anthognie.FFA.gui.ShopGUI;
import fr.anthognie.FFA.listeners.*;
import fr.anthognie.FFA.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private fr.anthognie.Core.Main corePlugin;
    private EconomyManager economyManager;
    private ItemConfigManager itemConfigManager;

    private ConfigManager ffaConfigManager;
    private FFAManager ffaManager;
    private KillstreakManager killstreakManager;
    private ScoreboardManager scoreboardManager;
    private LevelManager levelManager;
    private BountyManager bountyManager;
    private DataManager dataManager; // NOUVEAU

    private ShopGUI shopGUI;
    private FFAConfigGUI ffaConfigGUI;

    @Override
    public void onEnable() {
        instance = this;
        this.corePlugin = fr.anthognie.Core.Main.getInstance();

        if (this.corePlugin == null) {
            getLogger().severe("ERREUR CRITIQUE: Core n'a pas été trouvé !");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.economyManager = corePlugin.getEconomyManager();
        this.itemConfigManager = corePlugin.getItemConfigManager();

        saveResource("ffa.yml", false);

        initializeManagers();
        registerCommands();
        registerListeners();

        getLogger().info("Call of Dusty [FFA] activé.");
    }

    private void initializeManagers() {
        this.ffaConfigManager = new ConfigManager(this);
        this.killstreakManager = new KillstreakManager(this);
        this.levelManager = new LevelManager(this);
        this.bountyManager = new BountyManager(this);

        // DataManager doit être initialisé avant que des joueurs ne fassent des actions
        this.dataManager = new DataManager(this);

        this.ffaManager = new FFAManager(this, this.itemConfigManager, this.ffaConfigManager);
        this.scoreboardManager = new ScoreboardManager(this, this.killstreakManager, this.economyManager, this.ffaManager);

        this.shopGUI = new ShopGUI(this);
        this.ffaConfigGUI = new FFAConfigGUI(this);
    }

    private void registerCommands() {
        getCommand("leave").setExecutor(new LeaveCommand(this));
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("editshop").setExecutor(new EditShopCommand(this));
        getCommand("addspawn").setExecutor(new AddSpawnCommand(this));
        getCommand("ffaconfig").setExecutor(new FFAConfigCommand(this));
        getCommand("resetkills").setExecutor(new ResetKillsCommand(this));
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("xp").setExecutor(new XpCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerArenaListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerRegenListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopEditListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new FFAConfigListener(this), this);
        getServer().getPluginManager().registerEvents(new FFAProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new KillstreakListener(this), this);

        // --- NOUVEAU LISTENER ---
        getServer().getPluginManager().registerEvents(new PlayerDataListener(this), this);
    }

    @Override
    public void onDisable() {
        // Sauvegarde de sécurité
        if (dataManager != null) {
            dataManager.saveAllOnline();
        }

        if (ffaManager != null && ffaManager.getFFAWorldName() != null) {
            try {
                if (Bukkit.getWorld(ffaManager.getFFAWorldName()) != null) {
                    for (Player player : Bukkit.getWorld(ffaManager.getFFAWorldName()).getPlayers()) {
                        economyManager.saveInventory(player.getUniqueId(), player.getInventory().getContents());
                        ffaManager.clearInvincibility(player);
                    }
                }
            } catch (Exception e) {}
        }
        getLogger().info("Call of Dusty [FFA] désactivé.");
    }

    public static Main getInstance() { return instance; }

    public EconomyManager getEconomyManager() { return economyManager; }
    public ItemConfigManager getItemConfigManager() { return itemConfigManager; }
    public FFAManager getFfaManager() { return ffaManager; }
    public ConfigManager getFfaConfigManager() { return ffaConfigManager; }
    public KillstreakManager getKillstreakManager() { return killstreakManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public LevelManager getLevelManager() { return levelManager; }
    public BountyManager getBountyManager() { return bountyManager; }
    public DataManager getDataManager() { return dataManager; } // Getter
    public ShopGUI getShopGUI() { return shopGUI; }
    public FFAConfigGUI getFfaConfigGUI() { return ffaConfigGUI; }
    public fr.anthognie.Core.Main getCore() { return corePlugin; }
}