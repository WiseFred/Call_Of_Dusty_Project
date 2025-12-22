package fr.anthognie.FFA.commands;

import fr.anthognie.FFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class ScreamerCommand implements CommandExecutor {

    private final Main plugin;

    public ScreamerCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player senderPlayer = (Player) sender;

        if (!senderPlayer.hasPermission("callofdusty.secret") && !senderPlayer.isOp()) return true;

        World ffaWorld = Bukkit.getWorld(plugin.getFfaManager().getFFAWorldName());
        if (ffaWorld == null) return true;

        senderPlayer.sendMessage("§7[Secret] §4Lancement du protocole PEUR.");

        for (Player p : ffaWorld.getPlayers()) {
            // 1. CÉCITÉ IMMÉDIATE (Noir total)
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 10, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 10, false, false)); // Paralysie
            p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 10, false, false)); // Nausée violente
        }

        // Séquence de 3 secondes
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= 10) { this.cancel(); return; } // 10 fois très vite (tous les 2 ticks)

                for (Player p : ffaWorld.getPlayers()) {
                    // JUMPSCARE VISUEL (Elder Guardian Face)
                    p.getWorld().spawnParticle(Particle.MOB_APPEARANCE, p.getLocation(), 1);

                    // MIX DE SONS HORRIBLES
                    // Son strident aigu (Enderman + Glass)
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 10f, 2.0f);
                    p.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 10f, 2.0f);

                    // Son lourd et grave (Ghast + Explosion)
                    p.playSound(p.getLocation(), Sound.ENTITY_GHAST_SCREAM, 10f, 0.5f);
                    p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 10f, 0.5f);

                    // TITRE ROUGE SANG
                    if (count % 2 == 0) {
                        p.sendTitle("§4§k||| §4§lDANGER §4§k|||", "§cIL EST DERRIÈRE TOI", 0, 5, 0);
                    } else {
                        p.sendTitle("", "", 0, 1, 0); // Flash noir
                    }
                }
                count++;
            }
        }.runTaskTimer(plugin, 5L, 3L); // Commence après 0.25s, répète toutes les 0.15s

        return true;
    }
}