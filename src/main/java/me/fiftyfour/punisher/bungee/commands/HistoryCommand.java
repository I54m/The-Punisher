package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.fetchers.Status;
import me.fiftyfour.punisher.universal.fetchers.NameFetcher;
import me.fiftyfour.punisher.universal.fetchers.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.*;

public class HistoryCommand extends Command {
    private BungeeMain plugin = BungeeMain.getInstance();
    private int sqlfails = 0;
    private String targetuuid;

    public HistoryCommand() {
        super("history", "punisher.history", "hist");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            if (strings.length == 0) {
                player.sendMessage(new ComponentBuilder(plugin.prefix).append("View a player's punishment history").color(ChatColor.RED).append("\nUsage: /history <player name>").color(ChatColor.WHITE).create());
                return;
            }
            ProxiedPlayer findTarget = ProxyServer.getInstance().getPlayer(strings[0]);
            Future<String> future = null;
            ExecutorService executorService = null;
            if (findTarget != null) {
                targetuuid = findTarget.getUniqueId().toString().replace("-", "");
            } else {
                UUIDFetcher uuidFetcher = new UUIDFetcher();
                uuidFetcher.fetch(strings[0]);
                executorService = Executors.newSingleThreadExecutor();
                future = executorService.submit(uuidFetcher);
            }
            if (future != null) {
                try {
                    targetuuid = future.get(10, TimeUnit.SECONDS);
                } catch (TimeoutException te) {
                    player.sendMessage(new ComponentBuilder(plugin.prefix).append("ERROR: ").color(ChatColor.DARK_RED).append("Connection to mojang API took too long! Unable to fetch " + strings[0] + "'s uuid!").color(ChatColor.RED).create());
                    player.sendMessage(new ComponentBuilder(plugin.prefix).append("This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ").color(ChatColor.RED).create());
                    BungeeMain.Logs.severe("ERROR: Connection to mojang API took too long! Unable to fetch " + strings[0] + "'s uuid!");
                    BungeeMain.Logs.severe("Error message: " + te.getMessage());
                    StringBuilder stacktrace = new StringBuilder();
                    for (StackTraceElement stackTraceElement : te.getStackTrace()){
                        stacktrace.append(stackTraceElement.toString()).append("\n");
                    }
                    BungeeMain.Logs.severe("Stack Trace: " + stacktrace.toString());
                    executorService.shutdown();
                    return;
                } catch (Exception e) {
                    player.sendMessage(new ComponentBuilder(plugin.prefix).append("ERROR: ").color(ChatColor.DARK_RED).append("Unexpected error while executing command! Unable to fetch " + strings[0] + "'s uuid!").color(ChatColor.RED).create());
                    player.sendMessage(new ComponentBuilder(plugin.prefix).append("This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ").color(ChatColor.RED).create());
                    BungeeMain.Logs.severe("ERROR: Unexpected error while trying executing command in class: " + this.getName() + " Unable to fetch " + strings[0] + "'s uuid");
                    BungeeMain.Logs.severe("Error message: " + e.getMessage());
                    StringBuilder stacktrace = new StringBuilder();
                    for (StackTraceElement stackTraceElement : e.getStackTrace()){
                        stacktrace.append(stackTraceElement.toString()).append("\n");
                    }
                    BungeeMain.Logs.severe("Stack Trace: " + stacktrace.toString());
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
                    String sql = "SELECT * FROM `history` WHERE UUID='" + targetuuid + "'";
                    PreparedStatement stmt = plugin.connection.prepareStatement(sql);
                    ResultSet results = stmt.executeQuery();
                    if (results.next()) {
                        ExecutorService executorService1;
                        Future<TextComponent> futurestatus = null;
                        Status statusClass = new Status();
                        statusClass.setTargetuuid(targetuuid);
                        executorService1 = Executors.newSingleThreadExecutor();
                        futurestatus = executorService1.submit(statusClass);
                        player.sendMessage(new ComponentBuilder("|-------------").color(ChatColor.GREEN).strikethrough(true).append("History for: " + targetname).color(ChatColor.RED).strikethrough(false)
                                .append("-------------|").color(ChatColor.GREEN).strikethrough(true).create());
                        if (results.getInt("Minor_Chat_Offence") != 0) player.sendMessage(new ComponentBuilder("Been Punished for Minor Chat Offence: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Minor_Chat_Offence"))).color(ChatColor.RED).create());
                        if (results.getInt("Major_Chat_Offence") != 0) player.sendMessage(new ComponentBuilder("Been Punished for Major Chat Offence: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Major_Chat_Offence"))).color(ChatColor.RED).create());
                        if (results.getInt("DDoS_DoX_Threats") != 0) player.sendMessage(new ComponentBuilder("Been Punished for DDoS/DoX Threats: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("DDoS_DoX_Threats"))).color(ChatColor.RED).create());
                        if (results.getInt("Inappropriate_Link") != 0) player.sendMessage(new ComponentBuilder("Been Punished for Inappropriate Link: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Inappropriate_Link"))).color(ChatColor.RED).create());
                        if (results.getInt("Scamming") != 0) player.sendMessage(new ComponentBuilder("Been Punished for Scamming: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Scamming"))).color(ChatColor.RED).create());
                        if (results.getInt("X_Raying") != 0) player.sendMessage(new ComponentBuilder("Been Punished for X-Raying: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("X_Raying"))).color(ChatColor.RED).create());
                        if (results.getInt("AutoClicker") != 0) player.sendMessage(new ComponentBuilder("Been Punished for AutoClicker(Non PvP): ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("AutoClicker"))).color(ChatColor.RED).create());
                        if (results.getInt("Fly_Speed_Hacking") != 0) player.sendMessage(new ComponentBuilder("Been Punished for Fly/Speed Hacking: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Fly_Speed_Hacking"))).color(ChatColor.RED).create());
                        if (results.getInt("Malicious_PvP_Hacks") != 0) player.sendMessage(new ComponentBuilder("Been Punished for Malicious PvP Hacks: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Malicious_PvP_Hacks"))).color(ChatColor.RED).create());
                        if (results.getInt("Disallowed_Mods") != 0) player.sendMessage(new ComponentBuilder("Been Punished for Disallowed Mods: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Disallowed_Mods"))).color(ChatColor.RED).create());
                        if (results.getInt("Server_Advertisment") != 0) player.sendMessage(new ComponentBuilder("Been Punished for Server Advertisment: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Server_Advertisment"))).color(ChatColor.RED).create());
                        if (results.getInt("Greifing") != 0) player.sendMessage(new ComponentBuilder("Been Punished for Greifing: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Greifing"))).color(ChatColor.RED).create());
                        if (results.getInt("Exploiting") != 0) player.sendMessage(new ComponentBuilder("Been Punished for Exploiting: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Exploiting"))).color(ChatColor.RED).create());
                        if (results.getInt("Tpa_Trapping") != 0) player.sendMessage(new ComponentBuilder("Been Punished for TPA-Trapping: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Tpa_Trapping"))).color(ChatColor.RED).create());
                        if (results.getInt("Impersonation") != 0) player.sendMessage(new ComponentBuilder("Been Punished for Player_Impersonation: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Impersonation"))).color(ChatColor.RED).create());
                        if (results.getInt("Manual_Punishments") != 0) player.sendMessage(new ComponentBuilder("Been Punished Manually: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Manual_Punishments"))).color(ChatColor.RED).create());
                        TextComponent status;
                        try{
                            status = futurestatus.get(5, TimeUnit.SECONDS);
                        } catch (TimeoutException te) {
                            player.sendMessage(new ComponentBuilder(plugin.prefix).append("ERROR: ").color(ChatColor.DARK_RED).append("Status creation took too long! Unable to fetch " + strings[0] + "'s status!").color(ChatColor.RED).create());
                            player.sendMessage(new ComponentBuilder(plugin.prefix).append("This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ").color(ChatColor.RED).create());
                            BungeeMain.Logs.severe("ERROR: Status creation took too long! Unable to fetch " + strings[0] + "'s status!");
                            BungeeMain.Logs.severe("Error message: " + te.getMessage());
                            StringBuilder stacktrace = new StringBuilder();
                            for (StackTraceElement stackTraceElement : te.getStackTrace()) {
                                stacktrace.append(stackTraceElement.toString()).append("\n");
                            }
                            BungeeMain.Logs.severe("Stack Trace: " + stacktrace.toString());
                            executorService1.shutdown();
                            return;
                        } catch (Exception e) {
                            player.sendMessage(new ComponentBuilder(plugin.prefix).append("ERROR: ").color(ChatColor.DARK_RED).append("Unexpected error while executing command! Unable to fetch " + strings[0] + "'s status!").color(ChatColor.RED).create());
                            player.sendMessage(new ComponentBuilder(plugin.prefix).append("This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ").color(ChatColor.RED).create());
                            BungeeMain.Logs.severe("ERROR: Unexpected error while trying executing command in class: " + this.getName() + " Unable to fetch " + strings[0] + "'s status");
                            BungeeMain.Logs.severe("Error message: " + e.getMessage());
                            StringBuilder stacktrace = new StringBuilder();
                            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                                stacktrace.append(stackTraceElement.toString()).append("\n");
                            }
                            BungeeMain.Logs.severe("Stack Trace: " + stacktrace.toString());
                            executorService1.shutdown();
                            return;
                        }
                        executorService1.shutdown();
                        player.sendMessage(new ComponentBuilder("Current Status: ").color(ChatColor.GREEN).append(status).create());
                    } else {
                        player.sendMessage(new ComponentBuilder(plugin.prefix).append(targetname + " has not been punished yet!").color(ChatColor.RED).create());
                    }
                    stmt.close();
                    results.close();
                } catch (SQLException e) {
                    plugin.getLogger().severe(plugin.prefix + e);
                    sqlfails++;
                    if(sqlfails > 5){
                        plugin.getProxy().getPluginManager().unregisterCommand(this);
                        StringBuilder sb = new StringBuilder();
                        for (String args : strings){
                            sb.append(args).append(" ");
                        }
                        commandSender.sendMessage(new ComponentBuilder(this.getName() + " " + sb.toString() + " has thrown an exception more than 5 times!").color(ChatColor.RED).create());
                        commandSender.sendMessage(new ComponentBuilder("Disabling command to prevent further damage to database").color(ChatColor.RED).create());
                        plugin.getLogger().severe(plugin.prefix + this.getName() + " " + sb.toString() + " has thrown an exception more than 5 times!");
                        plugin.getLogger().severe(plugin.prefix + "Disabling command to prevent further damage to database!");
                        BungeeMain.Logs.severe(this.getName() + " has thrown an exception more than 5 times!");
                        BungeeMain.Logs.severe("Disabling command to prevent further damage to database!");
                        return;
                    }
                    if (plugin.testConnectionManual())
                        this.execute(commandSender, strings);
                }
            } else {
                player.sendMessage(new ComponentBuilder(plugin.prefix).append("That is not a player's name").color(ChatColor.RED).create());
            }
        } else {
            commandSender.sendMessage(new TextComponent("You must be a player to use this command!"));
        }
    }
}
