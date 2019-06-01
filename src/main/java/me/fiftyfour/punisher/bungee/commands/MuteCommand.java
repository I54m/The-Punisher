package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.exceptions.DataFecthException;
import me.fiftyfour.punisher.bungee.handlers.ErrorHandler;
import me.fiftyfour.punisher.bungee.managers.PunishmentManager;
import me.fiftyfour.punisher.bungee.objects.Punishment;
import me.fiftyfour.punisher.universal.fetchers.NameFetcher;
import me.fiftyfour.punisher.universal.fetchers.UUIDFetcher;
import me.fiftyfour.punisher.universal.systems.Permissions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MuteCommand extends Command {

    private BungeeMain plugin = BungeeMain.getInstance();
    private long length;
    private String targetuuid;
    private String targetname;
    private int sqlfails = 0;
    private PunishmentManager punishMnger = PunishmentManager.getInstance();

    public MuteCommand() {
        super("mute", "punisher.mute", "tempmute");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage(new TextComponent("You must be a player to use this command!"));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) commandSender;
        if (strings.length == 0) {
            player.sendMessage(new ComponentBuilder(plugin.prefix).append("Mute a player from speaking").color(ChatColor.RED).append("\nUsage: /mute <player> [length<s|m|h|d|w|M|perm>] [reason]").color(ChatColor.WHITE).create());
            return;
        }
        if (targetname != null || targetuuid != null){
            targetuuid = null;
            targetname = null;
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
            }else {
                duration = false;
            }
        }catch(NumberFormatException e){
            player.sendMessage(new ComponentBuilder(plugin.prefix).append(strings[1] + " is not a valid duration!").color(ChatColor.RED).create());
            player.sendMessage(new ComponentBuilder(plugin.prefix).append("Mute a player from speaking").color(ChatColor.RED).append("\nUsage: /mute <player> [length<s|m|h|d|w|M|perm>] [reason]").color(ChatColor.WHITE).create());
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
        }else {
            reason.append("Manually Muted");
        }
        String reasonString = reason.toString().replace("\"", "'");
        if (future != null) {
            try {
                targetuuid = future.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                try {
                    throw new DataFecthException("UUID Required for next step", strings[0], "UUID", this.getName(), e);
                }catch (DataFecthException dfe){
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.log(dfe);
                    errorHandler.alert(dfe, commandSender);
                }
                executorService.shutdown();
                return;
            }
            executorService.shutdown();
        }
        if (targetuuid == null) {
            player.sendMessage(new ComponentBuilder("That is not a player's name!").color(ChatColor.RED).create());
            return;
        }
        targetname = NameFetcher.getName(targetuuid);
        if (targetname == null) {
            targetname = strings[0];
        }
        try {
            if (!Permissions.higher(player, targetuuid, targetname)) {
                player.sendMessage(new ComponentBuilder(plugin.prefix).append("You cannot punish that player!").color(ChatColor.RED).create());
                return;
            }
        }catch (Exception e){
            player.sendMessage(new ComponentBuilder(plugin.prefix).append("ERROR: ").color(ChatColor.DARK_RED).append("Luckperms was unable to fetch permission data on: " + targetname).color(ChatColor.RED).create());
            player.sendMessage(new ComponentBuilder(plugin.prefix).append("This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ").color(ChatColor.RED).create());
            BungeeMain.Logs.severe("ERROR: Luckperms was unable to fetch permission data on: " + targetname);
            BungeeMain.Logs.severe("Error message: " + e.getMessage());
            StringBuilder stacktrace = new StringBuilder();
            for (StackTraceElement stackTraceElement : e.getStackTrace()){
                stacktrace.append(stackTraceElement.toString()).append("\n");
            }
            BungeeMain.Logs.severe("Stack Trace: " + stacktrace.toString());
            return;
        }
        try {
            Punishment mute = new Punishment(Punishment.Reason.Manual, reasonString, length, Punishment.Type.MUTE, targetuuid, player.getUniqueId().toString().replace("-", ""));
            punishMnger.issue(mute, player, targetname, true, true, true);
        }catch (SQLException e){
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
    }
}
