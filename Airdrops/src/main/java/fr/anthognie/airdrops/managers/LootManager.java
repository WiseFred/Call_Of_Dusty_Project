package fr.anthognie.airdrops.managers;

import fr.anthognie.airdrops.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Chest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LootManager {

    private final Main plugin;
    private File lootFile;
    private FileConfiguration lootConfig;
    private final Random random = new Random();

    public LootManager(Main plugin) {
        this.plugin = plugin;
        createLootConfig();
    }

    private void createLootConfig() {
        lootFile = new File(plugin.getDataFolder(), "loot.yml");
        if (!lootFile.exists()) {
            lootFile.getParentFile().mkdirs();
            plugin.saveResource("loot.yml", false);
        }
        lootConfig = YamlConfiguration.loadConfiguration(lootFile);
    }

    public FileConfiguration getLootConfig() {
        if (lootConfig == null) {
            createLootConfig();
        }
        return lootConfig;
    }

    public void saveLootConfig() {
        try {
            getLootConfig().save(lootFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- ALIASES FOR COMPATIBILITY ---
    public FileConfiguration getConfig() {
        return getLootConfig();
    }

    public void saveConfig() {
        saveLootConfig();
    }
    // ---------------------------------

    public void fillChest(Chest chest, boolean isUltimate) {
        chest.getInventory().clear();
        String typePath = isUltimate ? "ultimate" : "normal";
        ConfigurationSection section = getLootConfig().getConfigurationSection("loots." + typePath);

        if (section == null) return;

        List<String> keys = new ArrayList<>(section.getKeys(false));
        if (keys.isEmpty()) return;

        // Random fill
        int itemsToAdd = random.nextInt(3) + 3; // 3 to 5 items
        for (int i = 0; i < itemsToAdd; i++) {
            String randomKey = keys.get(random.nextInt(keys.size()));
            String path = "loots." + typePath + "." + randomKey;

            int chance = getLootConfig().getInt(path + ".chance");
            if (random.nextInt(100) < chance) {
                ItemStack item = getLootConfig().getItemStack(path + ".item");
                if (item != null) {
                    int min = getLootConfig().getInt(path + ".min");
                    int max = getLootConfig().getInt(path + ".max");
                    int amount = random.nextInt(max - min + 1) + min;
                    item.setAmount(amount);

                    int slot = random.nextInt(chest.getInventory().getSize());
                    chest.getInventory().setItem(slot, item);
                }
            }
        }
    }

    public void addLootItem(ItemStack item, int chance, int min, int max, boolean isUltimate) {
        String typePath = isUltimate ? "ultimate" : "normal";
        String key = "item_" + System.currentTimeMillis();
        String path = "loots." + typePath + "." + key;

        getLootConfig().set(path + ".item", item);
        getLootConfig().set(path + ".chance", chance);
        getLootConfig().set(path + ".min", min);
        getLootConfig().set(path + ".max", max);
        saveLootConfig();
    }

    public void removeLootItem(ItemStack displayItem, boolean isUltimate) {
        String typePath = isUltimate ? "ultimate" : "normal";
        ConfigurationSection section = getLootConfig().getConfigurationSection("loots." + typePath);
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ItemStack item = getLootConfig().getItemStack("loots." + typePath + "." + key + ".item");
            if (item != null && item.isSimilar(displayItem)) {
                getLootConfig().set("loots." + typePath + "." + key, null);
                saveLootConfig();
                return;
            }
        }
    }
}