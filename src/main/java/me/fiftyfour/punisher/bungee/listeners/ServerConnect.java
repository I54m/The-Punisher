package me.fiftyfour.punisher.bungee.listeners;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.universal.systems.UpdateChecker;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ServerConnect implements Listener {

    private BungeeMain plugin = BungeeMain.getInstance();
    public static int lastJoinId = 0;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerConnect(ServerConnectEvent event){
        ProxiedPlayer player = event.getPlayer();
        if (event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            if (BungeeMain.update && player.hasPermission("punisher.admin")) {
                player.sendMessage(new ComponentBuilder(plugin.prefix).append("Update checker found an update, current version: " + plugin.getDescription().getVersion() + " latest version: "
                        + UpdateChecker.getCurrentVersion() + " this update was released on: " + UpdateChecker.getRealeaseDate()).color(ChatColor.RED).create());
                player.sendMessage(new ComponentBuilder(plugin.prefix).append("This may fix some bugs and enhance features, You will also no longer receive support for this version!").color(ChatColor.RED).create());
            }
        }
        if (BungeeMain.InfoConfig.contains(player.getUniqueId().toString().replace("-", ""))) {
            if (event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
                BungeeMain.InfoConfig.set(player.getUniqueId().toString().replace("-", "") + ".lastlogin", System.currentTimeMillis());
                if (event.getTarget() != null)
                    BungeeMain.InfoConfig.set(player.getUniqueId().toString().replace("-", "") + ".lastserver", event.getTarget().getName());
                BungeeMain.saveInfo();
            } else if (event.getTarget() != null) {
                BungeeMain.InfoConfig.set(player.getUniqueId().toString().replace("-", "") + ".lastserver", event.getTarget().getName());
                BungeeMain.saveInfo();
            }
        }else{
            Date date = new Date();
            date.setTime(System.currentTimeMillis());
            DateFormat df = new SimpleDateFormat("dd MMM YYYY, hh:mm (Z)");
            df.setTimeZone(TimeZone.getDefault());
            BungeeMain.InfoConfig.set(player.getUniqueId().toString().replace("-", "") + ".firstjoin", df.format(date));
            BungeeMain.InfoConfig.set(player.getUniqueId().toString().replace("-", "") + ".joinid", (lastJoinId + 1));
            BungeeMain.InfoConfig.set("lastjoinid", (lastJoinId + 1));
            BungeeMain.InfoConfig.set(String.valueOf((lastJoinId + 1)), player.getUniqueId().toString().replace("-", ""));
            lastJoinId++;
            BungeeMain.InfoConfig.set(player.getUniqueId().toString().replace("-", "") + ".lastlogin", System.currentTimeMillis());
            if (event.getTarget() != null)
                BungeeMain.InfoConfig.set(player.getUniqueId().toString().replace("-", "") + ".lastserver", event.getTarget().getName());
            BungeeMain.saveInfo();
        }
    }
}
