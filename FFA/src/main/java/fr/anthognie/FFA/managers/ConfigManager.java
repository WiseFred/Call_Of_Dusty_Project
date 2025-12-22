package fr.anthognie.FFA.managers;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ConfigManager {

    private final JavaPlugin plugin;

    // Fichiers de configuration
    private File shopConfigFile;
    private FileConfiguration shopConfig;

    private File ffaConfigFile;
    private FileConfiguration ffaConfig;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();
        createShopConfig();
        createFFAConfig(); // Important : Initialisation de ffa.yml
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public void saveConfig() {
        plugin.saveConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        reloadShopConfig();
        reloadFFAConfig();
    }

    // --- GESTION SHOP.YML ---
    private void createShopConfig() {
        shopConfigFile = new File(plugin.getDataFolder(), "shop.yml");
        if (!shopConfigFile.exists()) {
            shopConfigFile.getParentFile().mkdirs();
            plugin.saveResource("shop.yml", false);
        }
        shopConfig = new YamlConfiguration();
        try {
            shopConfig.load(shopConfigFile);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur chargement shop.yml", e);
        }
    }

    public FileConfiguration getShopConfig() {
        if (shopConfig == null) createShopConfig();
        return shopConfig;
    }

    public void saveShopConfig() {
        try {
            getShopConfig().save(shopConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur sauvegarde shop.yml", e);
        }
    }

    public void reloadShopConfig() {
        shopConfigFile = new File(plugin.getDataFolder(), "shop.yml");
        shopConfig = YamlConfiguration.loadConfiguration(shopConfigFile);
    }

    // --- GESTION FFA.YML ---
    private void createFFAConfig() {
        ffaConfigFile = new File(plugin.getDataFolder(), "ffa.yml");
        if (!ffaConfigFile.exists()) {
            ffaConfigFile.getParentFile().mkdirs();
            plugin.saveResource("ffa.yml", false);
        }
        ffaConfig = new YamlConfiguration();
        try {
            ffaConfig.load(ffaConfigFile);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur chargement ffa.yml", e);
        }
    }

    public FileConfiguration getFFAConfig() {
        if (ffaConfig == null) createFFAConfig();
        return ffaConfig;
    }

    public void saveFFAConfig() {
        try {
            getFFAConfig().save(ffaConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur sauvegarde ffa.yml", e);
        }
    }

    public void reloadFFAConfig() {
        ffaConfigFile = new File(plugin.getDataFolder(), "ffa.yml");
        ffaConfig = YamlConfiguration.loadConfiguration(ffaConfigFile);
    }

    // --- GESTION DES SPAWNS (Correction : Utilise ffa.yml) ---
    public void addSpawn(Location location) {
        List<Location> spawns = getSpawnLocations();
        spawns.add(location);
        getFFAConfig().set("spawns", spawns);
        saveFFAConfig();
    }

    public List<Location> getSpawnLocations() {
        // Lit explicitement dans ffaConfig au lieu de getConfig()
        List<?> list = getFFAConfig().getList("spawns");
        List<Location> spawns = new ArrayList<>();
        if (list != null) {
            for (Object object : list) {
                if (object instanceof Location) {
                    spawns.add((Location) object);
                }
            }
        }
        return spawns;
    }
}