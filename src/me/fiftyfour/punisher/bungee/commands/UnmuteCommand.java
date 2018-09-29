package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.chats.StaffChat;
import me.fiftyfour.punisher.fetchers.NameFetcher;
import me.fiftyfour.punisher.fetchers.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.*;

public class UnmuteCommand extends Command {
    private BungeeMain plugin = BungeeMain.getInstance();
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;
    private String targetuuid;

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
            ProxiedPlayer findTarget = ProxyServer.getInstance().getPlayer(strings[0]);
            Future<String> future = null;
            ExecutorService executorService = null;
            if (findTarget != null){
                targetuuid = findTarget.getUniqueId().toString().replace("-", "");
            }else {
                UUIDFetcher uuidFetcher = new UUIDFetcher();
                uuidFetcher.fetch(strings[0]);
                executorService = Executors.newSingleThreadExecutor();
                future = executorService.submit(uuidFetcher);
            }
            if (future != null) {
                try {
                    targetuuid = future.get(5, TimeUnit.SECONDS);
                } catch (TimeoutException te) {
                    player.sendMessage(new ComponentBuilder(prefix).append("ERROR: ").color(ChatColor.DARK_RED).append("Connection to mojang API took too long! Unable to fetch " + strings[0] + "'s uuid!").color(ChatColor.RED).create());
                    player.sendMessage(new ComponentBuilder(prefix).append("This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ").color(ChatColor.RED).create());
                    BungeeMain.Logs.severe("ERROR: Connection to mojang API took too long! Unable to fetch " + strings[0] + "'s uuid!");
                    BungeeMain.Logs.severe("Error message: " + te.getMessage());
                    BungeeMain.Logs.severe("Stack Trace: " + te.getStackTrace().toString());
                    executorService.shutdown();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage(new ComponentBuilder(prefix).append("ERROR: ").color(ChatColor.DARK_RED).append("Unexpected error while executing command! Unable to fetch " + strings[0] + "'s uuid!").color(ChatColor.RED).create());
                    player.sendMessage(new ComponentBuilder(prefix).append("This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ").color(ChatColor.RED).create());
                    BungeeMain.Logs.severe("ERROR: Unexpected error while trying executing command in class: " + this.getName() + " Unable to fetch " + strings[0] + "'s uuid");
                    BungeeMain.Logs.severe("Error message: " + e.getMessage());
                    BungeeMain.Logs.severe("Stack Trace: " + e.getStackTrace().toString());
                    executorService.shutdown();
                    return;
                }
                executorService.shutdown();
            }
            if (targetuuid !=null) {
                String targetname = NameFetcher.getName(targetuuid);
                if (targetname == null) {
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
                        stmt1.close();
                        BungeeMain.Logs.info(targetname + " was Unmuted by: " + player.getName());
                        player.sendMessage(new ComponentBuilder(prefix).append("The mute on: " + targetname + " has been successfully removed!").color(ChatColor.GREEN).create());
                        StaffChat.sendMessage(player.getName() + " Unmuted: " + targetname);
                    }
                    stmt.close();
                    results.close();
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
            ProxiedPlayer findTarget = ProxyServer.getInstance().getPlayer(strings[0]);
            Future<String> future = null;
            ExecutorService executorService = null;
            if (findTarget != null){
                targetuuid = findTarget.getUniqueId().toString().replace("-", "");
            }else {
                UUIDFetcher uuidFetcher = new UUIDFetcher();
                uuidFetcher.fetch(strings[0]);
                executorService = Executors.newSingleThreadExecutor();
                future = executorService.submit(uuidFetcher);
            }
            if (future != null) {
                try {
                    targetuuid = future.get(5, TimeUnit.SECONDS);
                } catch (TimeoutException te) {
                    commandSender.sendMessage(new ComponentBuilder(prefix).append("ERROR: ").color(ChatColor.DARK_RED).append("Connection to mojang API took too long! Unable to fetch " + strings[0] + "'s uuid!").color(ChatColor.RED).create());
                    commandSender.sendMessage(new ComponentBuilder(prefix).append("This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ").color(ChatColor.RED).create());
                    BungeeMain.Logs.severe("ERROR: Connection to mojang API took too long! Unable to fetch " + strings[0] + "'s uuid!");
                    BungeeMain.Logs.severe("Error message: " + te.getMessage());
                    BungeeMain.Logs.severe("Stack Trace: " + te.getStackTrace().toString());
                    executorService.shutdown();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    commandSender.sendMessage(new ComponentBuilder(prefix).append("ERROR: ").color(ChatColor.DARK_RED).append("Unexpected error while executing command! Unable to fetch " + strings[0] + "'s uuid!").color(ChatColor.RED).create());
                    commandSender.sendMessage(new ComponentBuilder(prefix).append("This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ").color(ChatColor.RED).create());
                    BungeeMain.Logs.severe("ERROR: Unexpected error while trying executing command in class: " + this.getName() + " Unable to fetch " + strings[0] + "'s uuid");
                    BungeeMain.Logs.severe("Error message: " + e.getMessage());
                    BungeeMain.Logs.severe("Stack Trace: " + e.getStackTrace().toString());
                    executorService.shutdown();
                    return;
                }
                executorService.shutdown();
            }
            if (targetuuid != null) {
                String targetname = NameFetcher.getName(targetuuid);
                if (targetname == null) {
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
                        stmt1.close();
                        BungeeMain.Logs.info(targetname + " was Unmuted by CONSOLE");
                        commandSender.sendMessage(new ComponentBuilder(prefix).append("The mute on: " + targetname + " has been successfully removed!").color(ChatColor.GREEN).create());
                        StaffChat.sendMessage("CONSOLE Unmuted: " + targetname);
                    }
                    stmt.close();
                    results.close();
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