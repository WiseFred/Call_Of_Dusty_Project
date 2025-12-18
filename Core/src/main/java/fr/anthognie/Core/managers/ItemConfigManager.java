package fr.anthognie.Core.managers;

import fr.anthognie.Core.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;

public class ItemConfigManager {

    private final Main plugin;
    private FileConfiguration config;
    private File configFile;

    public ItemConfigManager(Main plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "items.yml");
        if (!configFile.exists()) {
            plugin.saveResource("items.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder le fichier items.yml !");
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void setItemStack(String path, ItemStack item) {
        config.set(path, item);
    }

    public ItemStack getItemStack(String path) {
        return config.getItemStack(path);
    }
}