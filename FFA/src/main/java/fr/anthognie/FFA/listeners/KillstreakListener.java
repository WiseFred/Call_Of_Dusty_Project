package fr.anthognie.FFA.listeners;

import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.game.FFAManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class KillstreakListener implements Listener {

    private final Main plugin;
    private final FFAManager ffaManager;

    public KillstreakListener(Main plugin) {
        this.plugin = plugin;
        this.ffaManager = plugin.getFfaManager();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        // Vérification Monde FFA
        if (!player.getWorld().getName().equals(ffaManager.getFFAWorldName())) return;

        String name = item.getItemMeta().getDisplayName();

        // --- ACTIVATION DRÔNE (UAV) ---
        if (name.equals("§e§lDRÔNE DE RECONNAISSANCE") && item.getType() == Material.RECOVERY_COMPASS) {
            event.setCancelled(true);
            consumeItem(player, item);

            player.sendMessage("§aDrône activé ! Radar en ligne pour 30s.");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 0.5f);

            // Appliquer l'effet Glowing aux ennemis
            for (Player enemy : player.getWorld().getPlayers()) {
                if (enemy != player && !ffaManager.isInvincible(enemy)) {
                    enemy.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 0, false, false, false)); // 30s = 600 ticks
                    enemy.sendMessage("§c§lALERTE ! §cVotre position est révélée par un Drône ennemi !");
                }
            }
        }

        // --- ACTIVATION NUKE TACTIQUE ---
        if (name.equals("§4§l☢ NUKE TACTIQUE ☢") && item.getType() == Material.NETHER_STAR) {
            event.setCancelled(true);
            consumeItem(player, item);
            launchNukeSequence(player);
        }
    }

    private void consumeItem(Player player, ItemStack item) {
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().remove(item);
        }
    }

    private void launchNukeSequence(Player launcher) {
        // Phase 1 : Alarme
        Bukkit.broadcastMessage("§4§l☢ NUKE TACTIQUE EN APPROCHE ! ☢");
        Bukkit.broadcastMessage("§cC'est la fin ! Lancée par " + launcher.getName());

        for (Player p : launcher.getWorld().getPlayers()) {
            // Son d'alarme grave (Wither Spawn répété ou Note Block)
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2f, 0.5f);
            p.sendTitle("§4§lNUKE INCOMING", "§cImpact dans 5 secondes...", 0, 100, 20);
        }

        // Phase 2 : Compte à rebours sonore (T+0 à T+4)
        new BukkitRunnable() {
            int count = 5;
            @Override
            public void run() {
                if (count > 0) {
                    for (Player p : launcher.getWorld().getPlayers()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
                    }
                    count--;
                } else {
                    this.cancel();
                    detonateNuke(launcher);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void detonateNuke(Player launcher) {
        // Phase 3 : EXPLOSION & FLASHBANG
        for (Player p : launcher.getWorld().getPlayers()) {
            // Son d'explosion massif
            p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 10f, 0.5f);

            // Son strident (Tinnitus)
            p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 2f, 2f); // Son très aigu

            if (p != launcher) {
                // Effet Flashbang (Aveuglement total + Nausée + Lenteur pour figer)
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1, false, false, false));
                p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 1, false, false, false)); // Nausée
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 10, false, false, false)); // Figé

                // Effet d'éjection (Knockback violent vers le haut/arrière)
                Vector away = p.getLocation().toVector().subtract(launcher.getLocation().toVector()).normalize();
                // Si le vecteur est nul (même position), on propulse juste en haut
                if (Double.isNaN(away.getX())) away = new Vector(0, 1, 0);

                p.setVelocity(away.multiply(2).setY(1.5)); // Boom !
            }
        }

        // Phase 4 : MORT GÉNÉRALE (2 secondes après le flash)
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : launcher.getWorld().getPlayers()) {
                    if (p != launcher && !ffaManager.isInvincible(p)) {
                        // On tue le joueur proprement via le FFAManager pour compter les points si besoin
                        // Ou damage(1000) pour être sûr
                        p.setHealth(0);
                    }
                }
                Bukkit.broadcastMessage("§4§lBOOM ! §cLa Nuke a tout rasé.");
            }
        }.runTaskLater(plugin, 40L); // 2 secondes après le flash
    }
}