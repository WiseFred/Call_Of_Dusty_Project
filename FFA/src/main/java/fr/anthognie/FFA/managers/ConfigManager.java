package fr.anthognie.FFA.managers;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final JavaPlugin plugin;
    private File shopConfigFile;
    private FileConfiguration shopConfig;
    private File ffaConfigFile;
    private FileConfiguration ffaConfig;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();
        createShopConfig();
        createFFAConfig();
    }

    public FileConfiguration getConfig() { return plugin.getConfig(); }
    public void saveConfig() { plugin.saveConfig(); }
    public void reloadConfig() { plugin.reloadConfig(); reloadShopConfig(); reloadFFAConfig(); }

    private void createShopConfig() {
        shopConfigFile = new File(plugin.getDataFolder(), "shop.yml");
        if (!shopConfigFile.exists()) {
            shopConfigFile.getParentFile().mkdirs();
            plugin.saveResource("shop.yml", false);
        }
        shopConfig = YamlConfiguration.loadConfiguration(shopConfigFile);
    }
    public FileConfiguration getShopConfig() { if (shopConfig == null) createShopConfig(); return shopConfig; }
    public void saveShopConfig() { try { getShopConfig().save(shopConfigFile); } catch (IOException e) { e.printStackTrace(); } }
    public void reloadShopConfig() { shopConfigFile = new File(plugin.getDataFolder(), "shop.yml"); shopConfig = YamlConfiguration.loadConfiguration(shopConfigFile); }

    private void createFFAConfig() {
        ffaConfigFile = new File(plugin.getDataFolder(), "ffa.yml");
        if (!ffaConfigFile.exists()) {
            ffaConfigFile.getParentFile().mkdirs();
            plugin.saveResource("ffa.yml", false);
        }
        ffaConfig = YamlConfiguration.loadConfiguration(ffaConfigFile);
    }
    public FileConfiguration getFFAConfig() { if (ffaConfig == null) createFFAConfig(); return ffaConfig; }
    public void saveFFAConfig() { try { getFFAConfig().save(ffaConfigFile); } catch (IOException e) { e.printStackTrace(); } }
    public void reloadFFAConfig() { ffaConfigFile = new File(plugin.getDataFolder(), "ffa.yml"); ffaConfig = YamlConfiguration.loadConfiguration(ffaConfigFile); }

    public void addSpawn(Location location) {
        List<Location> spawns = getSpawnLocations();
        spawns.add(location);
        getFFAConfig().set("spawns", spawns);
        saveFFAConfig();
    }

    // CORRECTION : Lecture robuste des spawns
    public List<Location> getSpawnLocations() {
        List<?> list = getFFAConfig().getList("spawns");
        List<Location> spawns = new ArrayList<>();
        if (list != null) {
            for (Object object : list) {
                if (object instanceof Location) {
                    spawns.add((Location) object);
                } else if (object instanceof Map) {
                    // Tente de désérialiser si c'est une Map (arrive parfois avec YML)
                    try {
                        spawns.add(Location.deserialize((Map<String, Object>) object));
                    } catch (Exception ignored) {}
                }
            }
        }
        return spawns;
    }
}