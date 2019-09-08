package me.fiftyfour.punisher.bungee.listeners;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.chats.StaffChat;
import me.fiftyfour.punisher.bungee.handlers.ErrorHandler;
import me.fiftyfour.punisher.bungee.managers.PunishmentManager;
import me.fiftyfour.punisher.bungee.objects.Punishment;
import me.fiftyfour.punisher.bungee.systems.ReputationSystem;
import me.fiftyfour.punisher.universal.exceptions.PunishmentIssueException;
import me.fiftyfour.punisher.universal.fetchers.NameFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;

public class PluginMessage implements Listener {

    static ArrayList<ServerInfo> chatOffServers = new ArrayList<>();
    private PunishmentManager punishmngr = PunishmentManager.getInstance();
    private int sqlfails = 0;
    private BungeeMain plugin = BungeeMain.getInstance();

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        if (e.getTag().equalsIgnoreCase("punisher:main")) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
            try {
                Connection sender = e.getReceiver();
                String chatState, action, uuid, name, reasonString;
                action = in.readUTF();
                switch (action) {
                    case "chatToggled": {
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
                        return;
                    }
                    case "getrep": {
                        uuid = in.readUTF();
                        String rep = ReputationSystem.getRep(uuid);
                        if (rep == null) {
                            rep = "5.0";
                        }
                        name = in.readUTF();
                        ProxiedPlayer player = (ProxiedPlayer) e.getReceiver();
                        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
                        DataOutputStream out = new DataOutputStream(bytestream);
                        out.writeUTF("rep");
                        out.writeUTF(rep);
                        out.writeUTF(uuid);
                        out.writeUTF(name);
                        player.getServer().sendData("punisher:main", bytestream.toByteArray());
                        return;
                    }
                    case "punish": {
                        name = in.readUTF();
                        uuid = in.readUTF().replace("-", "");
                        reasonString = in.readUTF();
                        Punishment punishment = null;
                        ProxiedPlayer punisher = (ProxiedPlayer) sender;
                        try {
                            if (sqlfails < 5) {
                                Punishment.Type type;
                                Punishment.Reason reason = Punishment.Reason.valueOf(reasonString);
                                String message;
                                if (reasonString.contains("Manual")) {
                                    String typeString = in.readUTF();
                                    type = Punishment.Type.valueOf(typeString);
                                    message = in.readUTF();
                                } else if (reasonString.contains("Other")) {
                                    type = Punishment.Type.BAN;
                                    message = in.readUTF();
                                } else {
                                    type = punishmngr.calculateType(uuid, reason);
                                    message = in.readUTF();
                                }
                                punishment = new Punishment(reason, message, null, type, uuid, punisher.getUniqueId().toString().replace("-", ""));
                                punishmngr.issue(punishment, punisher, name, true, true, true);
                                punisher.sendMessage(new ComponentBuilder(plugin.prefix).append("Punishing " + NameFetcher.getName(uuid) + " for: " + message).color(ChatColor.GREEN).create());
                            } else
                                punisher.sendMessage(new ComponentBuilder(plugin.prefix).append("The Punisher Gui is currently not functioning correctly please inform an admin+ asap!!").color(ChatColor.RED).create());
                        } catch (SQLException sqle) {
                            try {
                                throw new PunishmentIssueException("Automatic punishment was unable to issue due to an sql exception", punishment, sqle);
                            } catch (PunishmentIssueException pie) {
                                ErrorHandler errorHandler = ErrorHandler.getInstance();
                                errorHandler.log(pie);
                                errorHandler.alert(pie, punisher);
                            }
                            sqlfails++;
                            if (plugin.testConnectionManual()) {
                                this.onPluginMessage(new PluginMessageEvent(e.getSender(), e.getReceiver(), e.getTag(), e.getData()));
                            }
                        }
                    }
                }
            } catch (IOException IO) {
                IO.printStackTrace();
            }
        } else if (e.getTag().equals("punisher:minor")) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
            try {
                String action, uuid;
                action = in.readUTF();
                if (action.equals("getrepcache")) {
                    uuid = in.readUTF();
                    String rep = ReputationSystem.getRep(uuid);
                    if (rep == null) {
                        rep = "5.0";
                    }
                    ProxiedPlayer player = (ProxiedPlayer) e.getReceiver();
                    ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(bytestream);
                    out.writeUTF("repcache");
                    out.writeUTF(rep);
                    out.writeUTF(uuid);
                    player.getServer().sendData("punisher:minor", bytestream.toByteArray());
                } else if (action.equals("log")) {
                    Level level = Level.parse(in.readUTF());
                    BungeeMain.Logs.log(level, in.readUTF());
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
