package fr.anthognie.Core.managers;

import fr.anthognie.Core.Main;
import fr.anthognie.Core.utils.InventorySerializer; // <-- NOUVEL IMPORT
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack; // <-- NOUVEL IMPORT

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {

    private final Main plugin;
    private Connection connection;
    private final Map<UUID, Integer> moneyCache = new HashMap<>();

    public EconomyManager(Main plugin) {
        this.plugin = plugin;
    }

    // --- 1. GESTION DE LA BASE DE DONNÉES ---

    public void initializeDatabase() {
        File dbFile = new File(plugin.getDataFolder(), "data.db");
        if (!dbFile.exists()) {
            try {
                dbFile.getParentFile().mkdirs();
                dbFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Impossible de créer le fichier data.db !");
                e.printStackTrace();
            }
        }
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            createTable();
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().severe("Impossible de se connecter à la base de données SQLite !");
            e.printStackTrace();
        }
    }

    private void createTable() throws SQLException {
        // --- MISE À JOUR DE LA TABLE ---
        String sql = "CREATE TABLE IF NOT EXISTS player_data ("
                + "uuid TEXT PRIMARY KEY NOT NULL,"
                + "money INTEGER DEFAULT 0,"
                + "ffa_inventory TEXT"
                + ");";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
        }
    }

    public void closeDatabase() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- 2. GESTION DU JOUEUR (CACHE + DB) ---
    public void loadPlayerAccount(UUID playerUUID) {
        String sql = "SELECT money FROM player_data WHERE uuid = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                moneyCache.put(playerUUID, rs.getInt("money"));
            } else {
                moneyCache.put(playerUUID, 0);
                savePlayerData(playerUUID, 0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void saveAndUnloadPlayer(UUID playerUUID) {
        if (!moneyCache.containsKey(playerUUID)) {
            return;
        }
        int money = moneyCache.get(playerUUID);
        savePlayerData(playerUUID, money);
        moneyCache.remove(playerUUID);
    }
    private void savePlayerData(UUID playerUUID, int money) {
        String sql = "INSERT INTO player_data (uuid, money) VALUES (?, ?) "
                + "ON CONFLICT(uuid) DO UPDATE SET money = excluded.money;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setInt(2, money);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void saveAllData() {
        plugin.getLogger().info("Sauvegarde des données de tous les joueurs en ligne...");
        for (Player player : Bukkit.getOnlinePlayers()) {
            saveAndUnloadPlayer(player.getUniqueId());
        }
        plugin.getLogger().info("Sauvegarde terminée.");
    }
    public int getMoney(UUID playerUUID) {
        return moneyCache.getOrDefault(playerUUID, 0);
    }
    public void setMoney(UUID playerUUID, int amount) {
        if (amount < 0) amount = 0;
        moneyCache.put(playerUUID, amount);
    }
    public void addMoney(UUID playerUUID, int amount) {
        setMoney(playerUUID, getMoney(playerUUID) + amount);
    }
    public void removeMoney(UUID playerUUID, int amount) {
        setMoney(playerUUID, getMoney(playerUUID) - amount);
    }

    // --- 3. NOUVELLES MÉTHODES DE GESTION D'INVENTAIRE ---

    /**
     * Sauvegarde l'inventaire d'un joueur dans la DB (en Base64).
     */
    public void saveInventory(UUID playerUUID, ItemStack[] inventoryContents) {
        String base64Inventory = InventorySerializer.itemStackArrayToBase64(inventoryContents);
        String sql = "UPDATE player_data SET ffa_inventory = ? WHERE uuid = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, base64Inventory);
            stmt.setString(2, playerUUID.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Charge l'inventaire d'un joueur depuis la DB.
     * @return L'inventaire, ou null s'il n'y en a pas.
     */
    public ItemStack[] loadInventory(UUID playerUUID) {
        String sql = "SELECT ffa_inventory FROM player_data WHERE uuid = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String base64Inventory = rs.getString("ffa_inventory");
                if (base64Inventory != null && !base64Inventory.isEmpty()) {
                    return InventorySerializer.itemStackArrayFromBase64(base64Inventory);
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return null; // Pas d'inventaire sauvegardé
    }

    /**
     * Efface l'inventaire sauvegardé (utilisé après la mort).
     */
    public void clearInventory(UUID playerUUID) {
        String sql = "UPDATE player_data SET ffa_inventory = NULL WHERE uuid = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}