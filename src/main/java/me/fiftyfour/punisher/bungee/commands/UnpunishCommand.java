package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.managers.PunishmentManager;
import me.fiftyfour.punisher.bungee.objects.Punishment;
import me.fiftyfour.punisher.universal.fetchers.NameFetcher;
import me.fiftyfour.punisher.universal.fetchers.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.*;

public class UnpunishCommand extends Command {
    private BungeeMain plugin = BungeeMain.getInstance();
    private String targetuuid;
    private int sqlfails = 0;
    private PunishmentManager punishmentManager = PunishmentManager.getInstance();

    public UnpunishCommand() {
        super("unpunish", "punisher.unpunish", "unpun");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            if (strings.length <= 1) {
                player.sendMessage(new ComponentBuilder(plugin.prefix).append("Remove a punishment from a player's history (CaSe SeNsItIvE!!)").color(ChatColor.RED).append("\nUsage: /unpunish <player name> <reason>").color(ChatColor.WHITE).create());
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
            StringBuilder reason = new StringBuilder();
            for (int i = 1; i < strings.length; i++) {
                reason.append(strings[i]);
                if (i + 1 < strings.length)reason.append(" ");
            }
            String reasonString = reason.toString().replace("-", "_").replace(" ", "_").replace("/", "_");
            ArrayList<String> reasonsList;
            reasonsList = new ArrayList<>();
            reasonsList.add("Minor_Chat_Offence");
            reasonsList.add("Major_Chat_Offence");
            reasonsList.add("DDoS_DoX_Threats");
            reasonsList.add("Inapproprioate_Link");
            reasonsList.add("Scamming");
            reasonsList.add("X_Raying");
            reasonsList.add("AutoClicker");
            reasonsList.add("Fly_Speed_Hacking");
            reasonsList.add("Malicious_PvP_Hacks");
            reasonsList.add("Server_Advertisment");
            reasonsList.add("Greifing");
            reasonsList.add("Exploiting");
            reasonsList.add("Tpa_Trapping");
            reasonsList.add("Impersonation");
            if (reasonString.toLowerCase().contains("manual")) {
                player.sendMessage(new ComponentBuilder(plugin.prefix).append("Manual Punishments may not be removed from history!").color(ChatColor.RED).create());
                return;
            }
            if (!(reasonsList.contains(reasonString))) {
                player.sendMessage(new ComponentBuilder(plugin.prefix).append("That is not a punishment reason!").color(ChatColor.RED).create());
                player.sendMessage(new ComponentBuilder(plugin.prefix).append("Reasons are as follows (Case Sensitive):").color(ChatColor.RED).create());
                player.sendMessage(new ComponentBuilder(plugin.prefix).append(reasonsList.toString().replace("[", "").replace("]", "")).color(ChatColor.RED).create());
                return;
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
                    Punishment.Reason Reason = Punishment.Reason.valueOf(reasonString);
                    punishmentManager.revoke(new Punishment(Reason, null, null, Punishment.Type.ALL, targetuuid, null), player, targetname, true, true);
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
                        e.printStackTrace();
                        BungeeMain.Logs.severe(this.getName() + " has thrown an exception more than 5 times!");
                        BungeeMain.Logs.severe("Disabling command to prevent further damage to database!");
                        StringBuilder stacktrace = new StringBuilder();
                        for (StackTraceElement stackTraceElement : e.getStackTrace()){
                            stacktrace.append(stackTraceElement.toString()).append("\n");
                        }
                        BungeeMain.Logs.severe(stacktrace.toString());
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