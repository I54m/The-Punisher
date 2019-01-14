package me.fiftyfour.punisher.bukkit.events;

import me.fiftyfour.punisher.bukkit.BukkitMain;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class PluginMessage implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (channel.equals("BungeeCord")) {
            try {
                ByteArrayInputStream inBytes = new ByteArrayInputStream(message);
                DataInputStream in = new DataInputStream(inBytes);
                String subchannel = in.readUTF();
                if (subchannel.equals("Punisher")){
                    String action = in.readUTF();
                    if(action.equals("PlaySound")){
                        String[] soundstrings = in.readUTF().split(":");
                        Sound sound = Sound.valueOf(soundstrings[0]);
                        int volume = Integer.parseInt(soundstrings[1]);
                        float pitch = (float) Double.parseDouble(soundstrings[2]);
                        player.playSound(player.getLocation(), sound, volume, pitch);
                    }else if (action.equals("RepCache")){
                        double rep = Double.parseDouble(in.readUTF());
                        String uuid = in.readUTF();
                        BukkitMain.repCache.put(uuid, rep);
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }



        }
    }
}
