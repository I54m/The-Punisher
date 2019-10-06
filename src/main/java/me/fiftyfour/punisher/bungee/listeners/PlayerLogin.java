package me.fiftyfour.punisher.bungee.listeners;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.chats.StaffChat;
import me.fiftyfour.punisher.bungee.handlers.ErrorHandler;
import me.fiftyfour.punisher.bungee.managers.PunishmentManager;
import me.fiftyfour.punisher.bungee.objects.Punishment;
import me.fiftyfour.punisher.universal.exceptions.DataFecthException;
import me.fiftyfour.punisher.universal.exceptions.PunishmentsDatabaseException;
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
        UUID uuid = event.getConnection().getUniqueId();
        String fetcheduuid = uuid.toString().replace("-", "");
        PendingConnection connection = event.getConnection();
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
                    } catch (DataFecthException dfe) {
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
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            try {
                throw new PunishmentsDatabaseException("Revoking punishment", targetName, this.getClass().getName(), e);
            } catch (PunishmentsDatabaseException pde) {
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.log(pde);
                errorHandler.loginError(event);
            }
        }
        //todo improve async stuff, might be improved now?
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try {
                //update ip and make sure there is one in the database
                String sqlip = "SELECT * FROM `altlist` WHERE UUID='" + fetcheduuid + "'";
                PreparedStatement stmtip = plugin.connection.prepareStatement(sqlip);
                ResultSet resultsip = stmtip.executeQuery();
                if (resultsip.next()) {
                    if (!resultsip.getString("ip").equals(connection.getAddress().getHostString())) {
                        String oldip = resultsip.getString("ip");
                        String sqlipadd = "UPDATE `altlist` SET `ip`='" + connection.getAddress().getAddress().getHostAddress() + "' WHERE `ip`='" + oldip + "' ;";
                        PreparedStatement stmtipadd = plugin.connection.prepareStatement(sqlipadd);
                        stmtipadd.executeUpdate();
                        stmtipadd.close();
                    }
                } else {
                    String sql1 = "INSERT INTO `altlist` (`UUID`, `ip`) VALUES ('" + fetcheduuid + "', '" + connection.getAddress().getAddress().getHostAddress() + "');";
                    PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                    stmt1.executeUpdate();
                    stmt1.close();
                }
                stmtip.close();
                resultsip.close();
            } catch (SQLException sqle) {
                try {
                    throw new PunishmentsDatabaseException("Updating ip in altlist", targetName, PlayerLogin.class.getName(), sqle);
                } catch (PunishmentsDatabaseException pde) {
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.log(pde);
                    errorHandler.loginError(event);
                }
            }

            try {
                //update ip hist
                String sql = "SELECT * FROM `iphist` WHERE UUID='" + fetcheduuid + "'";
                PreparedStatement stmt = plugin.connection.prepareStatement(sql);
                ResultSet results = stmt.executeQuery();
                if (results.next()) {
                    if (!results.getString("ip").equals(connection.getAddress().getAddress().getHostAddress())) {
                        String addip = "INSERT INTO `iphist` (`UUID`, `date`, `ip`) VALUES ('" + fetcheduuid + "', '" + System.currentTimeMillis() + "', '" + connection.getAddress().getAddress().getHostAddress() + "');";
                        PreparedStatement addipstmt = plugin.connection.prepareStatement(addip);
                        addipstmt.executeUpdate();
                        addipstmt.close();
                    }
                } else {
                    String addip = "INSERT INTO `iphist` (`UUID`, `date`, `ip`) VALUES ('" + fetcheduuid + "', '" + System.currentTimeMillis() + "', '" + connection.getAddress().getAddress().getHostAddress() + "');";
                    PreparedStatement addipstmt = plugin.connection.prepareStatement(addip);
                    addipstmt.executeUpdate();
                    addipstmt.close();
                }
            } catch (SQLException sqle) {
                try {
                    throw new PunishmentsDatabaseException("Updating ip in iphist", targetName, PlayerLogin.class.getName(), sqle);
                } catch (PunishmentsDatabaseException pde) {
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.log(pde);
                    errorHandler.loginError(event);
                }
            }

            if (!BungeeMain.RepStorage.contains(fetcheduuid)) {
                BungeeMain.RepStorage.set(fetcheduuid, 5.0);
            }

            try {
                //check for banned alts
                ArrayList<String> altslist = new ArrayList<>();
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
                    for (String alts : new ArrayList<>(altslist)) {
                        if (!punishmngr.isBanned(alts)) {
                            altslist.remove(alts);
                        }
                    }
                    StringBuilder bannedalts = new StringBuilder();
                    int i = 0;
                    for (String alts : altslist) {
                        bannedalts.append(NameFetcher.getName(alts));
                        if (!(i + 1 >= altslist.size())) {
                            bannedalts.append(", ");
                        }
                        i++;
                    }
                    if (bannedalts.length() > 0) {
                        plugin.getProxy().getScheduler().schedule(plugin, () -> StaffChat.sendMessage(targetName + " Might have banned alts: " + bannedalts.toString()), 5, TimeUnit.SECONDS);
                    }
                }
            } catch (SQLException sqle) {
                try {
                    throw new PunishmentsDatabaseException("Checking for banned alts", targetName, PlayerLogin.class.getName(), sqle);
                } catch (PunishmentsDatabaseException pde) {
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.log(pde);
                    errorHandler.loginError(event);
                }
            }
        });
    }
}