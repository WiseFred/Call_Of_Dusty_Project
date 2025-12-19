package fr.anthognie.FFA;

import fr.anthognie.Core.managers.EconomyManager;
import fr.anthognie.Core.managers.ItemConfigManager;
import fr.anthognie.FFA.commands.*;
import fr.anthognie.FFA.game.FFAManager;
import fr.anthognie.FFA.gui.FFAConfigGUI;
import fr.anthognie.FFA.gui.ShopGUI;
import fr.anthognie.FFA.listeners.*;
import fr.anthognie.FFA.managers.ConfigManager;
import fr.anthognie.FFA.managers.KillstreakManager;
import fr.anthognie.FFA.managers.ScoreboardManager;
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
    private ShopGUI shopGUI;
    private FFAConfigGUI ffaConfigGUI;

    private HeadshotSoundListener headshotListener;

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

        getLogger().info("Call of Dusty [FFA] activé et connecté au Core.");
    }

    private void initializeManagers() {
        this.ffaConfigManager = new ConfigManager(this);
        this.killstreakManager = new KillstreakManager(this);
        this.ffaManager = new FFAManager(this, this.itemConfigManager, this.ffaConfigManager);
        this.scoreboardManager = new ScoreboardManager(this, this.killstreakManager, this.economyManager, this.ffaManager);
        this.shopGUI = new ShopGUI(this);
        this.ffaConfigGUI = new FFAConfigGUI(this);
        this.headshotListener = new HeadshotSoundListener(this);
    }

    private void registerCommands() {
        getCommand("leave").setExecutor(new LeaveCommand(this));
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("editshop").setExecutor(new EditShopCommand(this));
        getCommand("addspawn").setExecutor(new AddSpawnCommand(this));
        getCommand("ffaconfig").setExecutor(new FFAConfigCommand(this));
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
    }

    @Override
    public void onDisable() {
        if (ffaManager != null && ffaManager.getFFAWorldName() != null) {
            try {
                if (Bukkit.getWorld(ffaManager.getFFAWorldName()) != null) {
                    for (Player player : Bukkit.getWorld(ffaManager.getFFAWorldName()).getPlayers()) {
                        economyManager.saveInventory(player.getUniqueId(), player.getInventory().getContents());
                        ffaManager.clearInvincibility(player);
                    }
                }
            } catch (Exception e) {
            }
        }
        getLogger().info("Call of Dusty [FFA] désactivé.");
    }

    public static Main getInstance() { return instance; }

    public EconomyManager getEconomyManager() { return economyManager; }
    public ItemConfigManager getItemConfigManager() { return itemConfigManager; }

    // --- NOUVEAUX NOMS (Utilisés par mes fichiers récents) ---
    public FFAManager getFfaManager() { return ffaManager; }
    public ConfigManager getFfaConfigManager() { return ffaConfigManager; }

    // --- ANCIENS NOMS (ALIAS) (Pour corriger tes erreurs AddSpawnCommand / PlayerDamageListener) ---
    public FFAManager getFFAManager() { return ffaManager; }
    public ConfigManager getConfigManager() { return ffaConfigManager; }
    // -----------------------------------------------------------------------------------------------

    public KillstreakManager getKillstreakManager() { return killstreakManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public ShopGUI getShopGUI() { return shopGUI; }
    public FFAConfigGUI getFfaConfigGUI() { return ffaConfigGUI; }

    public fr.anthognie.Core.Main getCore() { return corePlugin; }
}