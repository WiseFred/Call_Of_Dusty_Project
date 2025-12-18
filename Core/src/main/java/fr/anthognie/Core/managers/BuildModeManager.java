package fr.anthognie.Core.managers;

import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BuildModeManager {

    private final Set<UUID> buildModeAdmins = new HashSet<>();

    /**
     * Active ou désactive le mode build pour un admin.
     * @return Le nouvel état (true = activé)
     */
    public boolean toggleBuildMode(Player admin) {
        UUID uuid = admin.getUniqueId();
        if (buildModeAdmins.contains(uuid)) {
            buildModeAdmins.remove(uuid);
            return false;
        } else {
            buildModeAdmins.add(uuid);
            return true;
        }
    }

    /**
     * Vérifie si un admin est en mode build.
     */
    public boolean isInBuildMode(Player player) {
        // Le joueur doit être OP ET dans la liste
        return player.isOp() && buildModeAdmins.contains(player.getUniqueId());
    }

    /**
     * Retire un joueur de la liste (ex: à la déconnexion).
     */
    public void removeFromBuildMode(Player player) {
        buildModeAdmins.remove(player.getUniqueId());
    }
}