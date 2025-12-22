package fr.anthognie.FFA;

import fr.anthognie.Core.managers.EconomyManager; // Import
import fr.anthognie.Core.managers.ItemConfigManager; // Import
import fr.anthognie.FFA.commands.*;
import fr.anthognie.FFA.game.FFAManager;
import fr.anthognie.FFA.gui.FFAConfigGUI;
import fr.anthognie.FFA.gui.ShopGUI;
import fr.anthognie.FFA.listeners.*;
import fr.anthognie.FFA.managers.*; // Import global pour DataManager, BountyManager, etc.
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private fr.anthognie.Core.Main core;

    // Managers Locaux (FFA)
    private ConfigManager configManager;
    private FFAManager ffaManager;
    private KillstreakManager killstreakManager;
    private LevelManager levelManager;
    private ScoreboardManager scoreboardManager;
    private DataManager dataManager;     // Manquait
    private BountyManager bountyManager; // Manquait

    // GUIs
    private ShopGUI shopGUI;
    private FFAConfigGUI ffaConfigGUI;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Connexion au Core
        this.core = (fr.anthognie.Core.Main) getServer().getPluginManager().getPlugin("Core");
        if (this.core == null) {
            getLogger().severe("ERREUR: Le plugin Core n'est pas installé ou chargé !");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialisation Managers Locaux
        this.configManager = new ConfigManager(this);
        this.dataManager = new DataManager(this); // Init DataManager

        this.shopGUI = new ShopGUI(this);
        this.ffaConfigGUI = new FFAConfigGUI(this);

        this.killstreakManager = new KillstreakManager(this); // CORRECTION: Argument 'this' ajouté
        this.levelManager = new LevelManager(this);

        // FFAManager a besoin des managers du Core
        this.ffaManager = new FFAManager(this, core.getItemConfigManager(), configManager);

        // BountyManager et ScoreboardManager ont besoin de l'éco du Core
        this.bountyManager = new BountyManager(this);
        this.scoreboardManager = new ScoreboardManager(this, killstreakManager, core.getEconomyManager(), ffaManager);

        registerCommands();
        registerListeners();

        getLogger().info("Module FFA chargé avec succès !");
    }

    private void registerCommands() {
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("addspawn").setExecutor(new AddSpawnCommand(this));
        getCommand("ffaconfig").setExecutor(new FFAConfigCommand(this));
        // StatsCommand a besoin d'être enregistrée si elle existe
        getCommand("stats").setExecutor(new StatsCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerArenaListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopEditListener(this), this);
        getServer().getPluginManager().registerEvents(new FFAConfigListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDataListener(this), this); // Pour DataManager

        // PlayerDamageListener et PlayerQuitListener
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
    }

    // --- GETTERS LOCAUX ---

    public FFAManager getFfaManager() { return ffaManager; }
    public ConfigManager getFfaConfigManager() { return configManager; }
    public ShopGUI getShopGUI() { return shopGUI; }
    public FFAConfigGUI getFfaConfigGUI() { return ffaConfigGUI; }
    public KillstreakManager getKillstreakManager() { return killstreakManager; }
    public LevelManager getLevelManager() { return levelManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public DataManager getDataManager() { return dataManager; } // Getter manquant ajouté
    public BountyManager getBountyManager() { return bountyManager; } // Getter manquant ajouté

    // --- PROXY GETTERS VERS CORE (Pour corriger tes erreurs) ---
    // Ces méthodes permettent d'appeler plugin.getEconomyManager() depuis FFA sans erreur

    public fr.anthognie.Core.Main getCore() { return core; }

    public EconomyManager getEconomyManager() {
        return core.getEconomyManager();
    }

    public ItemConfigManager getItemConfigManager() {
        return core.getItemConfigManager();
    }
}