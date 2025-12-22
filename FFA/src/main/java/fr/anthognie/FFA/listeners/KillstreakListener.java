package fr.anthognie.FFA.listeners;

import fr.anthognie.FFA.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class KillstreakListener implements Listener {

    private final Main plugin;

    public KillstreakListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Vérification de l'item (Os nommé "APPEL DES CHIENS")
        if (item == null || item.getType() != Material.BONE) return;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
        if (!item.getItemMeta().getDisplayName().contains("APPEL DES CHIENS")) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);

            // On retire l'os (1 seul)
            if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
            else player.getInventory().setItemInMainHand(null);

            player.sendMessage("§aLes chiens ont été lâchés !");
            player.playSound(player.getLocation(), Sound.ENTITY_WOLF_HOWL, 10f, 1f);

            // Recherche de la cible la plus proche (hors lanceur)
            Player nearestTarget = null;
            List<Entity> nearby = player.getNearbyEntities(50, 50, 50).stream()
                    .filter(e -> e instanceof Player)
                    .filter(e -> !e.getUniqueId().equals(player.getUniqueId())) // Pas le lanceur
                    .filter(e -> !((Player)e).getGameMode().name().contains("SPECTATOR")) // Pas les spectateurs
                    .sorted(Comparator.comparingDouble(e -> e.getLocation().distance(player.getLocation())))
                    .collect(Collectors.toList());

            if (!nearby.isEmpty()) {
                nearestTarget = (Player) nearby.get(0);
                player.sendMessage("§eVos chiens traquent : §c" + nearestTarget.getName() + " (" + (int)player.getLocation().distance(nearestTarget.getLocation()) + "m)");
                nearestTarget.sendMessage("§c§lATTENTION ! §4Les chiens de " + player.getName() + " vous traquent !");
            } else {
                player.sendMessage("§7Aucune cible proche, les chiens vous défendront.");
            }

            // Spawn des 3 Chiens
            for (int i = 0; i < 3; i++) {
                Wolf wolf = player.getWorld().spawn(player.getLocation(), Wolf.class);

                // Propriétés
                wolf.setTamed(true);
                wolf.setOwner(player);
                wolf.setAdult();
                wolf.setCustomName(ChatColor.RED + "Chien de " + player.getName());
                wolf.setCustomNameVisible(true);

                // Résistance accrue (40 PV au lieu de 20 + Resistance)
                if (wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                    wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0);
                }
                wolf.setHealth(40.0);
                // Petit effet de vitesse pour qu'ils soient effrayants
                wolf.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1200, 1));

                // Attaque
                if (nearestTarget != null) {
                    wolf.setTarget(nearestTarget);
                    wolf.setAngry(true);
                }
            }
        }
    }
}