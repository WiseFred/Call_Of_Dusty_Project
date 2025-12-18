package fr.anthognie.FFA.managers;

import fr.anthognie.FFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager {

    private final Main plugin;
    private FileConfiguration config;
    private File configFile;

    private List<Location> spawnLocations = new ArrayList<>();

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        this.configFile = new File(plugin.getDataFolder(), "ffa.yml");
        if (!configFile.exists()) {
            plugin.saveResource("ffa.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(configFile);

        loadSpawns();
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder ffa.yml !");
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    private void loadSpawns() {
        spawnLocations.clear();
        for (String s : config.getStringList("spawn-locations")) {
            String safeString = s.replace(',', '.');
            String[] parts = safeString.split(";");
            if (parts.length == 6) {
                try {
                    String world = parts[0];
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double z = Double.parseDouble(parts[3]);
                    float yaw = Float.parseFloat(parts[4]);
                    float pitch = Float.parseFloat(parts[5]);
                    spawnLocations.add(new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch));
                } catch (Exception e) {
                    plugin.getLogger().warning("Erreur au chargement d'un spawn: " + s);
                }
            }
        }
        plugin.getLogger().info("Chargé " + spawnLocations.size() + " points de spawn FFA.");
    }

    public void addSpawn(Location loc) {
        spawnLocations.add(loc);

        // On remet la logique de sauvegarde directement ici
        List<String> stringList = new ArrayList<>();
        for (Location location : spawnLocations) {
            String locString = location.getWorld().getName() + ";"
                    + String.valueOf(location.getX()) + ";"
                    + String.valueOf(location.getY()) + ";"
                    + String.valueOf(location.getZ()) + ";"
                    + String.valueOf(location.getYaw()) + ";"
                    + String.valueOf(location.getPitch());
            stringList.add(locString.replace(',', '.'));
        }

        config.set("spawn-locations", stringList);
        saveConfig();
    }

    public List<Location> getSpawnLocations() {
        return spawnLocations;
    }
}