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
    private File shopConfigFile;
    private FileConfiguration shopConfig;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();
        createShopConfig();
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
    }

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
            plugin.getLogger().log(Level.SEVERE, "Could not load shop.yml", e);
        }
    }

    public FileConfiguration getShopConfig() {
        if (shopConfig == null) {
            createShopConfig();
        }
        return shopConfig;
    }

    public void saveShopConfig() {
        if (shopConfig == null || shopConfigFile == null) {
            return;
        }
        try {
            getShopConfig().save(shopConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save shop.yml", e);
        }
    }

    public void reloadShopConfig() {
        if (shopConfigFile == null) {
            shopConfigFile = new File(plugin.getDataFolder(), "shop.yml");
        }
        shopConfig = YamlConfiguration.loadConfiguration(shopConfigFile);
    }

    public void addSpawn(Location location) {
        List<Location> spawns = getSpawnLocations();
        spawns.add(location);
        plugin.getConfig().set("spawns", spawns);
        saveConfig();
    }

    public List<Location> getSpawnLocations() {
        List<?> list = plugin.getConfig().getList("spawns");
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