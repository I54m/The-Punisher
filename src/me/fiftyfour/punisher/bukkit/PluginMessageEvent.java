package me.fiftyfour.punisher.bukkit;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class PluginMessageEvent implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (channel.equals("BungeeCord")) {
            try {
                ByteArrayInputStream inBytes = new ByteArrayInputStream(message);
                DataInputStream in = new DataInputStream(inBytes);
                String subchannel = in.readUTF();
                if (subchannel.equals("Punisher")){
                    String action = in.readUTF();
                    if(action.equals("PlayPunishSound")){
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 100, (float) 0.5);
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }



        }
    }
}
