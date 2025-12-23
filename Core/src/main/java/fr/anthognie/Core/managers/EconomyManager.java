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
    private final Map<UUID, Double> balances = new HashMap<>(); // Changed to Double for precision, usually better for money

    public EconomyManager(Main plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    // --- MISSING METHODS ADDED ---

    public boolean hasAccount(UUID uuid) {
        // Checks if player is in memory or if file exists
        return balances.containsKey(uuid) || new File(dataFolder, uuid + ".yml").exists();
    }

    public void createAccount(UUID uuid) {
        if (!hasAccount(uuid)) {
            setMoney(uuid, 0); // Default starting money
        }
    }

    // --- MONEY MANAGEMENT ---

    public double getMoney(UUID uuid) {
        if (!balances.containsKey(uuid)) {
            loadPlayer(uuid);
        }
        return balances.getOrDefault(uuid, 0.0);
    }

    public void addMoney(UUID uuid, double amount) {
        setMoney(uuid, getMoney(uuid) + amount);
    }

    public void removeMoney(UUID uuid, double amount) {
        double current = getMoney(uuid);
        setMoney(uuid, Math.max(0, current - amount));
    }

    public void setMoney(UUID uuid, double amount) {
        balances.put(uuid, amount);
        savePlayer(uuid);
    }

    public boolean hasMoney(UUID uuid, double amount) {
        return getMoney(uuid) >= amount;
    }

    // --- SAVE & LOAD ---

    public void loadPlayer(UUID uuid) {
        File file = new File(dataFolder, uuid + ".yml");
        if (file.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            balances.put(uuid, config.getDouble("money", 0));
        } else {
            balances.put(uuid, 0.0);
        }
    }

    public void savePlayer(UUID uuid) {
        File file = new File(dataFolder, uuid + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("money", balances.getOrDefault(uuid, 0.0));
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveAllData() {
        for (UUID uuid : balances.keySet()) {
            savePlayer(uuid);
        }
    }

    // --- INVENTORY MANAGEMENT ---

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