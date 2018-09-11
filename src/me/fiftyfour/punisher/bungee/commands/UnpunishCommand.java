package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.chats.StaffChat;
import me.fiftyfour.punisher.fetchers.NameFetcher;
import me.fiftyfour.punisher.fetchers.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UnpunishCommand extends Command {
    private BungeeMain plugin = BungeeMain.getInstance();
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;

    public UnpunishCommand() {
        super("unpunish", "punisher.unpunish", "unpun");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            if (strings.length <= 1) {
                player.sendMessage(new ComponentBuilder(prefix).append("Remove a punishment from a player's history (CaSe SeNsItIvE!!)").color(ChatColor.RED).append("\nUsage: /unpunish <player name> <reason>").color(ChatColor.WHITE).create());
                return;
            }
            String targetuuid = UUIDFetcher.getUUID(strings[0]);
            if (!targetuuid.equalsIgnoreCase("null")) {
                String targetname = NameFetcher.getName(targetuuid);
                if (targetname.equalsIgnoreCase("null")) {
                    targetname = strings[0];
                }
                try {
                    String sql = "SELECT * FROM `mutes` WHERE UUID='" + targetuuid + "'";
                    PreparedStatement stmt = plugin.connection.prepareStatement(sql);
                    ResultSet results = stmt.executeQuery();
                    if (results.next()){
                        String sql1 = "DELETE FROM `mutes` WHERE UUID='" + targetuuid + "'";
                        PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                        stmt1.executeUpdate();
                        StaffChat.sendMessage(player.getName() + " Unmuted: " + targetname);
                        BungeeMain.Logs.info(targetname + " Was Unmuted by: " + player.getName() + " Through an unpunish");
                    }
                    String sql2 = "SELECT * FROM `bans` WHERE UUID='" + targetuuid + "'";
                    PreparedStatement stmt2 = plugin.connection.prepareStatement(sql2);
                    ResultSet results2 = stmt2.executeQuery();
                    if (results2.next()){
                        String sql3 = "DELETE FROM `bans` WHERE UUID='" + targetuuid + "'";
                        PreparedStatement stmt3 = plugin.connection.prepareStatement(sql3);
                        stmt3.executeUpdate();
                        StaffChat.sendMessage(player.getName() + " Unbanned: " + targetname);
                        BungeeMain.Logs.info(targetname + " Was Unbanned by: " + player.getName() + " Through an unpunish");
                    }
                }catch (SQLException e){
                    plugin.mysqlfail(e);
                    if (plugin.testConnectionManual())
                        this.execute(commandSender, strings);
                    return;
                }
                StringBuilder reason = new StringBuilder();
                for (int i = 1; i < strings.length; i++) {
                    reason.append(strings[i]);
                    if (i + 1 < strings.length)reason.append(" ");
                }
                ArrayList<String> reasonsList;
                reasonsList = new ArrayList<>();
                reasonsList.add("Minor Chat Offence");
                reasonsList.add("Major Chat Offence");
                reasonsList.add("DDoS/DoX Threats");
                reasonsList.add("Inapproprioate Link");
                reasonsList.add("Scamming");
                reasonsList.add("X-Raying");
                reasonsList.add("AutoClicker(non PvP)");
                reasonsList.add("Fly/Speed Hacking");
                reasonsList.add("Malicious PvP Hacks");
                reasonsList.add("Server Advertisment");
                reasonsList.add("Greifing");
                reasonsList.add("Exploiting");
                reasonsList.add("Tpa-Trapping");
                reasonsList.add("Impersonation");
                if (reason.toString().contains("Manual") || reason.toString().contains("manual")) {
                    player.sendMessage(new ComponentBuilder(prefix).append("Manual Punishments may not be removed from history!").color(ChatColor.RED).create());
                    return;
                }
                if (!(reasonsList.contains(reason.toString()))) {
                    player.sendMessage(new ComponentBuilder(prefix).append("That is not a punishment reason!").color(ChatColor.RED).create());
                    player.sendMessage(new ComponentBuilder(prefix).append("Reasons are as follows:").color(ChatColor.RED).create());
                    player.sendMessage(new ComponentBuilder(prefix).append(reasonsList.toString().replace("[", "").replace("]", "")).color(ChatColor.RED).create());
                    return;
                }
                try {
                    String sql = "SELECT * FROM `history` WHERE UUID='" + targetuuid + "'";
                    PreparedStatement stmt = plugin.connection.prepareStatement(sql);
                    ResultSet results = stmt.executeQuery();
                    if (results.next()) {
                        int current = results.getInt(reason.toString());
                        if (current != 0) {
                            current--;
                        } else {
                            player.sendMessage(new ComponentBuilder(prefix).append(targetname + " Already has 0 punishments for that offence!").color(ChatColor.RED).create());
                            return;
                        }
                        String sql2 = "UPDATE `history` SET `" + reason + "`='" + current + "' WHERE `UUID`='" + targetuuid + "' ;";
                        PreparedStatement stmt2 = plugin.connection.prepareStatement(sql2);
                        stmt2.executeUpdate();
                        BungeeMain.Logs.info(player.getName() + " removed punishment: " + reason + " on player: " + targetname + " Through unpunish");
                        player.sendMessage(new ComponentBuilder(prefix).append("The punishment: " + reason + " on: " + targetname + " has been removed!").color(ChatColor.RED).create());
                        StaffChat.sendMessage(player.getName() + " Unpunished: " + targetname + " for the offence: " + reason);
                    }else player.sendMessage(new ComponentBuilder(prefix).append("That is not a player's name").color(ChatColor.RED).create());
                }catch (SQLException e){
                    plugin.mysqlfail(e);
                    if (plugin.testConnectionManual())
                        this.execute(commandSender, strings);
                }
            } else {
                player.sendMessage(new ComponentBuilder(prefix).append("That is not a player's name").color(ChatColor.RED).create());
            }
        } else {
            commandSender.sendMessage(new TextComponent("You must be a player to use this command!"));
        }
    }
}