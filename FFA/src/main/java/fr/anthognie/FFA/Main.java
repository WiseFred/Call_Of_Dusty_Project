package fr.anthognie.FFA;

import fr.anthognie.Core.managers.EconomyManager;
import fr.anthognie.Core.managers.ItemConfigManager;
import fr.anthognie.FFA.commands.AddSpawnCommand;
import fr.anthognie.FFA.commands.EditShopCommand;
import fr.anthognie.FFA.commands.FFAConfigCommand;
import fr.anthognie.FFA.commands.LeaveCommand;
import fr.anthognie.FFA.commands.ShopCommand;
import fr.anthognie.FFA.game.FFAManager;
import fr.anthognie.FFA.gui.FFAConfigGUI;
import fr.anthognie.FFA.gui.ShopGUI;
import fr.anthognie.FFA.listeners.*;
import fr.anthognie.FFA.managers.ConfigManager;
import fr.anthognie.FFA.managers.KillstreakManager;
import fr.anthognie.FFA.managers.ScoreboardManager;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
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

        // S'assurer que le ffa.yml est créé
        saveResource("ffa.yml", false);

        initializeManagers();
        registerCommands();
        registerListeners();

        getLogger().info("Call of Dusty [FFA] activé et connecté au Core.");
    }

    private void initializeManagers() {
        this.ffaConfigManager = new ConfigManager(this);
        this.killstreakManager = new KillstreakManager();
        this.ffaManager = new FFAManager(this, this.itemConfigManager, this.ffaConfigManager);
        this.scoreboardManager = new ScoreboardManager(this, this.killstreakManager, this.economyManager, this.ffaManager);
        this.shopGUI = new ShopGUI(this);
        this.ffaConfigGUI = new FFAConfigGUI(this);

        // --- NOUVEAU : On initialise le "sniffer" de son ---
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
        // Listeners Spigot (Bukkit)
        getServer().getPluginManager().registerEvents(new PlayerArenaListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerRegenListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopEditListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new FFAConfigListener(this), this);
        // (Le HeadshotSoundListener s'enregistre tout seul auprès de ProtocolLib)
    }

    @Override
    public void onDisable() {
        if (ffaManager != null && ffaManager.getFFAWorldName() != null) {
            try {
                for (Player player : Bukkit.getWorld(ffaManager.getFFAWorldName()).getPlayers()) {
                    economyManager.saveInventory(player.getUniqueId(), player.getInventory().getContents());
                    ffaManager.clearInvincibility(player);
                }
            } catch (Exception e) {
                // S'il n'y a pas de joueurs ou que le monde n'est pas chargé, on ignore
            }
        }
        getLogger().info("Call of Dusty [FFA] désactivé.");
    }

    // ... (tous les getters) ...
    public static Main getInstance() { return instance; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public FFAManager getFfaManager() { return ffaManager; }
    public ItemConfigManager getItemConfigManager() { return itemConfigManager; }
    public KillstreakManager getKillstreakManager() { return killstreakManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public ShopGUI getShopGUI() { return shopGUI; }
    public ConfigManager getFfaConfigManager() { return ffaConfigManager; }
    public FFAConfigGUI getFfaConfigGUI() { return ffaConfigGUI; }
    public fr.anthognie.Core.Main getCore() { return corePlugin; }
}