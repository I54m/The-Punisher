package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
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
import java.util.TreeMap;
import java.util.concurrent.*;

public class IpHistCommand extends Command {

    public IpHistCommand() {
        super("iphist", "punisher.alts.ip", "ihist", "ih");
    }

    private BungeeMain plugin = BungeeMain.getInstance();
    private String targetuuid;
    private int sqlfails;

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            if (strings.length < 1) {
                player.sendMessage(new ComponentBuilder(plugin.prefix).append("Please provide a player's name!").create());
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
                    for (StackTraceElement stackTraceElement : te.getStackTrace()) {
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
                    for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                        stacktrace.append(stackTraceElement.toString()).append("\n");
                    }
                    BungeeMain.Logs.severe("Stack Trace: " + stacktrace.toString());
                    executorService.shutdown();
                    return;
                }
                executorService.shutdown();
            }
            if (targetuuid == null) {
                player.sendMessage(new ComponentBuilder(plugin.prefix).append("That is not a player's name!").color(ChatColor.RED).create());
                return;
            }
            String targetName = NameFetcher.getName(targetuuid);
            if (targetName == null) {
                targetName = strings[0];
            }
            try {
                String sql = "SELECT * FROM `iphist` WHERE UUID='" + targetuuid + "'";
                PreparedStatement stmt = plugin.connection.prepareStatement(sql);
                ResultSet results = stmt.executeQuery();
                TreeMap<Long, String> iphist = new TreeMap<>();
                while (results.next()) {
                    iphist.put(results.getLong("date"), results.getString("ip"));
                }
                if (iphist.isEmpty()) {
                    player.sendMessage(new ComponentBuilder(plugin.prefix).append(targetName + " does not have any ips stored in the database").color(ChatColor.RED).create());
                    return;
                }
                player.sendMessage(new ComponentBuilder("|-----").strikethrough(true).color(ChatColor.GREEN).append(" " + targetName + "'s Ip hist ")
                        .strikethrough(false).color(ChatColor.RED).append("-----|").strikethrough(true).color(ChatColor.GREEN).create());
                while (!iphist.isEmpty()) {
                    long timeago = (System.currentTimeMillis() - iphist.firstEntry().getKey());
                    String ip = iphist.firstEntry().getValue();
                    int daysago = (int) (timeago / (1000 * 60 * 60 * 24));
                    int hoursago = (int) (timeago / (1000 * 60 * 60) % 24);
                    int minutesago = (int) (timeago / (1000 * 60) % 60);
                    if (minutesago <= 0) minutesago = 1;
                    if (daysago >= 1){
                        player.sendMessage(new ComponentBuilder(ip).color(ChatColor.RED).append(" " + daysago + "d " + hoursago + "h " + minutesago + "m ago").color(ChatColor.GREEN).create());
                    }else if (hoursago >= 1){
                        player.sendMessage(new ComponentBuilder(ip).color(ChatColor.RED).append(" " + hoursago + "h " + minutesago + "m ago").color(ChatColor.GREEN).create());
                    }else {
                        player.sendMessage(new ComponentBuilder(ip).color(ChatColor.RED).append(" " + minutesago + "m ago").color(ChatColor.GREEN).create());
                    }
                    iphist.remove(iphist.firstKey());
                }
            } catch (SQLException sqle) {
                plugin.getLogger().severe(plugin.prefix + sqle);
                sqlfails++;
                if (sqlfails > 5) {
                    plugin.getProxy().getPluginManager().unregisterCommand(this);
                    StringBuilder sb = new StringBuilder();
                    for (String args : strings) {
                        sb.append(args).append(" ");
                    }
                    commandSender.sendMessage(new ComponentBuilder(this.getName() + " " + sb.toString() + " has thrown an exception more than 5 times!").color(ChatColor.RED).create());
                    commandSender.sendMessage(new ComponentBuilder("Disabling command to prevent further damage to database").color(ChatColor.RED).create());
                    plugin.getLogger().severe(plugin.prefix + this.getName() + " " + sb.toString() + " has thrown an exception more than 5 times!");
                    plugin.getLogger().severe(plugin.prefix + "Disabling command to prevent further damage to database!");
                    sqle.printStackTrace();
                    BungeeMain.Logs.severe(this.getName() + " has thrown an exception more than 5 times!");
                    BungeeMain.Logs.severe("Disabling command to prevent further damage to database!");
                    return;
                }
                if (plugin.testConnectionManual())
                    this.execute(commandSender, strings);
            }
        } else commandSender.sendMessage(new TextComponent("You must be a player to use this command!"));
    }
}
