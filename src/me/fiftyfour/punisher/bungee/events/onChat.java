package me.fiftyfour.punisher.bungee.events;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.chats.StaffChat;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class onChat implements Listener {
    private BungeeMain plugin = BungeeMain.getInstance();
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;

    @EventHandler
    public void onPlayerChat(ChatEvent event) {
        UUID uuid;
        String fetcheduuid;
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        ServerInfo server = player.getServer().getInfo();
        if (onPluginMessage.chatOffServers.contains(server)){
            if(!player.hasPermission("punisher.togglechat.bypass")){
                if (event.isCommand()){
                    String[] args = event.getMessage().split(" ");
                    List<String> mutedcommands;
                    mutedcommands = BungeeMain.PunisherConfig.getStringList("Muted Commands");
                    if (mutedcommands.contains(args[0])) {
                        event.setCancelled(true);
                        player.sendMessage(new ComponentBuilder(prefix).append("You may not use that command at this time!").color(ChatColor.RED).create());
                    }else{
                        event.setCancelled(false);
                    }
                    return;
                }
                event.setCancelled(true);
                player.sendMessage(new ComponentBuilder(prefix).append("You may not chat at this time!").color(ChatColor.RED).create());
                return;
            }else {
                event.setCancelled(false);
                return;
            }
        }
        uuid = player.getUniqueId();
        fetcheduuid = uuid.toString().replace("-", "");
        try {
            String sql = "SELECT * FROM `mutes` WHERE UUID='" + fetcheduuid + "'";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                if (player.hasPermission("punisher.bypass")) {
                    String sql1 = "DELETE FROM `mutes` WHERE `UUID`='" + fetcheduuid + "' ;";
                    PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                    stmt1.executeUpdate();
                    BungeeMain.Logs.info(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + player.getName() + " Bypassed mute");
                    StaffChat.sendMessage(player.getName() + " Bypassed their mute, Unmuting...");
                    return;
                }
                Long mutetime = results.getLong("Length");
                Long muteleftmillis = mutetime - System.currentTimeMillis();
                long daysleft = muteleftmillis / (1000 * 60 * 60 * 24);
                long hoursleft = (long) Math.floor(muteleftmillis / (1000 * 60 * 60) % 24);
                long minutesleft = (long) Math.floor(muteleftmillis / (1000 * 60) % 60);
                long secondsleft = (long) Math.floor(muteleftmillis / 1000 % 60);
                if (System.currentTimeMillis() > mutetime) {
                    String sql1 = "DELETE FROM `mutes` WHERE `UUID`='" + fetcheduuid + "' ;";
                    PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                    stmt1.executeUpdate();
                    player.sendMessage(new ComponentBuilder(prefix).append("Your Mute has expired!").color(ChatColor.GREEN).create());
                    BungeeMain.Logs.info(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + player.getName() + "'s mute expired so was unmuted");
                } else {
                    if (event.isCommand()) {
                        String[] args = event.getMessage().split(" ");
                        List<String> mutedcommands;
                        mutedcommands = BungeeMain.PunisherConfig.getStringList("Muted Commands");
                        if (mutedcommands.contains(args[0])) {
                            event.setCancelled(true);
                            player.sendMessage(new TextComponent("\n"));
                            player.sendMessage(new ComponentBuilder(prefix).append("You may not use that command!").color(ChatColor.RED).create());
                            player.sendMessage(new ComponentBuilder(prefix).append("You are currently Muted! Reason: " + results.getString("Reason")).color(ChatColor.RED).create());
                            if (daysleft > 500) {
                                player.sendMessage(new ComponentBuilder(prefix).append("This mute is permanent and does not expire").color(ChatColor.RED).create());
                                player.sendMessage(new TextComponent("\n"));

                            } else {
                                player.sendMessage(new ComponentBuilder(prefix).append("This mute expires in: " + daysleft + "d " + hoursleft + "hr " + minutesleft + "m " + secondsleft + "s").color(ChatColor.RED).create());
                                player.sendMessage(new TextComponent("\n"));
                            }
                        }
                    }else {
                        event.setCancelled(true);
                        player.sendMessage(new TextComponent("\n"));
                        player.sendMessage(new ComponentBuilder(prefix).append("You may not talk in global chat!").color(ChatColor.RED).create());
                        player.sendMessage(new ComponentBuilder(prefix).append("You are currently Muted! Reason: " + results.getString("Reason")).color(ChatColor.RED).create());
                        if (daysleft > 500) {
                            player.sendMessage(new ComponentBuilder(prefix).append("This mute is permanent and does not expire!").color(ChatColor.RED).create());
                            player.sendMessage(new TextComponent("\n"));
                        } else {
                            player.sendMessage(new ComponentBuilder(prefix).append("This mute expires in: " + daysleft + "d " + hoursleft + "hr " + minutesleft + "m " + secondsleft + "s").color(ChatColor.RED).create());
                            player.sendMessage(new TextComponent("\n"));
                        }
                        StaffChat.sendMessage(player.getName() + " Tried to talk but is muted: " + event.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            plugin.mysqlfail(e);
            if(plugin.testConnectionManual())
            plugin.getProxy().getPluginManager().callEvent(new ChatEvent(event.getSender(), event.getReceiver(), event.getMessage()));
        }
    }
}