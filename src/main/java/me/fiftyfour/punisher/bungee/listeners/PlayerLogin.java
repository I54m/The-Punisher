package me.fiftyfour.punisher.bungee.listeners;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.chats.StaffChat;
import me.fiftyfour.punisher.bungee.exceptions.DataFecthException;
import me.fiftyfour.punisher.bungee.handlers.ErrorHandler;
import me.fiftyfour.punisher.bungee.managers.PunishmentManager;
import me.fiftyfour.punisher.bungee.objects.Punishment;
import me.fiftyfour.punisher.universal.fetchers.NameFetcher;
import me.fiftyfour.punisher.universal.fetchers.UserFetcher;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Contexts;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.caching.PermissionData;
import me.lucko.luckperms.api.context.ContextManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class PlayerLogin implements Listener {
    private BungeeMain plugin = BungeeMain.getInstance();
    private PunishmentManager punishmngr = PunishmentManager.getInstance();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(LoginEvent event) {
        event.registerIntent(plugin);
        UUID uuid = event.getConnection().getUniqueId();
        String fetcheduuid = uuid.toString().replace("-", "");
        PendingConnection connection = event.getConnection();
        ArrayList<String> altslist = new ArrayList<>();
        String targetName = NameFetcher.getName(fetcheduuid);
        try {
            User user = LuckPerms.getApi().getUser(uuid);
            if (user == null) {
                UserFetcher userFetcher = new UserFetcher();
                userFetcher.setUuid(uuid);
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                Future<User> userFuture = executorService.submit(userFetcher);
                try {
                    user = userFuture.get(5, TimeUnit.SECONDS);
                } catch (Exception e) {
                    try {
                        throw new DataFecthException("User instance required for ban bypass check", targetName, "User Instance", PlayerLogin.class.getName(), e);
                    }catch (DataFecthException dfe){
                        ErrorHandler errorHandler = ErrorHandler.getInstance();
                        errorHandler.log(dfe);
                        errorHandler.loginError(event);
                    }
                    executorService.shutdown();
                    return;
                }
                executorService.shutdown();
            }
            if (user == null) {
                event.setCancelled(true);
                event.setCancelReason(new TextComponent(ChatColor.RED + "ERROR: Luckperms was unable to fetch your permission data!\n If this error persists please contact an admin+"));
                event.completeIntent(plugin);
                return;
            }
            ContextManager cm = LuckPerms.getApi().getContextManager();
            Contexts contexts = cm.lookupApplicableContexts(user).orElse(cm.getStaticContexts());
            PermissionData permissionData = user.getCachedData().getPermissionData(contexts);
            if (punishmngr.isBanned(fetcheduuid)) {
                if (permissionData.getPermissionValue("punisher.bypass").asBoolean()) {
                    punishmngr.revoke(punishmngr.getBan(fetcheduuid), null, targetName, true, false);
                    BungeeMain.Logs.info(user.getName() + " Bypassed their ban and were unbanned");
                    plugin.getProxy().getScheduler().schedule(plugin, () ->
                                    StaffChat.sendMessage(targetName + " Bypassed their ban, Unbanning...")
                            , 5, TimeUnit.SECONDS);
                } else {
                    Punishment ban = punishmngr.getBan(fetcheduuid);
                    if (System.currentTimeMillis() > ban.getDuration()) {
                        punishmngr.revoke(punishmngr.getBan(fetcheduuid), null, targetName, false, false);
                        BungeeMain.Logs.info(user.getName() + "'s ban expired so they were unbanned");
                    } else {
                        long banleftmillis = ban.getDuration() - System.currentTimeMillis();
                        int daysleft = (int) (banleftmillis / (1000 * 60 * 60 * 24));
                        int hoursleft = (int) (banleftmillis / (1000 * 60 * 60) % 24);
                        int minutesleft = (int) (banleftmillis / (1000 * 60) % 60);
                        int secondsleft = (int) (banleftmillis / 1000 % 60);
                        String reason = ban.getMessage();
                        if (daysleft > 730) {
                            String banMessage = BungeeMain.PunisherConfig.getString("PermBan Message").replace("%days%", String.valueOf(daysleft))
                                    .replace("%hours%", String.valueOf(hoursleft)).replace("%minutes%", String.valueOf(minutesleft))
                                    .replace("%seconds%", String.valueOf(secondsleft)).replace("%reason%", reason);
                            event.setCancelled(true);
                            event.setCancelReason(new TextComponent(ChatColor.translateAlternateColorCodes('&', banMessage)));
                        } else {
                            String banMessage = BungeeMain.PunisherConfig.getString("TempBan Message").replace("%days%", String.valueOf(daysleft))
                                    .replace("%hours%", String.valueOf(hoursleft)).replace("%minutes%", String.valueOf(minutesleft))
                                    .replace("%seconds%", String.valueOf(secondsleft)).replace("%reason%", reason);
                            event.setCancelled(true);
                            event.setCancelReason(new TextComponent(ChatColor.translateAlternateColorCodes('&', banMessage)));
                        }
                        event.completeIntent(plugin);
                        return;
                    }
                }
            }
            event.completeIntent(plugin);
            plugin.getProxy().getScheduler().runAsync(plugin, () -> {
                plugin.getProxy().getScheduler().runAsync(plugin, () -> {
                    try {
                        //update ip and make sure there is one in the database
                        String sqlip = "SELECT * FROM `altlist` WHERE UUID='" + fetcheduuid + "'";
                        PreparedStatement stmtip = plugin.connection.prepareStatement(sqlip);
                        ResultSet resultsip = stmtip.executeQuery();
                        if (resultsip.next()) {
                            if (!resultsip.getString("ip").equals(connection.getAddress().getHostString())) {
                                String oldip = resultsip.getString("ip");
                                String sqlipadd = "UPDATE `altlist` SET `ip`='" + connection.getAddress().getHostString() + "' WHERE `ip`='" + oldip + "' ;";
                                PreparedStatement stmtipadd = plugin.connection.prepareStatement(sqlipadd);
                                stmtipadd.executeUpdate();
                                stmtipadd.close();
                            }
                        } else {
                            String sql1 = "INSERT INTO `altlist` (`UUID`, `ip`) VALUES ('" + fetcheduuid + "', '" + connection.getAddress().getHostString() + "');";
                            PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                            stmt1.executeUpdate();
                            stmt1.close();
                        }
                        stmtip.close();
                        resultsip.close();
                    } catch (SQLException sqle) {
                        sqlException(sqle, event, targetName);
                    }
                });
                plugin.getProxy().getScheduler().runAsync(plugin, () -> {
                    try {
                        //update ip hist
                        String sql = "SELECT * FROM `iphist` WHERE UUID='" + fetcheduuid + "'";
                        PreparedStatement stmt = plugin.connection.prepareStatement(sql);
                        ResultSet results = stmt.executeQuery();
                        if (results.next()) {
                            if (!results.getString("ip").equals(connection.getAddress().getHostString())) {
                                String addip = "INSERT INTO `iphist` (`UUID`, `date`, `ip`) VALUES ('" + fetcheduuid + "', '" + System.currentTimeMillis() + "', '" + connection.getAddress().getHostString() + "');";
                                PreparedStatement addipstmt = plugin.connection.prepareStatement(addip);
                                addipstmt.executeUpdate();
                                addipstmt.close();
                            }
                        } else {
                            String addip = "INSERT INTO `iphist` (`UUID`, `date`, `ip`) VALUES ('" + fetcheduuid + "', '" + System.currentTimeMillis() + "', '" + connection.getAddress().getHostString() + "');";
                            PreparedStatement addipstmt = plugin.connection.prepareStatement(addip);
                            addipstmt.executeUpdate();
                            addipstmt.close();
                        }
                    } catch (SQLException sqle) {
                        sqlException(sqle, event, targetName);
                    }
                });
                if (!BungeeMain.RepStorage.contains(fetcheduuid)) {
                    BungeeMain.RepStorage.set(fetcheduuid, 5.0);
                }
                try {
                    //check for banned alts
                    String ip = connection.getAddress().getHostString();
                    String sqlip1 = "SELECT * FROM `altlist` WHERE ip='" + ip + "'";
                    PreparedStatement stmtip1 = plugin.connection.prepareStatement(sqlip1);
                    ResultSet resultsip1 = stmtip1.executeQuery();
                    while (resultsip1.next()) {
                        String concacc = resultsip1.getString("uuid");
                        altslist.add(concacc);
                    }
                    stmtip1.close();
                    resultsip1.close();
                    altslist.remove(uuid.toString().replace("-", ""));
                    if (!altslist.isEmpty()) {
                        ArrayList<String> bannedalts = new ArrayList<>();
                        for (String alts : altslist) {
                            if (punishmngr.isBanned(alts)) {
                                bannedalts.add(NameFetcher.getName(alts));
                            }
                        }
                        if (!bannedalts.isEmpty()) {
                            plugin.getProxy().getScheduler().schedule(plugin, () -> StaffChat.sendMessage(targetName + " Might have banned alts: " + bannedalts.toString().replace("[", "").replace("]", "")), 5, TimeUnit.SECONDS);
                        }
                    }
                } catch (SQLException sqle) {
                    sqlException(sqle, event, targetName);
                }
            });
        } catch (SQLException e) {
            sqlException(e, event, targetName);
            event.completeIntent(plugin);
        }
    }

    private void sqlException(SQLException e, LoginEvent event, String targetName) {
        plugin.getLogger().severe(plugin.prefix + e);
        plugin.getLogger().severe(plugin.prefix + PlayerLogin.class.getName() + " has thrown an exception while processing login for: " + targetName + "!");
        plugin.getLogger().severe(plugin.prefix + "kicking player and returning to prevent further damage to database!");
        BungeeMain.Logs.severe(PlayerLogin.class.getName() + " has thrown an exception while processing login for: " + targetName + "!");
        BungeeMain.Logs.severe("kicking player and returning to prevent further damage to database!");
        event.getConnection().disconnect(new TextComponent(plugin.prefix + "\n" + ChatColor.RED + "We encountered an error while processing your login!" +
                "\nTo prevent further damage to our database we have kicked you!" +
                "\nPlease Contact an Admin+ ASAP!" +
                "\nIf you are an Admin+ please check that the database connection is functional" +
                "\nand that there are no errors in the database itself or config.yml!"));
    }
}