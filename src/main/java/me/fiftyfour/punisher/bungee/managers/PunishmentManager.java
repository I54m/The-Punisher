package me.fiftyfour.punisher.bungee.managers;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.chats.StaffChat;
import me.fiftyfour.punisher.universal.exceptions.PunishmentCalculationException;
import me.fiftyfour.punisher.universal.exceptions.PunishmentIssueException;
import me.fiftyfour.punisher.bungee.handlers.ErrorHandler;
import me.fiftyfour.punisher.bungee.objects.Punishment;
import me.fiftyfour.punisher.bungee.systems.ReputationSystem;
import me.fiftyfour.punisher.universal.fetchers.NameFetcher;
import me.fiftyfour.punisher.universal.fetchers.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PunishmentManager {

    private Map<String, Punishment> BanCache = new HashMap<>();
    private Map<String, Punishment> MuteCache = new HashMap<>();
    public ScheduledTask cacheTask;
    private BungeeMain plugin = BungeeMain.getInstance();

    private static final PunishmentManager INSTANCE = new PunishmentManager();
    private PunishmentManager(){}
    public static PunishmentManager getInstance(){return INSTANCE;}


    public void issue(@NotNull Punishment punishment, @Nullable ProxiedPlayer player, @Nullable String targetname, boolean addHistory, boolean announce, boolean minusRep) throws SQLException {
        String targetuuid = punishment.getTargetUUID();
        Punishment.Reason reason = punishment.getReason();
        Punishment.Type type = punishment.getType();
        String punisherUUID = punishment.getPunisherUUID();
        if (punisherUUID == null && player == null)
            punisherUUID = "CONSOLE";
        else if (player != null && player.isConnected() && punisherUUID == null)
            punisherUUID = player.getUniqueId().toString().replace("-", "");
        else if (punisherUUID == null)
            punisherUUID = "CONSOLE";
        if (targetname == null) NameFetcher.getName(targetuuid);
        long duration = 0;
        if (type != Punishment.Type.KICK && type != Punishment.Type.WARN) {
            if (punishment.getDuration() == 0){
                duration = (calculateDuration(reason, targetuuid) + System.currentTimeMillis());
            }
            else{
                duration = (punishment.getDuration() + System.currentTimeMillis());
            }
        }
        double repLoss;
        if (type == Punishment.Type.ALL) repLoss = calculateRepLoss(reason, Punishment.Type.ALL, targetuuid);
        else repLoss = calculateRepLoss(reason, type, targetuuid);
        createHistory(targetuuid);
        if (player == null && punishment.getPunisherUUID() != null)
            createStaffHistory(punishment.getPunisherUUID());
        else if (player != null && player.isConnected() && punishment.getPunisherUUID() == null)
            createStaffHistory(player.getUniqueId().toString().replace("_", ""));
        else if (player != null && player.isConnected() && punishment.getPunisherUUID() != null)
            createStaffHistory(punishment.getPunisherUUID());
        ProxiedPlayer target;
        switch (type) {
            case WARN: {
                target = plugin.getProxy().getPlayer(UUIDFetcher.formatUUID(targetuuid));
                if (target != null && target.isConnected()) {
                    if (BungeeMain.PunisherConfig.getBoolean("Warn Sound.Enabled")) {
                        try {
                            ByteArrayOutputStream outbytes = new ByteArrayOutputStream();
                            DataOutputStream out = new DataOutputStream(outbytes);
                            out.writeUTF("playsound");
                            out.writeUTF(BungeeMain.PunisherConfig.getString("Warn Sound.Sound"));
                            target.getServer().sendData("punisher.minor", outbytes.toByteArray());
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                    if (BungeeMain.PunisherConfig.getBoolean("Warn Title.Enabled")) {
                        String titleText = BungeeMain.PunisherConfig.getString("Warn Title.Title Message");
                        String subtitleText = BungeeMain.PunisherConfig.getString("Warn Title.Subtitle Message");
                        if (punishment.getMessage() != null) {
                            titleText = titleText.replace("%reason%", punishment.getMessage());
                            subtitleText = subtitleText.replace("%reason%", punishment.getMessage());
                        } else {
                            titleText = titleText.replace("%reason%", reason.toString());
                            subtitleText = subtitleText.replace("%reason%", reason.toString());
                        }
                        if (announce) ProxyServer.getInstance().createTitle().title(new TextComponent(ChatColor.translateAlternateColorCodes('&', titleText)))
                                .subTitle(new TextComponent(ChatColor.translateAlternateColorCodes('&', subtitleText)))
                                .fadeIn(BungeeMain.PunisherConfig.getInt("Warn Title.Fade In"))
                                .stay(BungeeMain.PunisherConfig.getInt("Warn Title.Stay")).fadeOut(BungeeMain.PunisherConfig.getInt("Warn Title.Fade Out"))
                                .send(target);
                    }
                    String warnMessage = BungeeMain.PunisherConfig.getString("Warn Message");
                    if (punishment.getMessage() != null)
                        warnMessage = warnMessage.replace("%reason%", punishment.getMessage());
                    else
                        warnMessage = warnMessage.replace("%reason%", reason.toString().replace("_", " "));
                    if (announce) target.sendMessage(new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', warnMessage)).create());
                }
                if (addHistory) {
                    incrementHistory(targetuuid, reason);
                    incrementStaffHistory(punisherUUID, reason);
                }
                String reasonMessage;
                if (punishment.getMessage() != null) {
                    reasonMessage = punishment.getMessage();
                } else {
                    reasonMessage = reason.toString();
                }
                if (player != null && player.isConnected()) {
                    BungeeMain.Logs.info(targetname + " Was Warned for: " + reasonMessage + " by: " + player.getName());
                    if (announce) StaffChat.sendMessage(player.getName() + " Warned: " + targetname + " for: " + reasonMessage);
                    else player.sendMessage(new ComponentBuilder(plugin.prefix).append("Silently Warned: " + targetname + " for: " + reasonMessage).color(ChatColor.RED).create());
                } else {
                    BungeeMain.Logs.info(targetname + " Was Warned for: " + reasonMessage + " by: CONSOLE");
                    if (announce) StaffChat.sendMessage("CONSOLE Warned: " + targetname + " for: " + reasonMessage);
                    else plugin.getProxy().getConsole().sendMessage(new ComponentBuilder(plugin.prefix).append("CONSOLE Warned: " + targetname + " for: " + reasonMessage).color(ChatColor.RED).create());
                }
                if (minusRep)
                    ReputationSystem.minusRep(targetname, targetuuid, repLoss);
                if (type != Punishment.Type.ALL)
                    return;
            }
            case KICK: {
                target = plugin.getProxy().getPlayer(UUIDFetcher.formatUUID(targetuuid));
                String reasonMessage;
                if (punishment.getMessage() != null) {
                    reasonMessage = punishment.getMessage();
                } else {
                    reasonMessage = reason.toString().replace("_", " ");
                }
                if (target != null && target.isConnected()) {
                    String kickMessage = BungeeMain.PunisherConfig.getString("Kick Message");
                    kickMessage = kickMessage.replace("%reason%", reasonMessage);
                    target.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', kickMessage)));
                }
                if (addHistory) {
                    incrementHistory(targetuuid, reason);
                    incrementStaffHistory(punisherUUID, reason);
                }
                if (player != null && player.isConnected()) {
                    BungeeMain.Logs.info(targetname + " Was Kicked for: " + reasonMessage + " by: " + player.getName());
                    if (announce) StaffChat.sendMessage(player.getName() + " Kicked: " + targetname + " for: " + reasonMessage);
                    else player.sendMessage(new ComponentBuilder(plugin.prefix).append("Silently Kicked: " + targetname + " for: " + reasonMessage).color(ChatColor.RED).create());
                } else {
                    BungeeMain.Logs.info(targetname + " Was Kicked for: " + reasonMessage + " by: CONSOLE");
                    if (announce) StaffChat.sendMessage("CONSOLE Kicked: " + targetname + " for: " + reasonMessage);
                    else plugin.getProxy().getConsole().sendMessage(new ComponentBuilder(plugin.prefix).append("CONSOLE Kicked: " + targetname + " for: " + reasonMessage).color(ChatColor.RED).create());
                }
                if (minusRep)
                    ReputationSystem.minusRep(targetname, targetuuid, repLoss);
                if (type != Punishment.Type.ALL)
                    return;
            }
            case MUTE: {
                target = plugin.getProxy().getPlayer(UUIDFetcher.formatUUID(targetuuid));
                String reasonMessage;
                if (punishment.getMessage() != null) {
                    reasonMessage = punishment.getMessage();
                } else {
                    reasonMessage = reason.toString().replace("_", " ");
                }
                long muteleftmillis;
                if (isMuted(targetuuid) && reason != Punishment.Reason.Manual) {
                    muteleftmillis = (getMute(targetuuid).getDuration() - System.currentTimeMillis()) + (duration - System.currentTimeMillis());
                    duration = (getMute(targetuuid).getDuration() - System.currentTimeMillis()) + duration;
                } else {
                    muteleftmillis = (duration - System.currentTimeMillis());
                }
                int daysleft = (int) (muteleftmillis / (1000 * 60 * 60 * 24));
                int hoursleft = (int) (muteleftmillis / (1000 * 60 * 60) % 24);
                int minutesleft = (int) (muteleftmillis / (1000 * 60) % 60);
                int secondsleft = (int) (muteleftmillis / 1000 % 60);
                if (secondsleft <= 0){
                    try {
                        throw new PunishmentIssueException("Seconds left cannot be less than or equal to 0 when punishing!", punishment);
                    }catch(PunishmentIssueException e){
                        ErrorHandler errorHandler = ErrorHandler.getInstance();
                        errorHandler.log(e);
                        if (player != null)
                            errorHandler.alert(e, player);
                        return;
                    }
                }
                if (target != null && target.isConnected()) {
                    if (BungeeMain.PunisherConfig.getBoolean("Mute Sound.Enabled")) {
                        try {
                            ByteArrayOutputStream outbytes = new ByteArrayOutputStream();
                            DataOutputStream out = new DataOutputStream(outbytes);
                            out.writeUTF("playsound");
                            out.writeUTF(BungeeMain.PunisherConfig.getString("Mute Sound.Sound"));
                            target.getServer().sendData("punisher:minor", outbytes.toByteArray());
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                    if (BungeeMain.PunisherConfig.getBoolean("Mute Title.Enabled")) {
                        String titleText = BungeeMain.PunisherConfig.getString("Mute Title.Title Message");
                        String subtitleText = BungeeMain.PunisherConfig.getString("Mute Title.Subtitle Message");
                        titleText = titleText.replace("%reason%", reasonMessage);
                        subtitleText = subtitleText.replace("%reason%", reasonMessage);
                        if (announce) ProxyServer.getInstance().createTitle().title(new TextComponent(ChatColor.translateAlternateColorCodes('&', titleText)))
                                .subTitle(new TextComponent(ChatColor.translateAlternateColorCodes('&', subtitleText)))
                                .fadeIn(BungeeMain.PunisherConfig.getInt("Mute Title.Fade In"))
                                .stay(BungeeMain.PunisherConfig.getInt("Mute Title.Stay")).fadeOut(BungeeMain.PunisherConfig.getInt("Mute Title.Fade Out"))
                                .send(target);
                    }
                    String muteMessage;
                    if (daysleft > 730)
                        muteMessage = BungeeMain.PunisherConfig.getString("PermMute Message").replace("%reason%", reasonMessage).replace("%days%", String.valueOf(daysleft))
                                .replace("%hours%", String.valueOf(hoursleft)).replace("%minutes%", String.valueOf(minutesleft)).replace("%seconds%", String.valueOf(secondsleft));
                    else
                        muteMessage = BungeeMain.PunisherConfig.getString("TempMute Message").replace("%reason%", reasonMessage).replace("%days%", String.valueOf(daysleft))
                                .replace("%hours%", String.valueOf(hoursleft)).replace("%minutes%", String.valueOf(minutesleft)).replace("%seconds%", String.valueOf(secondsleft));
                    if (announce) target.sendMessage(new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', muteMessage)).create());
                }
                if (addHistory) {
                    incrementHistory(targetuuid, reason);
                    incrementStaffHistory(punisherUUID, reason);
                }
                if (isMuted(targetuuid)) {
                    String sql1 = "UPDATE `mutes` SET `UUID`='" + targetuuid + "', `Name`='" + targetname + "', `Length`='" + duration +
                            "', `Reason`='" + reason.toString() + "', `Message`='" + reasonMessage +  "', `Punisher`='" + punisherUUID + "' WHERE `UUID`='" + targetuuid + "' ;";
                    PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                    stmt1.executeUpdate();
                    stmt1.close();
                } else {
                    String sql1 = "INSERT INTO `mutes` (`UUID`, `Name`, `Length`, `Reason`, `Message`, `Punisher`)" +
                            " VALUES ('"+ targetuuid + "', '" + targetname + "', '" + duration + "', '" + reason.toString() + "', '" + reasonMessage + "', '" + punisherUUID + "');";
                    PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                    stmt1.executeUpdate();
                    stmt1.close();
                }
                punishment.setDuration(duration);
                MuteCache.put(targetuuid, punishment);
                if (player != null && player.isConnected()) {
                    BungeeMain.Logs.info(targetname + " Was Muted for: " + reasonMessage + " by: " + player.getName());
                    if (announce){
                        StaffChat.sendMessage(player.getName() + " Muted: " + targetname + " for: " + reasonMessage);
                        if (daysleft < 730)
                            StaffChat.sendMessage("This Mute expires in: " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s");
                        else
                            StaffChat.sendMessage("This Mute is permanent and does not expire!");
                    }else{
                        player.sendMessage(new ComponentBuilder(plugin.prefix).append("Silently Muted: " + targetname + " for: " + reasonMessage).color(ChatColor.RED).create());
                        if (daysleft < 730)
                            player.sendMessage(new ComponentBuilder(plugin.prefix).append("This Mute expires in: " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s!").color(ChatColor.RED).create());
                        else
                            player.sendMessage(new ComponentBuilder(plugin.prefix).append("This Mute is permanent and does not expire!").color(ChatColor.RED).create());
                    }
                } else {
                    BungeeMain.Logs.info(targetname + " Was Muted for: " + reasonMessage + " by: CONSOLE");
                    if (announce){
                        StaffChat.sendMessage("CONSOLE Muted: " + targetname + " for: " + reasonMessage);
                        if (daysleft < 730)
                            StaffChat.sendMessage("This Mute expires in: " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s");
                        else
                            StaffChat.sendMessage("This Mute is permanent and does not expire!");
                    }else {
                        plugin.getProxy().getConsole().sendMessage(new ComponentBuilder(plugin.prefix).append("CONSOLE Muted: " + targetname + " for: " + reasonMessage).color(ChatColor.RED).create());
                        if (daysleft < 730)
                            plugin.getProxy().getConsole().sendMessage(new ComponentBuilder(plugin.prefix).append("This Mute expires in: " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s!").color(ChatColor.RED).create());
                        else
                            plugin.getProxy().getConsole().sendMessage(new ComponentBuilder(plugin.prefix).append("This Mute is permanent and does not expire!").color(ChatColor.RED).create());
                    }
                }
                if (minusRep)
                    ReputationSystem.minusRep(targetname, targetuuid, repLoss);
                if (type != Punishment.Type.ALL)
                    return;
            }
            case BAN: {
                target = plugin.getProxy().getPlayer(UUIDFetcher.formatUUID(targetuuid));
                String reasonMessage;
                if (punishment.getMessage() != null) {
                    reasonMessage = punishment.getMessage();
                } else {
                    reasonMessage = reason.toString().replace("_", " ");
                }
                long banleftmillis;
                if (isBanned(targetuuid) && reason != Punishment.Reason.Manual) {
                    banleftmillis = (BanCache.get(targetuuid).getDuration() - System.currentTimeMillis()) + (duration - System.currentTimeMillis());
                    duration = (BanCache.get(targetuuid).getDuration() - System.currentTimeMillis()) + duration;
                } else {
                    banleftmillis = (duration - System.currentTimeMillis());
                }
                int daysleft = (int) (banleftmillis / (1000 * 60 * 60 * 24));
                int hoursleft = (int) (banleftmillis / (1000 * 60 * 60) % 24);
                int minutesleft = (int) (banleftmillis / (1000 * 60) % 60);
                int secondsleft = (int) (banleftmillis / 1000 % 60);
                if (secondsleft <= 0){
                    try {
                        throw new PunishmentIssueException("Seconds left cannot be less than or equal to 0 when punishing!", punishment);
                    }catch(PunishmentIssueException e){
                        ErrorHandler errorHandler = ErrorHandler.getInstance();
                        errorHandler.log(e);
                        if (player != null)
                            errorHandler.alert(e, player);
                        return;
                    }
                }
                if (target != null && target.isConnected()) {
                    String banMessage;
                    if (daysleft > 730)
                        banMessage = BungeeMain.PunisherConfig.getString("PermBan Message").replace("%days%", String.valueOf(daysleft)).replace("%hours%", String.valueOf(hoursleft))
                                .replace("%minutes%", String.valueOf(minutesleft)).replace("%seconds%", String.valueOf(secondsleft));
                    else
                        banMessage = BungeeMain.PunisherConfig.getString("TempBan Message").replace("%days%", String.valueOf(daysleft)).replace("%hours%", String.valueOf(hoursleft))
                            .replace("%minutes%", String.valueOf(minutesleft)).replace("%seconds%", String.valueOf(secondsleft));
                    banMessage = banMessage.replace("%reason%", reasonMessage);
                    target.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', banMessage)));
                }
                if (addHistory) {
                    incrementHistory(targetuuid, reason);
                    incrementStaffHistory(punisherUUID, reason);
                }
                if (isBanned(targetuuid)) {
                    String sql1 = "UPDATE `bans` SET `UUID`='" + targetuuid + "', `Name`='" + targetname + "', `Length`='" + (duration + System.currentTimeMillis()) +
                            "', `Reason`='" + reason.toString() + "', `Message`='" + reasonMessage +  "', `Punisher`='" + punisherUUID + "' WHERE `UUID`='" + targetuuid + "';";
                    PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                    stmt1.executeUpdate();
                    stmt1.close();
                } else {
                    String sql1 = "INSERT INTO `bans` (`UUID`, `Name`, `Length`, `Reason`, `Message`, `Punisher`)" +
                            " VALUES ('"+ targetuuid + "', '" + targetname + "', '" + (duration + System.currentTimeMillis()) + "', '" + reason.toString() + "', '" + reasonMessage + "', '" + punisherUUID + "');";
                    PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                    stmt1.executeUpdate();
                    stmt1.close();
                }
                punishment.setDuration(duration);
                BanCache.put(targetuuid, punishment);
                if (player != null && player.isConnected()) {
                    BungeeMain.Logs.info(targetname + " Was Banned for: " + reasonMessage + " by: " + player.getName());
                    if (announce){
                        StaffChat.sendMessage(player.getName() + " Banned: " + targetname + " for: " + reasonMessage);
                        if (daysleft < 730)
                            StaffChat.sendMessage("This Ban expires in: " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s");
                        else
                            StaffChat.sendMessage("This Ban is permanent and does not expire!");
                    }else{
                        player.sendMessage(new ComponentBuilder(plugin.prefix).append("Silently Banned: " + targetname + " for: " + reasonMessage).color(ChatColor.RED).create());
                        if (daysleft < 730)
                            player.sendMessage(new ComponentBuilder(plugin.prefix).append("This Ban expires in: " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s!").color(ChatColor.RED).create());
                        else
                            player.sendMessage(new ComponentBuilder(plugin.prefix).append("This Ban is permanent and does not expire!").color(ChatColor.RED).create());
                    }
                } else {
                    BungeeMain.Logs.info(targetname + " Was Banned for: " + reasonMessage + " by: CONSOLE");
                    if (announce){
                        StaffChat.sendMessage("CONSOLE Banned: " + targetname + " for: " + reasonMessage);
                        if (daysleft < 730)
                            StaffChat.sendMessage("This Ban expires in: " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s");
                        else
                            StaffChat.sendMessage("This Ban is permanent and does not expire!");
                    }else {
                        plugin.getProxy().getConsole().sendMessage(new ComponentBuilder(plugin.prefix).append("CONSOLE Banned: " + targetname + " for: " + reasonMessage).color(ChatColor.RED).create());
                        if (daysleft < 730)
                            plugin.getProxy().getConsole().sendMessage(new ComponentBuilder(plugin.prefix).append("This Ban expires in: " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s!").color(ChatColor.RED).create());
                        else
                            plugin.getProxy().getConsole().sendMessage(new ComponentBuilder(plugin.prefix).append("This Ban is permanent and does not expire!").color(ChatColor.RED).create());
                    }
                }
            }
            if (minusRep)
                ReputationSystem.minusRep(targetname, targetuuid, repLoss);
        }
    }

    public void revoke(@NotNull Punishment punishment, @Nullable ProxiedPlayer player, @Nullable String targetname, boolean removeHistory, boolean announce) throws SQLException {
        String targetuuid = punishment.getTargetUUID();
        Punishment.Reason reason = punishment.getReason();
        if (targetname == null) {
            targetname = NameFetcher.getName(punishment.getTargetUUID());
        }

        if (isMuted(targetuuid) && (punishment.getType() == Punishment.Type.MUTE || punishment.getType() == Punishment.Type.ALL)) {
            String sql1 = "DELETE FROM `mutes` WHERE UUID='" + targetuuid + "'";
            PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
            stmt1.executeUpdate();
            stmt1.close();
            MuteCache.remove(targetuuid);
            if (player != null) {
                if (announce) StaffChat.sendMessage(player.getName() + " Unmuted: " + targetname);
                BungeeMain.Logs.info(targetname + " Was Unmuted by: " + player.getName());
            } else {
                if (announce) StaffChat.sendMessage("CONSOLE Unmuted: " + targetname);
                BungeeMain.Logs.info(targetname + " Was Unmuted by: CONSOLE");
            }
        }
        if (isBanned(targetuuid) && (punishment.getType() == Punishment.Type.BAN || punishment.getType() == Punishment.Type.ALL)) {
            String sql3 = "DELETE FROM `bans` WHERE UUID='" + targetuuid + "'";
            PreparedStatement stmt3 = plugin.connection.prepareStatement(sql3);
            stmt3.executeUpdate();
            BanCache.remove(targetuuid);
            if (player != null) {
                if (announce) StaffChat.sendMessage(player.getName() + " Unbanned: " + targetname);
                BungeeMain.Logs.info(targetname + " Was Unbanned by: " + player.getName());
            } else {
                if (announce) StaffChat.sendMessage("CONSOLE Unbanned: " + targetname);
                BungeeMain.Logs.info(targetname + " Was Unbanned by: CONSOLE");
            }
        }
        if (removeHistory) {
            String sqlhist = "SELECT * FROM `history` WHERE UUID='" + targetuuid + "'";
            PreparedStatement stmthist = plugin.connection.prepareStatement(sqlhist);
            ResultSet resultshist = stmthist.executeQuery();
            if (resultshist.next()) {
                int current;
                if (reason.toString().contains("Manual")) {
                    current = resultshist.getInt("Manual_Punishments");
                }else{
                    current = resultshist.getInt(reason.toString());
                }
                if (current != 0) {
                    current--;
                } else {
                    if (player != null) {
                        player.sendMessage(new ComponentBuilder(plugin.prefix).append(targetname + " Already has 0 punishments for that offence!").color(ChatColor.RED).create());
                        return;
                    }
                }
                String collum;
                if (reason.toString().contains("Manual")) {
                    collum = "Manual_Punishments";
                }else{
                    collum = reason.toString();
                }
                String sqlhistupdate = "UPDATE `history` SET `" + collum + "`='" + current + "' WHERE `UUID`='" + targetuuid + "' ;";
                PreparedStatement stmthistupdate = plugin.connection.prepareStatement(sqlhistupdate);
                stmthistupdate.executeUpdate();
                stmthistupdate.close();
            }
            if (player != null) {
                BungeeMain.Logs.info(player.getName() + " removed punishment: " + reason.toString().replace("_", " ") + " on player: " + targetname + " Through unpunish");
                player.sendMessage(new ComponentBuilder(plugin.prefix).append("The punishment: " + reason.toString().replace("_", " ") + " on: " + targetname + " has been removed!").color(ChatColor.RED).create());
                if (announce) StaffChat.sendMessage(player.getName() + " Unpunished: " + targetname + " for the offence: " + reason.toString().replace("_", " "));
            }
        }
    }

    public long calculateDuration(@NotNull Punishment.Reason reason, @NotNull String targetUUID) throws SQLException {
        switch (reason) {
            case Manual_Hour:
                return (long) 3.6e+6;
            case Manual_1_Day:
                return (long) 8.64e+7;
            case Manual_3_Day:
                return (long) 2.592e+8;
            case Manual_1_Week:
            case Other_Minor_Offence:
                return (long) 6.048e+8;
            case Manual_2_Week:
                return (long) 1.21e+9;
            case Manual_3_Week:
                return (long) 1.814e+9;
            case Manual_1_Month:
            case Other_Major_Offence:
                return (long) 2.628e+9;
            case Manual_Permanently:
                return (long) 3.154e+12;
            case Other_Offence:
                return (long) 1.8e+6;
        }
        int punishment;
        String sql = "SELECT * FROM `history` WHERE UUID='" + targetUUID + "'";
        PreparedStatement stmt = plugin.connection.prepareStatement(sql);
        ResultSet results = stmt.executeQuery();
        if (results.next())
            punishment = results.getInt(reason.toString());
        else punishment = 0;
        stmt.close();
        results.close();
        punishment++;
        if (punishment > 5){
            punishment = 5;
        }
        return (60000 * ((long)BungeeMain.PunishmentsConfig.getDouble(reason.toString() + "." + punishment + "length")));
    }

    public Punishment.Type calculateType(String targetUUID, Punishment.Reason reason) throws SQLException{
        if (reason.toString().contains("Manual") || reason.toString().contains("Other")){
            try {
                throw new PunishmentCalculationException("Punishment reason cannot be 'manual' or 'other' when calculating automatic punishment type!", "type");
            }catch(PunishmentCalculationException e){
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.log(e);
                errorHandler.adminChatAlert(e, ProxyServer.getInstance().getConsole());
                return null;
            }
        }
        int punishment;
        String sql = "SELECT * FROM `history` WHERE UUID='" + targetUUID + "'";
        PreparedStatement stmt = plugin.connection.prepareStatement(sql);
        ResultSet results = stmt.executeQuery();
        if (results.next())
            punishment = results.getInt(reason.toString());
        else punishment = 0;
        stmt.close();
        results.close();
        punishment++;
        if (punishment > 5){
            punishment = 5;
        }
        return Punishment.Type.valueOf(BungeeMain.PunishmentsConfig.getString(reason.toString() + "." + punishment).toUpperCase());
    }

    public double calculateRepLoss(@NotNull Punishment.Reason reason, @NotNull Punishment.Type type, @NotNull String targetUUID) throws SQLException {
        if (type == Punishment.Type.ALL) return 0;
        if (reason.toString().contains("Manual") && (type == Punishment.Type.BAN || type == Punishment.Type.MUTE))
            return BungeeMain.PunisherConfig.getDouble("ReputationScale." + type.toString() + "." + 5);
        if (reason.toString().contains("Manual") && (type == Punishment.Type.WARN || type == Punishment.Type.KICK))
            return BungeeMain.PunisherConfig.getDouble("ReputationScale." + type.toString());
        if (type == Punishment.Type.BAN || type == Punishment.Type.MUTE) {
            int offence;
            String sql = "SELECT * FROM `history` WHERE UUID='" + targetUUID + "'";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            ResultSet results = stmt.executeQuery();
            if (results.next())
                offence = results.getInt(reason.toString());
            else offence = 0;
            stmt.close();
            results.close();
            offence++;
            if (offence > 5){
                offence = 5;
            }
            return BungeeMain.PunisherConfig.getDouble("ReputationScale." + type.toString() + "." + offence);
        } else {
            return BungeeMain.PunisherConfig.getDouble("ReputationScale." + type.toString());
        }
    }

    public void createHistory(String targetUUID) throws SQLException {
        String sql = "SELECT * FROM `history` WHERE UUID='" + targetUUID + "'";
        PreparedStatement stmt = plugin.connection.prepareStatement(sql);
        ResultSet results = stmt.executeQuery();
        if (!results.next()) {
            String sql1 = "INSERT INTO `history` (UUID) VALUES ('" + targetUUID + "');";
            PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
            stmt1.executeUpdate();
            stmt1.close();
        }
        stmt.close();
        results.close();
    }

    public void createStaffHistory(String targetUUID) throws SQLException {
        String sql2 = "SELECT * FROM `staffhistory` WHERE UUID='" + targetUUID + "'";
        PreparedStatement stmt2 = plugin.connection.prepareStatement(sql2);
        ResultSet results2 = stmt2.executeQuery();
        if (!results2.next()) {
            String sql3 = "INSERT INTO `staffhistory` (UUID) VALUES ('" + targetUUID + "');";
            PreparedStatement stmt3 = plugin.connection.prepareStatement(sql3);
            stmt3.executeUpdate();
            stmt3.close();
        }
        stmt2.close();
        results2.close();
    }

    public void incrementHistory(String targetUUID, Punishment.Reason reason) throws SQLException {
        String sql = "SELECT * FROM `history` WHERE UUID='" + targetUUID + "'";
        PreparedStatement stmt = plugin.connection.prepareStatement(sql);
        ResultSet history = stmt.executeQuery();
        history.next();
        int current;
        if (!reason.toString().contains("Manual") && !reason.toString().contains("Other")) current = history.getInt(reason.toString());
        else current = history.getInt("Manual_Punishments");
        stmt.close();
        history.close();
        current++;
        if (reason.toString().contains("Manual") || reason.toString().contains("Other")) {
            String sql4 = "UPDATE `history` SET `Manual_Punishments`='" + current + "' WHERE `UUID`='" + targetUUID + "' ;";
            PreparedStatement stmt4 = plugin.connection.prepareStatement(sql4);
            stmt4.executeUpdate();
            stmt4.close();
        } else {
            String sql3 = "UPDATE `history` SET `" + reason.toString() + "`='" + current + "' WHERE `UUID`='" + targetUUID + "' ;";
            PreparedStatement stmt3 = plugin.connection.prepareStatement(sql3);
            stmt3.executeUpdate();
            stmt3.close();
        }
    }

    public void incrementStaffHistory(String punisherUUID, Punishment.Reason reason) throws SQLException {
        String sql2 = "SELECT * FROM `staffhistory` WHERE UUID='" + punisherUUID + "'";
        PreparedStatement stmt2 = plugin.connection.prepareStatement(sql2);
        ResultSet staffHistory = stmt2.executeQuery();
        staffHistory.next();
        int Punishmentno;
        if (!reason.toString().contains("Manual") && !reason.toString().contains("Other")) Punishmentno = staffHistory.getInt(reason.toString());
        else Punishmentno = staffHistory.getInt("Manual_Punishments");
        stmt2.close();
        staffHistory.close();
        Punishmentno++;
        if (reason.toString().contains("Manual") || reason.toString().contains("Other")) {
            String sql1 = "UPDATE `staffhistory` SET `Manual_Punishments`=" + Punishmentno + " WHERE UUID='" + punisherUUID + "';";
            PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
            stmt1.executeUpdate();
            stmt1.close();
        } else {
            String sql1 = "UPDATE `staffhistory` SET `" + reason.toString() + "`=" + Punishmentno + " WHERE UUID='" + punisherUUID + "';";
            PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
            stmt1.executeUpdate();
            stmt1.close();
        }
    }

    public boolean isBanned(String targetUUID) {
        return BanCache.containsKey(targetUUID);
    }

    public boolean isMuted(String targetUUID) {
        return MuteCache.containsKey(targetUUID);
    }

    public int totalBans(){
        return BanCache.keySet().toArray().length;
    }

    public int totalMutes(){
        return MuteCache.keySet().toArray().length;
    }

    public Punishment getBan(String targetUUID) {
        if (!isBanned(targetUUID)) return null;
        return BanCache.get(targetUUID);
    }

    public Punishment getMute(String targetUUID) {
        if (!isMuted(targetUUID)) return null;
        return MuteCache.get(targetUUID);
    }

    public void startCaching() {
        if (plugin == null)
            plugin = BungeeMain.getInstance();
        if (cacheTask == null) {
            cache();
            cacheTask = plugin.getProxy().getScheduler().schedule(plugin, () -> {
                if (BungeeMain.PunisherConfig.getBoolean("MySql.debugMode"))
                    plugin.getLogger().info(plugin.prefix + ChatColor.GREEN + "Caching Punishments...");
                resetCache();
            }, 10, 10, TimeUnit.SECONDS);
        }
    }

    public void resetCache() {
        BanCache.clear();
        MuteCache.clear();
        cache();
    }

    private void cache() {
        try {
            String sqlbans = "SELECT * FROM `bans`;";
            PreparedStatement stmtbans = plugin.connection.prepareStatement(sqlbans);
            ResultSet resultsbans = stmtbans.executeQuery();
            while (resultsbans.next()) {
                Punishment punishment = new Punishment(Punishment.Reason.valueOf(resultsbans.getString("Reason")), resultsbans.getString("Message"),
                        resultsbans.getLong("Length"), Punishment.Type.BAN, resultsbans.getString("UUID"), resultsbans.getString("Punisher"));
                BanCache.put(resultsbans.getString("UUID"), punishment);
            }
            resultsbans.close();
            stmtbans.close();
            String sqlmutes = "SELECT * FROM `mutes`;";
            PreparedStatement stmtmutes = plugin.connection.prepareStatement(sqlmutes);
            ResultSet resultsmutes = stmtmutes.executeQuery();
            while (resultsmutes.next()) {
                Punishment punishment = new Punishment(Punishment.Reason.valueOf(resultsmutes.getString("Reason")), resultsmutes.getString("Message"),
                        resultsmutes.getLong("Length"), Punishment.Type.MUTE, resultsmutes.getString("UUID"), resultsmutes.getString("Punisher"));
                MuteCache.put(resultsmutes.getString("UUID"), punishment);
            }
            resultsmutes.close();
            stmtmutes.close();
        } catch (SQLException sqle) {
            plugin.getLogger().severe(plugin.prefix + ChatColor.RED + "Error was encountered when updating punishment Cache!");
            BungeeMain.Logs.severe(plugin.prefix + ChatColor.RED + "Error was encountered when updating punishment Cache!");
            BungeeMain.Logs.severe(plugin.prefix + ChatColor.RED + "Error Message: " + sqle.getMessage());
            StringBuilder stackTrace = new StringBuilder();
            for (StackTraceElement stackTraceElement : sqle.getStackTrace()) {
                stackTrace.append(stackTraceElement.toString()).append("\n");
            }
            BungeeMain.Logs.severe(plugin.prefix + ChatColor.RED + "StackTrace: " + stackTrace.toString());
            sqle.printStackTrace();
        }
    }
}
