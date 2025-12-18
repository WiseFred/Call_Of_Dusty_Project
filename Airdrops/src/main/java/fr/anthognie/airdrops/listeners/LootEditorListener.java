package fr.anthognie.airdrops.listeners;

import fr.anthognie.airdrops.Main;
import fr.anthognie.airdrops.commands.AirdropCommand;
import fr.anthognie.Core.managers.ItemConfigManager;
import fr.anthognie.Core.utils.InventorySerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class LootEditorListener implements Listener {

    private final Main plugin;
    private final ItemConfigManager itemConfigManager;

    public LootEditorListener(Main plugin) {
        this.plugin = plugin;
        this.itemConfigManager = plugin.getItemConfigManager();
    }

    @EventHandler
    public void onLootEditorClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();

        if (!title.startsWith(AirdropCommand.LOOT_EDIT_TITLE_PREFIX)) {
            return;
        }

        Player player = (Player) event.getPlayer();

        // Titre = "§c[ÉDITION] Kit: <type>:<kitName>"
        String[] parts = title.replace(AirdropCommand.LOOT_EDIT_TITLE_PREFIX, "").split(":");
        String type = parts[0];
        String kitName = parts[1];

        String itemPath = "airdrops.loot." + type + "." + kitName;

        ItemStack[] contents = event.getInventory().getContents();

        try {
            String base64data = InventorySerializer.itemStackArrayToBase64(contents);

            itemConfigManager.getConfig().set(itemPath, base64data);
            itemConfigManager.saveConfig();

            player.sendMessage("§aContenu du kit '" + kitName + "' sauvegardé !");

        } catch (Exception e) {
            player.sendMessage("§cErreur lors de la sauvegarde du kit. Voir console.");
            e.printStackTrace();
        }
    }
}