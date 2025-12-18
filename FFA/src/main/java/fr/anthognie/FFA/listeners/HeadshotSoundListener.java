package fr.anthognie.FFA.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import fr.anthognie.FFA.Main;
import fr.anthognie.FFA.game.FFAManager;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public class HeadshotSoundListener {

    private final Main plugin;
    private final FFAManager ffaManager;
    private final String headshotSoundID;

    public HeadshotSoundListener(Main plugin) {
        this.plugin = plugin;
        this.ffaManager = plugin.getFfaManager();
        this.headshotSoundID = plugin.getFfaConfigManager().getConfig().getString("headshot-bonus.sound-id", "cgm:sound.headshot");

        registerPacketListener();
    }

    private void registerPacketListener() {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        protocolManager.addPacketListener(
                new PacketAdapter(plugin, PacketType.Play.Server.NAMED_SOUND_EFFECT) {

                    @Override
                    public void onPacketSending(PacketEvent event) {
                        Player player = event.getPlayer();

                        String soundName = event.getPacket().getSoundEffects().read(0).getKey().toString();

                        if (event.getPacket().getSoundCategories().read(0).name().equals(SoundCategory.MASTER.name()) &&
                                soundName.equals(headshotSoundID)) {

                            ffaManager.recordHeadshot(player);
                        }
                    }
                }
        );
    }
}