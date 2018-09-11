package me.fiftyfour.punisher.bungee.events;

import me.fiftyfour.punisher.bungee.chats.StaffChat;
import me.fiftyfour.punisher.systems.ReputationSystem;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.util.ArrayList;

public class onPluginMessage implements Listener {

    static ArrayList<ServerInfo> chatOffServers = new ArrayList<>();
    @EventHandler
    public void PluginMessage(PluginMessageEvent e) {
        if (e.getTag().equalsIgnoreCase("BungeeCord")) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
            try {
                String chatState, action, uuid;
                action = in.readUTF();
                if (action.equals("chatToggled")) {
                    chatState = in.readUTF();
                    ProxiedPlayer toggeler = ProxyServer.getInstance().getPlayer(e.getReceiver().toString());
                    if (chatState.equalsIgnoreCase("on")) {
                        StaffChat.sendMessage(toggeler.getName() + " Has toggled chat: On!");
                        ServerInfo server = toggeler.getServer().getInfo();
                        chatOffServers.remove(server);
                    } else {
                        StaffChat.sendMessage(toggeler.getName() + " Has toggled chat: Off!");
                        ServerInfo server = toggeler.getServer().getInfo();
                        chatOffServers.add(server);
                    }
                }else if (action.equals("getrep")){
                    uuid = in.readUTF();
                    String rep = ReputationSystem.getRep(uuid);
                    if (rep == null){
                        rep = "5.0";
                    }
                    ProxiedPlayer player = (ProxiedPlayer) e.getReceiver();
                    ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(bytestream);
                    out.writeUTF("rep");
                    out.writeUTF(rep);
                    out.writeUTF(uuid);
                    player.getServer().sendData("BungeeCord", bytestream.toByteArray());
                }
            } catch (IOException IO) {
                IO.printStackTrace();
            }
        }
    }
}
