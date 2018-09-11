package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.chats.StaffChat;
import me.fiftyfour.punisher.fetchers.NameFetcher;
import me.fiftyfour.punisher.fetchers.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UnmuteCommand extends Command {
    private BungeeMain plugin = BungeeMain.getInstance();
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;

    public UnmuteCommand() {
        super("unmute", "punisher.unmute");
    }
    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            if (strings.length == 0) {
                player.sendMessage(new ComponentBuilder(prefix).append("Unmute a player").color(ChatColor.RED).append("\nUsage: /unmute <player name>").color(ChatColor.WHITE).create());
                return;
            }
            String targetuuid = UUIDFetcher.getUUID(strings[0]);
            if (!targetuuid.equals("null")) {
                String targetname = NameFetcher.getName(targetuuid);
                if (targetname.equalsIgnoreCase("null")) {
                    targetname = strings[0];
                }
                try {
                    String sql = "SELECT * FROM `mutes` WHERE UUID='" + targetuuid + "'";
                    PreparedStatement stmt = plugin.connection.prepareStatement(sql);
                    ResultSet results = stmt.executeQuery();
                    if (!results.next()) {
                        player.sendMessage(new ComponentBuilder(prefix).append(targetname + " is not muted!").color(ChatColor.RED).create());
                    }else{
                        String sql1 = "DELETE FROM `mutes` WHERE `UUID`='" + targetuuid + "' ;";
                        PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                        stmt1.executeUpdate();
                        BungeeMain.Logs.info(targetname + " was Unmuted by: " + player.getName());
                        player.sendMessage(new ComponentBuilder(prefix).append("The mute on: " + targetname + " has been successfully removed!").color(ChatColor.GREEN).create());
                        StaffChat.sendMessage(player.getName() + " Unmuted: " + targetname);
                    }
                }catch (SQLException e) {
                    plugin.mysqlfail(e);
                    if (plugin.testConnectionManual())
                        this.execute(commandSender, strings);
                }
            } else {
                player.sendMessage(new ComponentBuilder(prefix).append("That is not a player's name!").color(ChatColor.RED).create());
            }
        } else {
            if (strings.length == 0) {
                commandSender.sendMessage(new ComponentBuilder(prefix).append("Unmute a player").color(ChatColor.RED).append("\nUsage: /unmute <player name>").color(ChatColor.WHITE).create());
                return;
            }
            String targetuuid = UUIDFetcher.getUUID(strings[0]);
            if (!targetuuid.equals("null")) {
                String targetname = NameFetcher.getName(targetuuid);
                if (targetname.equalsIgnoreCase("null")) {
                    targetname = strings[0];
                }
                try {
                    String sql = "SELECT * FROM `mutes` WHERE UUID='" + targetuuid + "'";
                    PreparedStatement stmt = plugin.connection.prepareStatement(sql);
                    ResultSet results = stmt.executeQuery();
                    if (!results.next()) {
                        commandSender.sendMessage(new ComponentBuilder(prefix).append(targetname + " is not muted!").color(ChatColor.RED).create());
                    }else{
                        String sql1 = "DELETE FROM `mutes` WHERE `UUID`='" + targetuuid + "' ;";
                        PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                        stmt1.executeUpdate();
                        BungeeMain.Logs.info(targetname + " was Unmuted by CONSOLE");
                        commandSender.sendMessage(new ComponentBuilder(prefix).append("The mute on: " + targetname + " has been successfully removed!").color(ChatColor.GREEN).create());
                        StaffChat.sendMessage("CONSOLE Unmuted: " + targetname);
                    }
                }catch (SQLException e) {
                    plugin.mysqlfail(e);
                    if (plugin.testConnectionManual())
                        this.execute(commandSender, strings);
                }
            } else {
                commandSender.sendMessage(new ComponentBuilder(prefix).append("That is not a player's name!").color(ChatColor.RED).create());
            }
        }
    }
}