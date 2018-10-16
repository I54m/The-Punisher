package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.fetchers.UserFetcher;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Contexts;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.caching.MetaData;
import me.lucko.luckperms.api.context.ContextManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class StaffCommand extends Command {
    public StaffCommand() {
        super("staff", null, "onlinestaff", "staffonline");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender.hasPermission("punisher.staff")){
            ArrayList<ProxiedPlayer> staff = new ArrayList<>();
            commandSender.sendMessage(new ComponentBuilder("|------------- ").color(ChatColor.GREEN).strikethrough(true).append("Online Staff Members").color(ChatColor.RED).strikethrough(false)
                    .append(" -------------|").color(ChatColor.GREEN).strikethrough(true).create());
            for (ProxiedPlayer all : ProxyServer.getInstance().getPlayers()){
                if (all.hasPermission("punisher.staff")){
                    staff.add(all);
                    UUID uuid = all.getUniqueId();
                    User user = LuckPerms.getApi().getUser(all.getName());
                    if (user == null) {
                        UserFetcher userFetcher = new UserFetcher();
                        userFetcher.setUuid(uuid);
                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                        Future<User> userFuture = executorService.submit(userFetcher);
                        try {
                            user = userFuture.get(5, TimeUnit.SECONDS);
                        }catch (Exception e){
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
                    String prefixText = ChatColor.translateAlternateColorCodes('&', prefix);
                    if (!BungeeMain.StaffHidden.contains(all.getUniqueId().toString()))
                        commandSender.sendMessage(new ComponentBuilder(prefixText + " ").append(all.getName()).color(ChatColor.RED).append(" - " + all.getServer().getInfo().getName()).color(ChatColor.GRAY).create());
                    else
                        commandSender.sendMessage(new ComponentBuilder(prefixText + " ").strikethrough(true).append(all.getName()).strikethrough(true).color(ChatColor.RED)
                                .append(" - HIDDEN").color(ChatColor.GRAY).strikethrough(false).create());

                }
            }
            if (staff.toArray().length <= 0){
                commandSender.sendMessage(new ComponentBuilder("No Staff Online!").color(ChatColor.RED).strikethrough(true).create());
            }
            commandSender.sendMessage(new ComponentBuilder("|---------------------------------------------|").color(ChatColor.GREEN).strikethrough(true).create());
            //player can see crossed out people and is a staff member
        }else{
            ArrayList<ProxiedPlayer> staff = new ArrayList<>();
            commandSender.sendMessage(new ComponentBuilder("|------------- ").color(ChatColor.GREEN).strikethrough(true).append("Online Staff Members").color(ChatColor.RED).strikethrough(false)
                    .append(" -------------|").color(ChatColor.GREEN).strikethrough(true).create());
            for (ProxiedPlayer all : ProxyServer.getInstance().getPlayers()){
                if (all.hasPermission("punisher.staff")){
                    if (!BungeeMain.StaffHidden.contains(all.getUniqueId().toString())) {
                        staff.add(all);
                        UUID uuid = all.getUniqueId();
                        User user = LuckPerms.getApi().getUser(all.getName());
                        if (user == null) {
                            UserFetcher userFetcher = new UserFetcher();
                            userFetcher.setUuid(uuid);
                            ExecutorService executorService = Executors.newSingleThreadExecutor();
                            Future<User> userFuture = executorService.submit(userFetcher);
                            try {
                                user = userFuture.get(5, TimeUnit.SECONDS);
                            }catch (Exception e){
                                BungeeMain.Logs.severe("ERROR: Luckperms was unable to fetch permission data on: " + all.getName());
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
                        String prefixText = ChatColor.translateAlternateColorCodes('&', prefix);
                        commandSender.sendMessage(new ComponentBuilder(prefixText + " ").append(all.getName()).color(ChatColor.RED).append(" - " + all.getServer().getInfo().getName()).create());
                    }
                }
            }
            if (staff.toArray().length <= 0){
                commandSender.sendMessage(new ComponentBuilder(" No Staff Online!").color(ChatColor.RED).create());
            }
            commandSender.sendMessage(new ComponentBuilder("|---------------------------------------------|").color(ChatColor.GREEN).strikethrough(true).create());
            //player can only see non-crossed out people only
        }
    }
}
