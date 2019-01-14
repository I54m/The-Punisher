package me.fiftyfour.punisher.bungee.chats;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.universal.fetchers.UserFetcher;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Contexts;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.caching.MetaData;
import me.lucko.luckperms.api.context.ContextManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class AdminChat extends Command {

    public AdminChat() {
        super("ac", "punisher.adminchat", "adminchat");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            if (strings.length == 0) {
                player.sendMessage(new ComponentBuilder("Send a message to all admin members").color(ChatColor.RED).append("\nUsage: /adminchat <message>").color(ChatColor.WHITE).create());
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (String arg : strings){
                sb.append(arg).append(" ");
            }
            UUID uuid = player.getUniqueId();
            User user = LuckPerms.getApi().getUser(player.getName());
            if (user == null) {
                UserFetcher userFetcher = new UserFetcher();
                userFetcher.setUuid(uuid);
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                Future<User> userFuture = executorService.submit(userFetcher);
                try {
                    user = userFuture.get(5, TimeUnit.SECONDS);
                }catch (Exception e){
                    BungeeMain.Logs.severe("ERROR: Luckperms was unable to fetch permission data on: " + player.getName());
                    BungeeMain.Logs.severe("This Error was encountered when trying to get a prefix so to avoid issues the prefix was set to \"\"");
                    BungeeMain.Logs.severe("Error message: " + e.getMessage());
                    StringBuilder stacktrace = new StringBuilder();
                    for (StackTraceElement stackTraceElement : e.getStackTrace()){
                        stacktrace.append(stackTraceElement.toString()).append("\n");
                    }
                    BungeeMain.Logs.severe("Stack Trace: " + stacktrace.toString());
                    user = null;
                }
                executorService.shutdown();
            }
            String prefix;
            if (user != null) {
                ContextManager cm = LuckPerms.getApi().getContextManager();
                Contexts contexts = cm.lookupApplicableContexts(user).orElse(cm.getStaticContexts());
                MetaData metaData = user.getCachedData().getMetaData(contexts);
                prefix = metaData.getPrefix();
                if (prefix == null) {
                    prefix = "";
                }
            }else
                prefix = "";
            BaseComponent[] messagetosend = new ComponentBuilder("[").color(ChatColor.DARK_GRAY).append("AC").color(ChatColor.DARK_RED).bold(true).append("]").color(ChatColor.DARK_GRAY).bold(false)
                    .append(" ").color(ChatColor.RESET).append(ChatColor.translateAlternateColorCodes('&', prefix + " ")).bold(false).append(player.getName() + ": " + sb)
                    .color(ChatColor.DARK_RED).bold(false).create();
            for (ProxiedPlayer all : ProxyServer.getInstance().getPlayers()) {
                if (all.hasPermission("punisher.adminchat")) {
                    all.sendMessage(messagetosend);
                }
            }
        } else {
            commandSender.sendMessage(new TextComponent("You must be a player to use this command!"));
        }
    }
    public static void sendMessage(String message){
        BaseComponent[] messagetosend = new ComponentBuilder("[").color(ChatColor.DARK_GRAY).append("AC").color(ChatColor.DARK_RED).bold(true).append("]").color(ChatColor.DARK_GRAY).bold(false)
                .append(" ").color(ChatColor.RESET).bold(false).append(message).color(ChatColor.DARK_RED).bold(false).create();
        for (ProxiedPlayer all : ProxyServer.getInstance().getPlayers()) {
            if (all.hasPermission("punisher.adminchat")) {
                all.sendMessage(messagetosend);
            }
        }
    }
}
