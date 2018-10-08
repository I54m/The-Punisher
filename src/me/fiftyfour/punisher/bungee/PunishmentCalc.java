package me.fiftyfour.punisher.bungee;

import me.fiftyfour.punisher.bungee.chats.StaffChat;
import me.fiftyfour.punisher.fetchers.UUIDFetcher;
import me.fiftyfour.punisher.systems.ReputationSystem;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;


public class PunishmentCalc implements Listener {

    private BungeeMain plugin = BungeeMain.getInstance();
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;
    private Long currentlength;
    private HashMap<String, Long> cooldowns = new HashMap<>();
    private String name, uuid, reason;
    private ProxiedPlayer punisher;
    private int offence;
    private int sqlfails = 0;

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        String action;
        Connection sender = e.getReceiver();
        if (e.getTag().equalsIgnoreCase("BungeeCord")) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
            try {
                action = in.readUTF();
                if (action.equals("punish")) {
                    name = in.readUTF();
                    uuid = in.readUTF().replace("-", "");
                    reason = in.readUTF();
                }else return;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            try {
                punisher = (ProxiedPlayer) sender;
                String sql = "SELECT * FROM `history` WHERE UUID='" + uuid + "'";
                PreparedStatement stmt = plugin.connection.prepareStatement(sql);
                ResultSet results = stmt.executeQuery();
                if (!results.next()) {
                    String sql1 = "INSERT INTO `history` (UUID) VALUES ('"+ uuid + "');";
                    PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                    stmt1.executeUpdate();
                    stmt1.close();
                }
                stmt.close();
                results.close();
                String sql2 = "SELECT * FROM `staffhistory` WHERE UUID='" + punisher.getUniqueId().toString().replace("-", "") + "'";
                PreparedStatement stmt2 = plugin.connection.prepareStatement(sql2);
                ResultSet results2 = stmt2.executeQuery();
                if (!results2.next()) {
                    String sql3 = "INSERT INTO `staffhistory` (UUID) VALUES ('"+ punisher.getUniqueId().toString().replace("-", "") + "');";
                    PreparedStatement stmt3 = plugin.connection.prepareStatement(sql3);
                    stmt3.executeUpdate();
                    stmt3.close();
                }
                stmt2.close();
                results2.close();
                if (sqlfails < 5)
                    punish(uuid, reason);
                else throw new SQLException("sql has failed 5 times or more!");
            }catch (SQLException sqle){
                plugin.getLogger().severe(prefix + e);
                sqlfails++;
                if(sqlfails > 5){
                    plugin.getProxy().getPluginManager().unregisterListener(this);
                    plugin.getLogger().severe(prefix + "Event: OnPluginMessageReceived has thrown an exception more than 5 times!");
                    plugin.getLogger().severe(prefix + "Disabling event to prevent further damage to database!");
                    BungeeMain.Logs.severe("Event: OnPluginMessageReceived has thrown an exception more than 5 times!");
                    BungeeMain.Logs.severe("Disabling command to prevent further damage to database!");
                    return;
                }
                if (plugin.testConnectionManual())
                    plugin.getProxy().getPluginManager().callEvent(new PluginMessageEvent(e.getSender(), e.getReceiver(), e.getTag(), e.getData()));
            }
        }
    }

    private void punish(String uuid, String reason) throws SQLException{
            int cooldownTime = 20;
            if (cooldowns.containsKey(name) && !punisher.hasPermission("punisher.cooldowns.override")) {
                long secondsLeft = ((cooldowns.get(name) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
                if (secondsLeft > 0) {
                    punisher.sendMessage(new ComponentBuilder(prefix).append(name + " Has recently been punished please wait " + secondsLeft + "s Before punishing them again!").color(ChatColor.RED).create());
                    return;
                }
            }
            String sql = "SELECT * FROM `history` WHERE UUID='" + uuid + "'";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            ResultSet history = stmt.executeQuery();
            history.next();
            String sql2 = "SELECT * FROM `staffhistory` WHERE UUID='" + punisher.getUniqueId().toString().replace("-", "") + "'";
            PreparedStatement stmt2 = plugin.connection.prepareStatement(sql2);
            ResultSet staffHistory = stmt2.executeQuery();
            staffHistory.next();
            cooldowns.put(name, System.currentTimeMillis());
            int Punishmentno;
            if (!reason.contains("Manually "))Punishmentno = staffHistory.getInt(reason);
            else Punishmentno = staffHistory.getInt("Manual Punishments");
            stmt2.close();
            staffHistory.close();
            int current;
            if (!reason.contains("Manually "))current = history.getInt(reason);
            else current = history.getInt("Manual Punishments");
            stmt.close();
            history.close();
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(UUIDFetcher.formatUUID(uuid));
            if (reason.contains("Manually ")) {
                if (reason.contains("Warned")) {
                    if (player != null) {
                        try {
                            ByteArrayOutputStream outbytes = new ByteArrayOutputStream();
                            DataOutputStream out = new DataOutputStream(outbytes);
                            out.writeUTF("Punisher");
                            out.writeUTF("PlayPunishSound");
                            player.getServer().sendData("BungeeCord", outbytes.toByteArray());
                        }catch (IOException ioe){
                            ioe.printStackTrace();
                        }
                        ProxyServer.getInstance().createTitle().title(new TextComponent(ChatColor.DARK_RED + "You have been Warned!!")).subTitle(new TextComponent(ChatColor.RED + "Reason: " + reason)).fadeIn(5).stay(100).fadeOut(5).send(player);
                        player.sendMessage(new TextComponent("\n"));
                        player.sendMessage(new ComponentBuilder(prefix).append("You have been Warned, Reason: " + reason).color(ChatColor.RED).create());
                        player.sendMessage(new ComponentBuilder(prefix).append("Something you did was against our server rules!").color(ChatColor.RED).create());
                        player.sendMessage(new ComponentBuilder(prefix).append("Next time there may be harsher punishments!!").color(ChatColor.RED).create());
                        player.sendMessage(new ComponentBuilder(prefix).append("Do /rules for the server rules to get more info!!").color(ChatColor.RED).create());
                        player.sendMessage(new TextComponent("\n"));
                    }
                    StaffChat.sendMessage(punisher.getName() + " Warned: " + name + " for: " + reason);
                    ReputationSystem.minusRep(name, uuid, 0.2);
                } else if (reason.contains("Muted ")) {
                    if (reason.contains("for 1 Hour")) {
                        mute(player, (long) 1000 * 60 * 60);
                        ReputationSystem.minusRep(name, uuid, 1.0);
                    } else if (reason.contains("Day")) {
                        if (reason.contains("for 1")) {
                            mute(player, (long) 1000 * 60 * 60 * 24);
                            ReputationSystem.minusRep(name, uuid, 1.0);
                        } else if (reason.contains("for 3")) {
                            mute(player, (long) 1000 * 60 * 60 * 24 * 3);
                            ReputationSystem.minusRep(name, uuid, 1.0);
                        }
                    } else if (reason.contains("Week")) {
                        if (reason.contains("for 1")) {
                            mute(player, (long) 1000 * 60 * 60 * 24 * 7);
                            ReputationSystem.minusRep(name, uuid, 2.0);
                        } else if (reason.contains("for 2")) {
                            mute(player, (long) 1000 * 60 * 60 * 24 * 7 * 2);
                            ReputationSystem.minusRep(name, uuid, 2.0);
                        } else if (reason.contains("for 3")) {
                            mute(player, (long) 1000 * 60 * 60 * 24 * 7 * 3);
                            ReputationSystem.minusRep(name, uuid, 2.0);
                        }
                    } else if (reason.contains("Month")) {
                        if (reason.contains("for 1")) {
                            mute(player, (long) 1000 * 60 * 60 * 24 * 7 * 4);
                            ReputationSystem.minusRep(name, uuid, 2.5);
                        }
                    } else if (reason.contains("Permanently")) {
                        mute(player, (long) 1000 * 60 * 60 * 24 * 7 * 4 * 12 * 54);
                        ReputationSystem.minusRep(name, uuid, 2.5);
                    }
                } else if (reason.contains("Kicked")) {
                    if (player != null)
                    player.disconnect(new TextComponent(ChatColor.RED + "You have been Kicked from the server!" + "\n" + "You were kicked for the reason: " + reason + "\n" + "You may reconnect at anytime, but make sure to read the /rules!"));
                    StaffChat.sendMessage(punisher.getName() + " Kicked: " + name + " for: ");
                } else if (reason.contains("Banned ")) {
                    if (reason.contains("for 1 Hour")) {
                        ban(player, 1000 * 60 * 60);
                        ReputationSystem.minusRep(name, uuid, 2.0);
                    } else if (reason.contains("Day")) {
                        if (reason.contains("for 1")) {
                            ban(player, 1000 * 60 * 60 * 24);
                            ReputationSystem.minusRep(name, uuid, 2.0);
                        } else if (reason.contains("for 3")) {
                            ban(player, 1000 * 60 * 60 * 24 * 3);
                            ReputationSystem.minusRep(name, uuid, 2.0);
                        }
                    } else if (reason.contains("Week")) {
                        if (reason.contains("for 1")) {
                            ban(player, 1000 * 60 * 60 * 24 * 7);
                            ReputationSystem.minusRep(name, uuid, 4.0);
                        } else if (reason.contains("for 2")) {
                            ban(player, 1000 * 60 * 60 * 24 * 7 * 2);
                            ReputationSystem.minusRep(name, uuid, 4.0);
                        } else if (reason.contains("for 3")) {
                            ban(player, 1000 * 60 * 60 * 24 * 7 * 3);
                            ReputationSystem.minusRep(name, uuid, 4.0);
                        }
                    } else if (reason.contains("Month")) {
                        if (reason.contains("for 1")) {
                            ban(player, (long) 1000 * 60 * 60 * 24 * 7 * 4);
                            ReputationSystem.minusRep(name, uuid, 4.5);
                        }
                    } else if (reason.contains("Permanently")) {
                        ban(player, (long) 1000 * 60 * 60 * 24 * 7 * 4 * 12 * 54);
                        ReputationSystem.minusRep(name, uuid, 4.5);
                    }
                }
            }else if (reason.equals("Other Minor Offence")){
                ban(player, 1000 * 60 * 60 * 24 * 7);
                ReputationSystem.minusRep(name, uuid, 2.0);
            }else if (reason.equals("Other Major Offence")){
                ban(player, (long) 1000 * 60 * 60 * 24 * 30);
                ReputationSystem.minusRep(name, uuid, 3.0);
            }else if (reason.equals("Other Offence")){
                ban(player, 1000 * 60 * 30);
                ReputationSystem.minusRep(name, uuid, 1.5);
            }
            Punishmentno++;
            current++;
            if (!reason.contains("Manually ")) {
                String sql1 = "UPDATE `staffhistory` SET `" + reason + "`=" + Punishmentno + " WHERE UUID='" + punisher.getUniqueId().toString().replace("-", "") + "';";
                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                stmt1.executeUpdate();
                stmt1.close();
                String sql3 = "UPDATE `history` SET `" + reason + "`='" + current + "' WHERE `UUID`='" + uuid + "' ;";
                PreparedStatement stmt3 = plugin.connection.prepareStatement(sql3);
                stmt3.executeUpdate();
                stmt3.close();
            }else{
                String sql1 = "UPDATE `staffhistory` SET `Manual Punishments`=" + Punishmentno + " WHERE UUID='" + punisher.getUniqueId().toString().replace("-", "") + "';";
                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                stmt1.executeUpdate();
                stmt1.close();
                String sql4 = "UPDATE `history` SET `Manual Punishments`='" + current + "' WHERE `UUID`='" + uuid + "' ;";
                PreparedStatement stmt4 = plugin.connection.prepareStatement(sql4);
                stmt4.executeUpdate();
                stmt4.close();
            }
            if (reason.equals("Other Minor Offence") || reason.equals("Other Major Offence") || reason.equals("Other Offence") || reason.contains("Manually ")){
                BungeeMain.Logs.info(name + " Was punished for: " + reason + " by: " + punisher.getName());
                return;
            }
            offence = current;
            if (offence > 5) {
                offence = 5;
            }
            String punishment = BungeeMain.PunishmentsConfig.getString(reason + "." + offence);
            if (punishment.equalsIgnoreCase("warn")) {
                if (player != null) {
                    try {
                        ByteArrayOutputStream outbytes = new ByteArrayOutputStream();
                        DataOutputStream out = new DataOutputStream(outbytes);
                        out.writeUTF("Punisher");
                        out.writeUTF("PlayPunishSound");
                        player.getServer().sendData("BungeeCord", outbytes.toByteArray());
                    }catch (IOException ioe){
                        ioe.printStackTrace();
                    }
                    ProxyServer.getInstance().createTitle().title(new TextComponent(ChatColor.DARK_RED + "You have been Warned!!")).subTitle(new TextComponent(ChatColor.RED + "Reason: " + reason)).fadeIn(5).stay(100).fadeOut(5).send(player);
                    player.sendMessage(new TextComponent("\n"));
                    player.sendMessage(new ComponentBuilder(prefix).append("You have been Warned, Reason: " + reason).color(ChatColor.RED).create());
                    player.sendMessage(new ComponentBuilder(prefix).append("Something you did was against our server rules!").color(ChatColor.RED).create());
                    player.sendMessage(new ComponentBuilder(prefix).append("Next time there may be harsher punishments!!").color(ChatColor.RED).create());
                    player.sendMessage(new ComponentBuilder(prefix).append("Do /rules for the server rules to get more info!!").color(ChatColor.RED).create());
                    player.sendMessage(new TextComponent("\n"));
                }
                BungeeMain.Logs.info(name + " Was punished for: " + reason + " by: " + punisher.getName());
                StaffChat.sendMessage(punisher.getName() + " Warned: " + name + " for: " + reason);
                ReputationSystem.minusRep(name, uuid, 0.2);
            } else if (punishment.equalsIgnoreCase("mute")) {
                int mutelength;
                mutelength = BungeeMain.PunishmentsConfig.getInt(reason + "." + offence + "mutelength");
                mute(player, (long) 1000 * 60 * mutelength);
            } else if (punishment.equalsIgnoreCase("kick")) {
                String kickMessage = BungeeMain.PunisherConfig.getString("Kick Message");
                if (player != null)
                player.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', kickMessage).replace("%reason%", reason)));
                StaffChat.sendMessage(punisher.getName() + " Kicked: " + name + " for: " + reason);
                BungeeMain.Logs.info(name + " Was punished for: " + reason + " by: " + punisher.getName());
            } else if (punishment.equalsIgnoreCase("ban")) {
                int banlength;
                banlength = BungeeMain.PunishmentsConfig.getInt(reason + "." + offence + "banlength");
                ban(player, (long) 1000 * 60 * banlength);
            }else {
                punisher.sendMessage(new ComponentBuilder(prefix).append("FATAL ERROR: ").color(ChatColor.DARK_RED)
                        .append("Error in configuration: punishments.yml, Could not find punishment to fit reason: " + reason + " with offence number: " + offence + ", This error has been logged!").color(ChatColor.RED).create());
                plugin.getLogger().severe("Error in configuration: punishments.yml, Could not find punishment to fit reason: " + reason + " with offence number: " + offence + " More information in log file corresponding to this date");
                BungeeMain.Logs.severe("CONFIGURATION ERROR, Error in configuration file: punishments.yml - Could not find punishment for player: " + name + " to fit the reason: " + reason + " with offence number: " + offence
                + " \nThis may be due to a misconfiguration in the punishments.yml file, if you can find the misconfiguration do /punisher punishments reset to reset them to their default values!");
            }
    }

    private void mute(ProxiedPlayer player, long length) throws SQLException {
        if (offence ==  1){
            ReputationSystem.minusRep(name, uuid, 0.5);
        }else if (offence == 2){
            ReputationSystem.minusRep(name, uuid, 1);
        }else if (offence == 3){
            ReputationSystem.minusRep(name, uuid, 1.5);
        }else if (offence == 4 || offence == 5){
            ReputationSystem.minusRep(name, uuid, 2);
        }
            String sql = "SELECT * FROM `mutes` WHERE UUID='" + uuid + "'";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            ResultSet results = stmt.executeQuery();
            if (!results.next()) {
                currentlength = (long) 0;
                String sql1 = "INSERT INTO `mutes` (`UUID`, `Name`, `Length`, `Reason`, `Punisher`) VALUES ('"+ uuid + "', '" + name + "', '" + (length + System.currentTimeMillis()) + "', '" + reason + "', '" + punisher.getName() + "');";
                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                stmt1.executeUpdate();
                stmt1.close();
            }else{
                if (reason.contains("Manually "))currentlength = results.getLong("Length") - System.currentTimeMillis();
                else currentlength = (long)0;
                String sql1 = "UPDATE `mutes` SET `UUID`='" + uuid + "', `Name`='" + name + "', `Length`='" + ((currentlength + length)+ System.currentTimeMillis()) + "', `Reason`='" + reason + "', `Punisher`='" + punisher.getName() + "' WHERE `UUID`='" + uuid + "' ;";
                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                stmt1.executeUpdate();
                stmt1.close();
            }
            stmt.close();
            results.close();
            Long muteleftmillis = currentlength + length;
            int daysleft = (int) (muteleftmillis / (1000 * 60 * 60 * 24));
            int hoursleft = (int) (muteleftmillis / (1000 * 60 * 60) % 24);
            int minutesleft = (int) (muteleftmillis / (1000 * 60) % 60);
            int secondsleft = (int) (muteleftmillis / 1000 % 60);
            if (player != null) {
                try {
                    ByteArrayOutputStream outbytes = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(outbytes);
                    out.writeUTF("Punisher");
                    out.writeUTF("PlayPunishSound");
                    player.getServer().sendData("BungeeCord", outbytes.toByteArray());
                }catch (IOException ioe){
                    ioe.printStackTrace();
                }
                ProxyServer.getInstance().createTitle().title(new TextComponent(ChatColor.DARK_RED + "You have been Muted!!")).subTitle(new TextComponent(ChatColor.RED + "Reason: " + reason)).fadeIn(5).stay(100).fadeOut(5).send(player);
                player.sendMessage(new TextComponent("\n"));
                player.sendMessage(new ComponentBuilder(prefix).append("You have been Muted! Reason: " + reason).color(ChatColor.RED).create());
                player.sendMessage(new ComponentBuilder(prefix).append("Something you did was against our server rules!").color(ChatColor.RED).create());
                player.sendMessage(new ComponentBuilder(prefix).append("Do /rules for more info!").color(ChatColor.RED).create());
                if (daysleft > 500) {
                    player.sendMessage(new ComponentBuilder(prefix).append("This mute is permanent and does not expire").color(ChatColor.RED).create());
                    player.sendMessage(new TextComponent("\n"));
                } else {
                    player.sendMessage(new ComponentBuilder(prefix).append("This mute expires in: " + daysleft + "d " + hoursleft + "hr " + minutesleft + "m " + secondsleft + "s").color(ChatColor.RED).create());
                    player.sendMessage(new TextComponent("\n"));
                }
            }
            StaffChat.sendMessage(punisher.getName() + " Muted: " + name + " for: " + reason);
            if (daysleft < 500)
                StaffChat.sendMessage("This mute expires in: " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s");
            else
                StaffChat.sendMessage("This mute is permanent and does not expire!");
            BungeeMain.Logs.info(name + " Was Muted for: " + reason + " by: " + punisher.getName());
    }

    private void ban(ProxiedPlayer player, long length) throws SQLException{
        if (offence ==  1){
            ReputationSystem.minusRep(name, uuid, 1);
        }else if (offence == 2){
            ReputationSystem.minusRep(name, uuid, 2);
        }else if (offence == 3){
            ReputationSystem.minusRep(name, uuid, 3);
        }else if (offence == 4 || offence == 5){
            ReputationSystem.minusRep(name, uuid, 4);
        }

            String sql = "SELECT * FROM `bans` WHERE UUID='" + uuid + "'";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            ResultSet results = stmt.executeQuery();
            if (!results.next()) {
                currentlength = (long) 0;
                String sql1 = "INSERT INTO `bans` (`UUID`, `Name`, `Length`, `Reason`, `Punisher`) VALUES ('"+ uuid + "', '" + name + "', '" + (length + System.currentTimeMillis()) + "', '" + reason + "', '" + punisher.getName() + "');";
                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                stmt1.executeUpdate();
                stmt1.close();
            }else {
                if (reason.contains("Manually "))
                    currentlength = results.getLong("Length") - System.currentTimeMillis();
                else currentlength = (long) 0;
                String sql1 = "UPDATE `bans` SET `UUID`='" + uuid + "', `Name`='" + name + "', `Length`='" + ((currentlength + length) + System.currentTimeMillis()) + "', `Reason`='" + reason + "', `Punisher`='" + punisher.getName() + "' WHERE `UUID`='" + uuid + "';";
                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                stmt1.executeUpdate();
                stmt1.close();
            }
            stmt.close();
            results.close();
            Long banleftmillis = currentlength + length;
            int daysleft = (int) (banleftmillis / (1000 * 60 * 60 * 24));
            int hoursleft = (int) (banleftmillis / (1000 * 60 * 60) % 24);
            int minutesleft = (int) (banleftmillis / (1000 * 60) % 60);
            int secondsleft = (int) (banleftmillis / 1000 % 60);
            if (daysleft > 500) {
                String banMessage = BungeeMain.PunisherConfig.getString("PermBan Message").replace("%days%", String.valueOf(daysleft))
                        .replace("%hours%", String.valueOf(hoursleft)).replace("%minutes%", String.valueOf(minutesleft))
                        .replace("%seconds%", String.valueOf(secondsleft)).replace("%reason%", reason);
                if (player !=null)
                player.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', banMessage)));
            } else {
                String banMessage = BungeeMain.PunisherConfig.getString("TempBan Message").replace("%days%", String.valueOf(daysleft))
                        .replace("%hours%", String.valueOf(hoursleft)).replace("%minutes%", String.valueOf(minutesleft))
                        .replace("%seconds%", String.valueOf(secondsleft)).replace("%reason%", reason);
                if (player !=null)
                player.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', banMessage)));
            }
            StaffChat.sendMessage(punisher.getName() + " Banned: " + name + " for: " + reason);
            if (daysleft < 500)
            StaffChat.sendMessage("This ban expires in: " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s");
            else
                StaffChat.sendMessage("This ban is permanent and does not expire!");
        BungeeMain.Logs.info(name + " Was banned for: " + reason + " by: " + punisher.getName());
    }
}