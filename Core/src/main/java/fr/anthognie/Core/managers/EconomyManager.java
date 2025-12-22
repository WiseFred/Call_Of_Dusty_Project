package fr.anthognie.Core.managers;

import fr.anthognie.Core.Main;
import fr.anthognie.Core.utils.InventorySerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {

    private final Main plugin;
    private final File dataFolder;
    private final Map<UUID, Integer> balances = new HashMap<>();

    public EconomyManager(Main plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    // --- ARGENT ---

    public int getMoney(UUID uuid) {
        if (!balances.containsKey(uuid)) {
            loadPlayer(uuid);
        }
        return balances.getOrDefault(uuid, 0);
    }

    public void addMoney(UUID uuid, int amount) {
        setMoney(uuid, getMoney(uuid) + amount);
    }

    public void removeMoney(UUID uuid, int amount) {
        int current = getMoney(uuid);
        setMoney(uuid, Math.max(0, current - amount));
    }

    public void setMoney(UUID uuid, int amount) {
        balances.put(uuid, amount);
        savePlayer(uuid);
    }

    public boolean hasMoney(UUID uuid, int amount) {
        return getMoney(uuid) >= amount;
    }

    // --- SAUVEGARDE & CHARGEMENT ---

    public void loadPlayer(UUID uuid) {
        File file = new File(dataFolder, uuid + ".yml");
        if (file.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            balances.put(uuid, config.getInt("money", 0));
        } else {
            balances.put(uuid, 0);
        }
    }

    public void savePlayer(UUID uuid) {
        File file = new File(dataFolder, uuid + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("money", balances.getOrDefault(uuid, 0));
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Pour Main.java onDisable
    public void saveAllData() {
        for (UUID uuid : balances.keySet()) {
            savePlayer(uuid);
        }
    }

    // --- INVENTAIRES ---

    public void saveInventory(UUID uuid, ItemStack[] contents) {
        File file = new File(dataFolder, uuid + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        String serialized = InventorySerializer.toBase64(contents);
        config.set("inventory", serialized);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ItemStack[] loadInventory(UUID uuid) {
        File file = new File(dataFolder, uuid + ".yml");
        if (!file.exists()) return null;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config.contains("inventory")) {
            try {
                return InventorySerializer.fromBase64(config.getString("inventory"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void clearInventory(UUID uuid) {
        File file = new File(dataFolder, uuid + ".yml");
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("inventory", null);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}