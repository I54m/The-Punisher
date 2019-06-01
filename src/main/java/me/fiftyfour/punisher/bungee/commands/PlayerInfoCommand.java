package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.exceptions.DataFecthException;
import me.fiftyfour.punisher.bungee.fetchers.PlayerInfo;
import me.fiftyfour.punisher.bungee.fetchers.Status;
import me.fiftyfour.punisher.bungee.handlers.ErrorHandler;
import me.fiftyfour.punisher.universal.fetchers.NameFetcher;
import me.fiftyfour.punisher.universal.fetchers.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class PlayerInfoCommand extends Command {
    public PlayerInfoCommand() {
        super("playerinfo", "punisher.playerinfo", "info", "pi", "player");
    }

    private BungeeMain plugin = BungeeMain.getInstance();
    private String targetuuid;

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (!(commandSender instanceof ProxiedPlayer)){
            commandSender.sendMessage(new TextComponent("You need to be a player to use this command!"));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) commandSender;
        if (strings.length <= 0){
            player.sendMessage(new ComponentBuilder(plugin.prefix).append("View useful information about a player").color(ChatColor.RED).create());
            player.sendMessage(new ComponentBuilder(plugin.prefix).append("Usage: /playerinfo <player>").color(ChatColor.WHITE).create());
            return;
        }
        player.sendMessage(new ComponentBuilder(plugin.prefix).append("Collecting info on: " + strings[0] + ". Please wait...").color(ChatColor.GREEN).create());
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
        if (targetuuid == null) {
            player.sendMessage(new ComponentBuilder(plugin.prefix).append(strings[0] + " is not a player's name!").color(ChatColor.RED).create());
            return;
        }
        String targetname = NameFetcher.getName(targetuuid);
        if (targetname == null) {
            targetname = strings[0];
        }
        try {
            //send plugin message
            ByteArrayOutputStream outbytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(outbytes);
            out.writeUTF("Punisher");
            out.writeUTF("GetAAC");
            out.writeUTF(targetuuid);
            player.sendData("BungeeCord", outbytes.toByteArray());
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
        PlayerInfo playerInfo = new PlayerInfo();
        playerInfo.setTargetName(targetname);
        playerInfo.setTargetuuid(targetuuid);
        ExecutorService executorServiceinfo = Executors.newSingleThreadExecutor();
        Future<Map<String, String>> futureInfo = executorServiceinfo.submit(playerInfo);
        List<String> notes = new ArrayList<>();
        if (BungeeMain.InfoConfig.contains(targetuuid + ".notes")){
            notes = BungeeMain.InfoConfig.getStringList(targetuuid + ".notes");
        }
        ExecutorService executorService1;
        Future<TextComponent> futurestatus;
        Status statusClass = new Status();
        statusClass.setTargetuuid(targetuuid);
        executorService1 = Executors.newSingleThreadExecutor();
        futurestatus = executorService1.submit(statusClass);
        Map<String, String> info;
        try{
            info = futureInfo.get(20, TimeUnit.SECONDS);
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
        executorServiceinfo.shutdown();
        TextComponent status;
        try{
            status = futurestatus.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            BungeeMain.Logs.severe("ERROR: Status creation took too long! Unable to fetch " + targetname + "'s status!");
            BungeeMain.Logs.severe("Error message: " + te.getMessage());
            StringBuilder stacktrace = new StringBuilder();
            for (StackTraceElement stackTraceElement : te.getStackTrace()) {
                stacktrace.append(stackTraceElement.toString()).append("\n");
            }
            BungeeMain.Logs.severe("Stack Trace: " + stacktrace.toString());
            status = new TextComponent("Unable to fetch status!");
            status.setColor(ChatColor.RED);
        } catch (Exception e) {
            BungeeMain.Logs.severe("ERROR: Unexpected error while trying executing command in class: " + this.getClass().getName() + " Unable to fetch " + targetname + "'s status");
            BungeeMain.Logs.severe("Error message: " + e.getMessage());
            StringBuilder stacktrace = new StringBuilder();
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                stacktrace.append(stackTraceElement.toString()).append("\n");
            }
            BungeeMain.Logs.severe("Stack Trace: " + stacktrace.toString());
            status = new TextComponent("Unable to fetch status!");
            status.setColor(ChatColor.RED);
        }
        executorService1.shutdown();
        BungeeMain.Logs.severe("ERROR: Status creation took too long! Unable to fetch " + targetname + "'s status!");
        BungeeMain.Logs.severe("Error message: ");
        BungeeMain.Logs.severe("Stack Trace: ");
        player.sendMessage(new ComponentBuilder("|--------").strikethrough(true).color(ChatColor.RED).append(targetname + "'s Player Info").strikethrough(false).color(ChatColor.GREEN).append("--------|").strikethrough(true).color(ChatColor.RED).create());
        player.sendMessage(new ComponentBuilder("UUID: ").color(ChatColor.RED).append(info.get("uuid")).color(ChatColor.GREEN).create());
        if (info.containsKey("prefix"))
            player.sendMessage(new ComponentBuilder("Rank: ").color(ChatColor.RED).append(ChatColor.translateAlternateColorCodes('&', info.get("prefix"))).create());
        TextComponent statusTitle = new TextComponent("Status: ");
        statusTitle.setColor(ChatColor.RED);
        statusTitle.addExtra(status);
        player.sendMessage(statusTitle);
        if (info.containsKey("alts") && player.hasPermission("punisher.alts"))
            player.sendMessage(new ComponentBuilder("Alts: ").color(ChatColor.RED).append(info.get("alts")).color(ChatColor.GREEN).create());
        if (info.containsKey("ip") && player.hasPermission("punisher.alts.ip"))
            player.sendMessage(new ComponentBuilder("Ip: ").color(ChatColor.RED).append(info.get("ip")).color(ChatColor.GREEN).create());
        if (info.containsKey("firstjoin"))
            player.sendMessage(new ComponentBuilder("First Join Date: ").color(ChatColor.RED).append(info.get("firstjoin")).color(ChatColor.GREEN).create());
        if (info.containsKey("lastlogin"))
            player.sendMessage(new ComponentBuilder("Last Login: ").color(ChatColor.RED).append(info.get("lastlogin")).color(ChatColor.GREEN).create());
        if (info.containsKey("lastlogout"))
            player.sendMessage(new ComponentBuilder("Last Logout: ").color(ChatColor.RED).append(info.get("lastlogout")).color(ChatColor.GREEN).create());
        if (info.containsKey("lastserver"))
            player.sendMessage(new ComponentBuilder("Last Server Played: ").color(ChatColor.RED).append(info.get("lastserver")).color(ChatColor.GREEN).create());
        if (info.containsKey("reputation"))
            player.sendMessage(new ComponentBuilder("Reputation: ").color(ChatColor.RED).append(info.get("reputation")).color(ChatColor.GREEN).create());
        if (info.containsKey("punishmentsreceived") && player.hasPermission("punisher.history"))
            player.sendMessage(new ComponentBuilder("Punishments Received: ").color(ChatColor.RED).append(info.get("punishmentsreceived")).color(ChatColor.GREEN).create());
        if (info.containsKey("punishmentsgiven") && player.hasPermission("punisher.staffhistory"))
            player.sendMessage(new ComponentBuilder("Punishments Given: ").color(ChatColor.RED).append(info.get("punishmentsgiven")).color(ChatColor.GREEN).create());
        if (info.containsKey("punishlevel"))
            player.sendMessage(new ComponentBuilder("Punish Permission level: ").color(ChatColor.RED).append(info.get("punishlevel")).color(ChatColor.GREEN).create());
        if (info.containsKey("punishmentbypass"))
            player.sendMessage(new ComponentBuilder("Can Bypass Punishments: ").color(ChatColor.RED).append(info.get("punishmentbypass")).create());
        if (!notes.isEmpty()) {
            player.sendMessage(new ComponentBuilder("Notes for " + targetname + ":").color(ChatColor.RED).create());
            for (String note : notes) {
                player.sendMessage(new ComponentBuilder("    " + (notes.indexOf(note) + 1) + ". ").color(ChatColor.GREEN).append(note).color(ChatColor.GREEN).create());
            }
        }else{
            player.sendMessage(new ComponentBuilder("No available notes for " + targetname).color(ChatColor.RED).create());
        }
    }
}
