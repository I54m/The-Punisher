package me.fiftyfour.punisher.bungee.events;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.chats.StaffChat;
import me.fiftyfour.punisher.fetchers.NameFetcher;
import me.fiftyfour.punisher.systems.UpdateChecker;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class onServerConnect implements Listener {
    private BungeeMain plugin = BungeeMain.getInstance();
    private ResultSet results;
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void ServerConnectEvent(final ServerConnectEvent event) {
        if (!event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)){
            if (!event.getReason().equals(ServerConnectEvent.Reason.UNKNOWN)) return;
        }
        UUID uuid = event.getPlayer().getUniqueId();
        String fetcheduuid = uuid.toString().replace("-", "");
        ArrayList<String> altslist = new ArrayList<>();
        ProxiedPlayer player = event.getPlayer();
        if (BungeeMain.update && player.hasPermission("punisher.admin")){
            player.sendMessage(new ComponentBuilder(prefix).append("Update checker found an update, current version: " + plugin.getDescription().getVersion() + " latest version: "
                    + UpdateChecker.getCurrentVersion()  + " this update was released on: " + UpdateChecker.getRealeaseDate()).color(ChatColor.RED).create());
            player.sendMessage(new ComponentBuilder(prefix).append("This may fix some bugs and enhance features, You will also no longer receive support for this version!").color(ChatColor.RED).create());
        }
        if (!BungeeMain.RepStorage.contains(fetcheduuid)){
            BungeeMain.RepStorage.set(fetcheduuid, 5.0);
        }
        try {
            String sql = "SELECT * FROM `bans` WHERE UUID='" + fetcheduuid + "'";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            results = stmt.executeQuery();
            if (results.next()){
                if (player.hasPermission("punisher.bypass")) {
                    String sql1 = "DELETE FROM `bans` WHERE `UUID`='" + fetcheduuid + "' ;";
                    PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                    stmt1.executeUpdate();
                    stmt1.close();
                    BungeeMain.Logs.info(player.getName() + " Bypassed their ban and were unbanned");
                    StaffChat.sendMessage(player.getName() + " Bypassed their ban, Unbanning...");
                    return;
                }
                long bantime = results.getLong("Length");
                if (System.currentTimeMillis() > bantime) {
                    String sql1 = "DELETE FROM `bans` WHERE `UUID`='" + fetcheduuid + "' ;";
                    PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                    stmt1.executeUpdate();
                    stmt1.close();
                    BungeeMain.Logs.info(player.getName() + "'s ban expired so they were unbanned");
                } else {
                    event.setCancelled(true);
                    kick(event.getPlayer());
                    return;
                }
            }
            stmt.close();
            results.close();
            plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        //update ip and make sure there is one in the database
                        String sqlip = "SELECT * FROM `iplist` WHERE UUID='" + fetcheduuid + "'";
                        PreparedStatement stmtip = plugin.connection.prepareStatement(sqlip);
                        ResultSet resultsip = stmtip.executeQuery();
                        if (resultsip.next()){
                            if (!resultsip.getString("ip").equals(player.getAddress().getHostString())){
                                String oldip = resultsip.getString("ip");
                                String sqlipadd = "UPDATE `iplist` SET `ip`='" + player.getAddress().getHostString() + "' WHERE `ip`='" + oldip + "' ;";
                                PreparedStatement stmtipadd = plugin.connection.prepareStatement(sqlipadd);
                                stmtipadd.executeUpdate();
                                stmtipadd.close();
                            }
                        }else{
                            String sql1 = "INSERT INTO `iplist` (`UUID`, `ip`) VALUES ('"+ fetcheduuid + "', '" + player.getAddress().getHostString() + "');";
                            PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                            stmt1.executeUpdate();
                            stmt1.close();
                        }
                        stmtip.close();
                        resultsip.close();
                        //check for banned alts
                        String ip = player.getAddress().getHostString();
                        String sqlip1 = "SELECT * FROM `iplist` WHERE ip='" + ip + "'";
                        PreparedStatement stmtip1 = plugin.connection.prepareStatement(sqlip1);
                        ResultSet resultsip1 = stmtip1.executeQuery();
                        while (resultsip1.next()) {
                            String concacc = resultsip1.getString("uuid");
                            altslist.add(concacc);
                        }
                        stmtip1.close();
                        resultsip1.close();
                        altslist.remove(player.getUniqueId().toString().replace("-", ""));
                        if (!altslist.isEmpty()) {
                            ArrayList<String> bannedalts = new ArrayList<>();
                            for (String alts : altslist) {
                                String sql1 = "SELECT * FROM `bans` WHERE UUID='" + alts + "'";
                                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                                ResultSet results1 = stmt1.executeQuery();
                                if (results1.next()) {
                                    bannedalts.add(NameFetcher.getName(results1.getString("uuid")));
                                }
                                stmt1.close();
                                results1.close();
                            }
                            if (!bannedalts.isEmpty()) {
                                StaffChat.sendMessage(player.getName() + " Might have banned alts: " + bannedalts.toString().replace("[", "").replace("]", ""));
                            }
                        }
                    }catch (SQLException e){
                        plugin.mysqlfail(e);
                    }
                }
            });
        } catch (SQLException e) {
            plugin.mysqlfail(e);
            if(plugin.testConnectionManual())
                plugin.getProxy().getPluginManager().callEvent(new ServerConnectEvent(event.getPlayer(), event.getTarget(), event.getReason()));
        }
    }

    private void kick(ProxiedPlayer player) {
        try {
            Long bantime = results.getLong("Length");
            Long banleftmillis = bantime - System.currentTimeMillis();
            int daysleft = (int) (banleftmillis / (1000 * 60 * 60 * 24));
            int hoursleft = (int) (banleftmillis / (1000 * 60 * 60) % 24);
            int minutesleft = (int) (banleftmillis / (1000 * 60) % 60);
            int secondsleft = (int) (banleftmillis / 1000 % 60);
            String reason = results.getString("Reason");
            if (daysleft > 500) {
                String banMessage = BungeeMain.PunisherConfig.getString("PermBan Message").replace("%days%", String.valueOf(daysleft))
                        .replace("%hours%", String.valueOf(hoursleft)).replace("%minutes%", String.valueOf(minutesleft))
                        .replace("%seconds%", String.valueOf(secondsleft)).replace("%reason%", reason);
                player.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', banMessage)));
            } else {
                String banMessage = BungeeMain.PunisherConfig.getString("TempBan Message").replace("%days%", String.valueOf(daysleft))
                        .replace("%hours%", String.valueOf(hoursleft)).replace("%minutes%", String.valueOf(minutesleft))
                        .replace("%seconds%", String.valueOf(secondsleft)).replace("%reason%", reason);
                player.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', banMessage)));
            }
        } catch (SQLException e) {
            plugin.mysqlfail(e);
            if (plugin.testConnectionManual())
                kick(player);
        }
    }
}