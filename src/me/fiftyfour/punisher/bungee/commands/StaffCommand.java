package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.systems.Permissions;
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
                        user = Permissions.giveMeADamnUser(uuid);
                        if(user == null){
                            throw new IllegalStateException();
                        }
                    }
                    ContextManager cm = LuckPerms.getApi().getContextManager();
                    Contexts contexts = cm.lookupApplicableContexts(user).orElse(cm.getStaticContexts());
                    MetaData metaData = user.getCachedData().getMetaData(contexts);
                    String prefix = metaData.getPrefix();
                    if (prefix == null)
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
                            user = Permissions.giveMeADamnUser(uuid);
                            if (user == null) {
                                throw new IllegalStateException();
                            }
                        }
                        ContextManager cm = LuckPerms.getApi().getContextManager();
                        Contexts contexts = cm.lookupApplicableContexts(user).orElse(cm.getStaticContexts());
                        MetaData metaData = user.getCachedData().getMetaData(contexts);
                        String prefix = metaData.getPrefix();
                        if (prefix == null)
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
