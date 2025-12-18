package fr.anthognie.airdrops.managers;

import fr.anthognie.Core.managers.ItemConfigManager;
import fr.anthognie.airdrops.Main;
import fr.anthognie.Core.utils.InventorySerializer;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LootManager {

    private final Main plugin;
    private final ItemConfigManager itemConfigManager;
    private final Random random = new Random();
    private FileConfiguration lootConfig;
    private File configFile;

    public LootManager(Main plugin) {
        this.plugin = plugin;
        this.itemConfigManager = plugin.getItemConfigManager();
        loadLootConfig();
    }

    public void loadLootConfig() {
        this.configFile = new File(plugin.getDataFolder(), "loot.yml");
        if (!configFile.exists()) {
            plugin.saveResource("loot.yml", false);
        }
        this.lootConfig = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        return lootConfig;
    }

    public void saveConfig() {
        try {
            lootConfig.save(configFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Impossible de sauver loot.yml!");
        }
    }

    public void fillChest(Chest chest, boolean isUltimate) {
        Inventory inv = chest.getInventory();
        inv.clear();

        String type = isUltimate ? "ultimate" : "normal";
        ConfigurationSection kitsSection = lootConfig.getConfigurationSection(type + ".kits");
        if (kitsSection == null) return;

        if (isUltimate) {
            // --- LOGIQUE ULTIME : On teste la chance pour CHAQUE kit ---
            for (String key : kitsSection.getKeys(false)) {
                int kitChance = kitsSection.getInt(key + ".chance", 100);
                if (random.nextInt(100) < kitChance) {
                    addKitToInventory(inv, type, key);
                }
            }
        } else {
            // --- LOGIQUE NORMALE : On choisit UN SEUL kit ---
            int totalWeight = 0;
            Map<String, Integer> kitWeights = new HashMap<>();
            for (String key : kitsSection.getKeys(false)) {
                int weight = kitsSection.getInt(key + ".chance", 0);
                totalWeight += weight;
                kitWeights.put(key, weight);
            }
            if (totalWeight == 0) return;

            int roll = random.nextInt(totalWeight);
            int cumulative = 0;
            String chosenKitKey = null;

            for (Map.Entry<String, Integer> entry : kitWeights.entrySet()) {
                cumulative += entry.getValue();
                if (roll < cumulative) {
                    chosenKitKey = entry.getKey();
                    break;
                }
            }

            if (chosenKitKey != null) {
                addKitToInventory(inv, type, chosenKitKey);
            }
        }
    }

    /**
     * Récupère un kit depuis le items.yml (du Core) et l'ajoute au coffre.
     */
    private void addKitToInventory(Inventory inv, String type, String kitName) {
        String itemPath = "airdrops.loot." + type + "." + kitName;

        String base64data = itemConfigManager.getConfig().getString(itemPath);
        if (base64data == null || base64data.isEmpty()) {
            plugin.getLogger().warning("Contenu du kit '" + kitName + "' introuvable ! (Path: " + itemPath + ")");
            return;
        }

        try {
            ItemStack[] items = InventorySerializer.itemStackArrayFromBase64(base64data);

            for (ItemStack item : items) {
                if (item != null && item.getType() != Material.AIR) {
                    if (inv.firstEmpty() != -1) {
                        inv.setItem(random.nextInt(inv.getSize()), item);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Erreur lors du décodage du kit: " + kitName);
            e.printStackTrace();
        }
    }
}