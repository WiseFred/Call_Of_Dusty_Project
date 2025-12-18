package fr.anthognie.spawn;

import fr.anthognie.spawn.commands.AdminCommand;
import fr.anthognie.spawn.gui.AdminDashboardGUI;
import fr.anthognie.spawn.gui.GameSelectorGUI;
import fr.anthognie.spawn.gui.SpawnConfigGUI; // <-- NOUVEL IMPORT
import fr.anthognie.spawn.listeners.AdminDashboardListener;
import fr.anthognie.spawn.listeners.CompassListener;
import fr.anthognie.spawn.listeners.GameSelectorListener;
import fr.anthognie.spawn.listeners.SpawnConfigListener; // <-- NOUVEL IMPORT
import fr.anthognie.spawn.listeners.SpawnListener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private fr.anthognie.Core.Main core;
    private GameSelectorGUI gameSelectorGUI;
    private AdminDashboardGUI adminDashboardGUI;
    private SpawnConfigGUI spawnConfigGUI; // <-- NOUVEAU

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.core = (fr.anthognie.Core.Main) getServer().getPluginManager().getPlugin("Core");
        if (this.core == null) {
            getLogger().severe("ERREUR CRITIQUE: Core n'a pas été trouvé !");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        initializeManagers();
        registerListeners();
        registerCommands();

        getLogger().info("Module Spawn activé et connecté au Core !");
    }

    private void initializeManagers() {
        this.gameSelectorGUI = new GameSelectorGUI(this);
        this.adminDashboardGUI = new AdminDashboardGUI();
        this.spawnConfigGUI = new SpawnConfigGUI(this); // <-- ON L'INITIALISE
    }

    private void registerCommands() {
        getCommand("codadmin").setExecutor(new AdminCommand(this.adminDashboardGUI));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new SpawnListener(this), this);
        getServer().getPluginManager().registerEvents(new CompassListener(this), this);
        getServer().getPluginManager().registerEvents(new GameSelectorListener(this.gameSelectorGUI), this);
        getServer().getPluginManager().registerEvents(new AdminDashboardListener(this), this); // <-- MODIFIÉ
        getServer().getPluginManager().registerEvents(new SpawnConfigListener(this), this); // <-- NOUVEAU
    }

    @Override
    public void onDisable() {
        getLogger().info("Module Spawn désactivé.");
    }

    public fr.anthognie.Core.Main getCore() {
        return core;
    }

    public GameSelectorGUI getGameSelectorGUI() {
        return gameSelectorGUI;
    }

    public AdminDashboardGUI getAdminDashboardGUI() {
        return adminDashboardGUI;
    }

    // --- NOUVEAU GETTER ---
    public SpawnConfigGUI getSpawnConfigGUI() {
        return spawnConfigGUI;
    }
}