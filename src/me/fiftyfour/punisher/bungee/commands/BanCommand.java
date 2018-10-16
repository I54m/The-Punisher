package me.fiftyfour.punisher.bungee.commands;

import com.google.common.collect.Lists;
import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.chats.StaffChat;
import me.fiftyfour.punisher.fetchers.NameFetcher;
import me.fiftyfour.punisher.fetchers.UUIDFetcher;
import me.fiftyfour.punisher.systems.Permissions;
import me.fiftyfour.punisher.systems.ReputationSystem;
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

public class BanCommand extends Command {
    private BungeeMain plugin = BungeeMain.getInstance();
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;
    private long length;
    private String targetname;
    private String targetuuid;
    private int sqlfails = 0;

    public BanCommand() {
        super("ban", "punisher.ban", "tempban", "ipban", "banip");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage(new TextComponent("You must be a player to use this command!"));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) commandSender;
        if (strings.length == 0) {
            player.sendMessage(new ComponentBuilder(prefix).append("Ban a player from the server").color(ChatColor.RED).append("\nUsage: /ban <player> [length<s|m|h|d|w|M|perm>] [reason]").color(ChatColor.WHITE).create());
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
        boolean duration;
        try {
            if (strings.length == 1 || strings[1].toLowerCase().endsWith("perm")) {
                length = (long) 1000 * 60 * 60 * 24 * 7 * 4 * 12 * 54;
                duration = true;
            } else if (strings[1].endsWith("M")) {
                length = (long) 1000 * 60 * 60 * 24 * 7 * 4 * (long) Integer.parseInt(strings[1].replace("M", ""));
                duration = true;
            } else if (strings[1].toLowerCase().endsWith("w")) {
                length = 1000 * 60 * 60 * 24 * 7 * (long) Integer.parseInt(strings[1].replace("w", ""));
                duration = true;
            } else if (strings[1].toLowerCase().endsWith("d")) {
                length = 1000 * 60 * 60 * 24 * (long) Integer.parseInt(strings[1].replace("d", ""));
                duration = true;
            } else if (strings[1].toLowerCase().endsWith("h")) {
                length = 1000 * 60 * 60 * (long) Integer.parseInt(strings[1].replace("h", ""));
                duration = true;
            } else if (strings[1].endsWith("m")) {
                length = 1000 * 60 * (long) Integer.parseInt(strings[1].replace("m", ""));
                duration = true;
            } else if (strings[1].toLowerCase().endsWith("s")) {
                length = 1000 * (long) Integer.parseInt(strings[1].replace("s", ""));
                duration = true;
            } else {
                duration = false;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(new ComponentBuilder(prefix).append(strings[1] + " is not a valid duration!").color(ChatColor.RED).create());
            player.sendMessage(new ComponentBuilder(prefix).append("Ban a player from the server").color(ChatColor.RED).append("\nUsage: /ban <player> [length<s|m|h|d|w|M|perm>] [reason]").color(ChatColor.WHITE).create());
            return;
        }
        StringBuilder reason = new StringBuilder();
        if (strings.length > 2 && duration) {
            for (int i = 2; i < strings.length; i++) {
                reason.append(strings[i]).append(" ");
            }
        } else if (!duration) {
            for (int i = 1; i < strings.length; i++) {
                reason.append(strings[i]).append(" ");
            }
            length = (long) 1000 * 60 * 60 * 24 * 7 * 4 * 12 * 54;
        } else {
            reason.append("Manually Banned");
        }
        try {
            String sql2 = "SELECT * FROM `staffhistory` WHERE UUID='" + player.getUniqueId().toString().replace("-", "") + "'";
            PreparedStatement stmt2 = plugin.connection.prepareStatement(sql2);
            ResultSet results2 = stmt2.executeQuery();
            if (!results2.next()) {
                String sql3 = "INSERT INTO `staffhistory` (UUID) VALUES ('" + player.getUniqueId().toString().replace("-", "") + "');";
                PreparedStatement stmt3 = plugin.connection.prepareStatement(sql3);
                stmt3.executeUpdate();
                stmt3.close();
            }
            stmt2.close();
            results2.close();
        } catch (SQLException e) {
            plugin.getLogger().severe(prefix + e);
            sqlfails++;
            if (sqlfails > 5) {
                plugin.getProxy().getPluginManager().unregisterCommand(this);
                commandSender.sendMessage(new ComponentBuilder(this.getName() + Lists.asList(strings[0], strings).toString() + " has thrown an exception more than 5 times!").color(ChatColor.RED).create());
                commandSender.sendMessage(new ComponentBuilder("Disabling command to prevent further damage to database").color(ChatColor.RED).create());
                plugin.getLogger().severe(prefix + this.getName() + Lists.asList(strings[0], strings).toString() + " has thrown an exception more than 5 times!");
                plugin.getLogger().severe(prefix + "Disabling command to prevent further damage to database!");
                BungeeMain.Logs.severe(this.getName() + " has thrown an exception more than 5 times!");
                BungeeMain.Logs.severe("Disabling command to prevent further damage to database!");
                return;
            }
            if (plugin.testConnectionManual())
                this.execute(commandSender, strings);
        }
        String reasonString = reason.toString().replace("\"", "'");
        if (future != null && targetuuid == null) {
            try {
                targetuuid = future.get(10, TimeUnit.SECONDS);
            } catch (TimeoutException te) {
                player.sendMessage(new ComponentBuilder(prefix).append("ERROR: ").color(ChatColor.DARK_RED).append("Connection to mojang API took too long! Unable to fetch " + strings[0] + "'s uuid!").color(ChatColor.RED).create());
                player.sendMessage(new ComponentBuilder(prefix).append("This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ").color(ChatColor.RED).create());
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
                e.printStackTrace();
                player.sendMessage(new ComponentBuilder(prefix).append("ERROR: ").color(ChatColor.DARK_RED).append("Unexpected error while executing command! Unable to fetch " + strings[0] + "'s uuid!").color(ChatColor.RED).create());
                player.sendMessage(new ComponentBuilder(prefix).append("This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ").color(ChatColor.RED).create());
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
        } else return;
        if (targetuuid == null) {
            player.sendMessage(new ComponentBuilder("That is not a player's name!").color(ChatColor.RED).create());
            return;
        }
        if (findTarget == null) {
            targetname = NameFetcher.getName(targetuuid);
            if (targetname == null) {
                targetname = strings[0];
            }
        } else {
            targetname = findTarget.getName();
        }
        try {
            if (!Permissions.higher(player, targetuuid, targetname)) {
                player.sendMessage(new ComponentBuilder(prefix).append("You cannot punish that player!").color(ChatColor.RED).create());
                return;
            }
        } catch (Exception e) {
            player.sendMessage(new ComponentBuilder(prefix).append("ERROR: ").color(ChatColor.DARK_RED).append("Luckperms was unable to fetch permission data on: " + targetname).color(ChatColor.RED).create());
            player.sendMessage(new ComponentBuilder(prefix).append("This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ").color(ChatColor.RED).create());
            BungeeMain.Logs.severe("ERROR: Luckperms was unable to fetch permission data on: " + targetname);
            BungeeMain.Logs.severe("Error message: " + e.getMessage());
            StringBuilder stacktrace = new StringBuilder();
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                stacktrace.append(stackTraceElement.toString()).append("\n");
            }
            BungeeMain.Logs.severe("Stack Trace: " + stacktrace.toString());
            return;
        }
        try {
            String sql = "SELECT * FROM `history` WHERE UUID='" + targetuuid + "'";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            ResultSet results = stmt.executeQuery();
            if (!results.next()) {
                String sql1 = "INSERT INTO `history` (UUID) VALUES ('" + targetuuid + "');";
                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                stmt1.executeUpdate();
                stmt.close();
            }
            stmt.close();
            results.close();
        } catch (SQLException e) {
            plugin.mysqlfail(e);
            if (plugin.testConnectionManual())
                execute(commandSender, strings);
        }
        try {
            String sql = "SELECT * FROM `staffhistory` WHERE UUID='" + player.getUniqueId().toString().replace("-", "") + "'";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                int Punishmentno = results.getInt("Manual Punishments");
                Punishmentno++;
                String sql1 = "UPDATE `staffhistory` SET `Manual Punishments`=" + Punishmentno + " WHERE UUID='" + player.getUniqueId().toString().replace("-", "") + "';";
                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                stmt1.executeUpdate();
                stmt1.close();
            }
            stmt.close();
            results.close();
            String sql2 = "SELECT * FROM `history` WHERE UUID='" + targetuuid + "'";
            PreparedStatement stmt2 = plugin.connection.prepareStatement(sql2);
            ResultSet results1 = stmt2.executeQuery();
            if (results1.next()) {
                int Punishmentno1 = results1.getInt("Manual Punishments");
                Punishmentno1++;
                String sql3 = "UPDATE `history` SET `Manual Punishments`='" + Punishmentno1 + "' WHERE `UUID`='" + targetuuid + "' ;";
                PreparedStatement stmt3 = plugin.connection.prepareStatement(sql3);
                stmt3.executeUpdate();
                stmt3.close();
            }
            stmt2.close();
            results1.close();
        } catch (SQLException e) {
            plugin.mysqlfail(e);
            if (plugin.testConnectionManual())
                this.execute(commandSender, strings);
            return;
        }
        Long banleftmillis = length;
        int daysleft = (int) (banleftmillis / (1000 * 60 * 60 * 24));
        int hoursleft = (int) (banleftmillis / (1000 * 60 * 60) % 24);
        int minutesleft = (int) (banleftmillis / (1000 * 60) % 60);
        int secondsleft = (int) (banleftmillis / 1000 % 60);
        ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
            @Override
            public void run() {
                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(UUIDFetcher.formatUUID(targetuuid));
                if (target != null && target.isConnected()) {
                    plugin.getLogger().info("target is not null?");
                    if (daysleft > 500) {
                        String banMessage = BungeeMain.PunisherConfig.getString("PermBan Message").replace("%days%", String.valueOf(daysleft))
                                .replace("%hours%", String.valueOf(hoursleft)).replace("%minutes%", String.valueOf(minutesleft))
                                .replace("%seconds%", String.valueOf(secondsleft)).replace("%reason%", reason.toString());
                        plugin.getLogger().info(banMessage);
                        target.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', banMessage)));
                    } else {
                        String banMessage = BungeeMain.PunisherConfig.getString("TempBan Message").replace("%days%", String.valueOf(daysleft))
                                .replace("%hours%", String.valueOf(hoursleft)).replace("%minutes%", String.valueOf(minutesleft))
                                .replace("%seconds%", String.valueOf(secondsleft)).replace("%reason%", reason.toString());
                        target.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', banMessage)));
                    }
                }
                StaffChat.sendMessage(player.getName() + " Banned: " + targetname + " for: " + reason);
                if (daysleft > 500)
                    StaffChat.sendMessage("This ban is permanent and does not expire!");
                else
                    StaffChat.sendMessage("This ban expires in: " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s");
                ReputationSystem.minusRep(targetname, targetuuid, 4);
            }
        });
        try {
            String sql = "SELECT * FROM `bans` WHERE UUID='" + targetuuid + "'";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            ResultSet results = stmt.executeQuery();
            if (!results.next()) {
                String sql1 = "INSERT INTO `bans` (`UUID`, `Name`, `Length`, `Reason`, `Punisher`) VALUES ('" + targetuuid + "', '" + targetname + "', '" + (length + System.currentTimeMillis()) + "', \"" + reasonString + "\", '" + player.getName() + "');";
                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                stmt1.executeUpdate();
                stmt1.close();
            } else {
                String sql1 = "UPDATE `bans` SET `UUID`='" + targetuuid + "', `Name`='" + targetname + "', `Length`='" + (length + System.currentTimeMillis()) + "', `Reason`=\"" + reasonString + "\", `Punisher`='" + player.getName() + "' WHERE `UUID`='" + targetuuid + "';";
                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                stmt1.executeUpdate();
                stmt1.close();
            }
            stmt.close();
            results.close();
        } catch (SQLException e) {
            plugin.mysqlfail(e);
            if (plugin.testConnectionManual())
                this.execute(commandSender, strings);
            return;
        }
        BungeeMain.Logs.info(targetname + " Was Banned for: " + reason + " by: " + player.getName());
    }
}