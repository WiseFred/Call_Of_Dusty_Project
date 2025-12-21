package fr.anthognie.FFA.managers;

import fr.anthognie.FFA.Main;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class DataManager {

    private final Main plugin;
    private final File dataFolder;

    public DataManager(Main plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public void savePlayer(Player player) {
        File file = new File(dataFolder, player.getUniqueId() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Sauvegarde XP
        config.set("xp", plugin.getLevelManager().getTotalXp(player));

        // Sauvegarde Stats Kills/Morts
        config.set("kills", plugin.getKillstreakManager().getSessionKills(player));
        config.set("deaths", plugin.getKillstreakManager().getDeaths(player));

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder les données pour " + player.getName());
            e.printStackTrace();
        }
    }

    public void loadPlayer(Player player) {
        File file = new File(dataFolder, player.getUniqueId() + ".yml");
        if (!file.exists()) return; // Nouveau joueur = rien à charger

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Chargement XP
        int xp = config.getInt("xp", 0);
        plugin.getLevelManager().setTotalXp(player, xp);

        // Chargement Stats
        int kills = config.getInt("kills", 0);
        int deaths = config.getInt("deaths", 0);

        plugin.getKillstreakManager().setTotalKills(player, kills);
        plugin.getKillstreakManager().setTotalDeaths(player, deaths);
    }

    // Sauvegarde de tout le monde (utile pour le /stop)
    public void saveAllOnline() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            savePlayer(p);
        }
    }
}