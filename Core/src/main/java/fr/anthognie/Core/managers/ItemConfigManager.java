package fr.anthognie.Core.managers;

import fr.anthognie.Core.Main;
import fr.anthognie.Core.utils.InventorySerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class ItemConfigManager {

    private final Main plugin;
    private File file;
    private FileConfiguration config;

    public ItemConfigManager(Main plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        file = new File(plugin.getDataFolder(), "items.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Sauvegarde l'item avec le nouveau Serializer (NBT Base64)
    public void saveItem(String path, ItemStack item) {
        String base64 = InventorySerializer.singleItemToBase64(item);
        config.set(path, base64);
        saveConfig();
    }

    // Récupère l'item
    public ItemStack getItemStack(String path) {
        if (!config.contains(path)) return null;
        String data = config.getString(path);
        return InventorySerializer.singleItemFromBase64(data);
    }

    public void deleteItem(String path) {
        config.set(path, null);
        saveConfig();
    }

    public Set<String> getItemKeys() {
        return config.getKeys(true); // true pour la profondeur, ou false pour juste les racines
    }

    // Helper pour récupérer la liste à plat pour le GUI (ex: kits.ffa.pistol)
    public ConfigurationSection getConfig() {
        return config;
    }
}