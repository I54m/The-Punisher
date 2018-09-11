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

public class UnbanCommand extends Command {
    private BungeeMain plugin = BungeeMain.getInstance();
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;

    public UnbanCommand() {
        super("unban", "punisher.unban", "pardon");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            if (strings.length == 0) {
                player.sendMessage(new ComponentBuilder(prefix).append("Unban a player").color(ChatColor.RED).append("\nUsage: /unban <player name>").color(ChatColor.WHITE).create());
                return;
            }
            String targetuuid = UUIDFetcher.getUUID(strings[0]);
            if (!targetuuid.equals("null")) {
                String targetname = NameFetcher.getName(targetuuid);
                if (targetname.equalsIgnoreCase("null")) {
                    targetname = strings[0];
                }
                try {
                    String sql = "SELECT * FROM `bans` WHERE UUID='" + targetuuid + "'";
                    PreparedStatement stmt = plugin.connection.prepareStatement(sql);
                    ResultSet results = stmt.executeQuery();
                    if (!results.next()) {
                        player.sendMessage(new ComponentBuilder(prefix).append(targetname + " is not banned!").color(ChatColor.RED).create());
                    }else{
                        String sql1 = "DELETE FROM `bans` WHERE `UUID`='" + targetuuid + "' ;";
                        PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                        stmt1.executeUpdate();
                        BungeeMain.Logs.info(targetname + " Was Unbanned by: " + player.getName());
                        player.sendMessage(new ComponentBuilder(prefix).append("The ban on: " + targetname + " has been successfully removed!").color(ChatColor.GREEN).create());
                        StaffChat.sendMessage(player.getName() + " Unbanned: " + targetname);
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
                commandSender.sendMessage(new ComponentBuilder(prefix).append("Unban a player").color(ChatColor.RED).append("\nUsage: /unban <player name>").color(ChatColor.WHITE).create());
                return;
            }
            String targetuuid = UUIDFetcher.getUUID(strings[0]);
            if (!targetuuid.equals("null")) {
                String targetname = NameFetcher.getName(targetuuid);
                if (targetname.equalsIgnoreCase("null")) {
                    targetname = strings[0];
                }
                try {
                    String sql = "SELECT * FROM `bans` WHERE UUID='" + targetuuid + "'";
                    PreparedStatement stmt = plugin.connection.prepareStatement(sql);
                    ResultSet results = stmt.executeQuery();
                    if (!results.next()) {
                        commandSender.sendMessage(new ComponentBuilder(prefix).append(targetname + " is not banned!").color(ChatColor.RED).create());
                    }else{
                        String sql1 = "DELETE FROM `bans` WHERE `UUID`='" + targetuuid + "' ;";
                        PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                        stmt1.executeUpdate();
                        BungeeMain.Logs.info(targetname + " Was Unbanned by: CONSOLE");
                        commandSender.sendMessage(new ComponentBuilder(prefix).append("The ban on: " + targetname + " has been successfully removed!").color(ChatColor.GREEN).create());
                        StaffChat.sendMessage("CONSOLE Unbanned: " + targetname);
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