package me.fiftyfour.punisher.systems;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.chats.StaffChat;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReputationSystem {
    private static BungeeMain plugin = BungeeMain.getInstance();
    private static Long length;

    public static void minusRep(String targetname, String uuid, double amount) {
        if (!BungeeMain.RepStorage.contains(uuid)) {
            BungeeMain.RepStorage.set(uuid, 5.0 - amount);
            BungeeMain.saveRep();
        } else {
            double currentRep = BungeeMain.RepStorage.getDouble(uuid);
            if ((currentRep - amount) > -10 && (currentRep - amount) < 10) {
                BungeeMain.RepStorage.set(uuid, (currentRep - amount));
                BungeeMain.saveRep();
            } else if ((currentRep - amount) > 10) {
                BungeeMain.RepStorage.set(uuid, 10.0);
                BungeeMain.saveRep();
            } else if ((currentRep - amount) <= -10) {
                BungeeMain.RepStorage.set(uuid, (currentRep - amount));
                BungeeMain.saveRep();
                length = (long) 1000 * 60 * 60 * 24 * 7 * 4 * 12 * 54;
                String reason = "Overly Toxic (reputation dropped below -10)";
                try {
                    String sql2 = "SELECT * FROM `bans` WHERE UUID='" + uuid + "'";
                    PreparedStatement stmt2 = plugin.connection.prepareStatement(sql2);
                    ResultSet results2 = stmt2.executeQuery();
                    if (!results2.next()) {
                        String sql1 = "INSERT INTO `bans` (`UUID`, `Name`, `Length`, `Reason`, `Punisher`) VALUES ('" + uuid + "', '" + targetname + "', '" + (length + System.currentTimeMillis()) + "', '" + reason + "', 'CONSOLE');";
                        PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                        stmt1.executeUpdate();
                        stmt1.close();
                    } else {
                        String sql1 = "UPDATE `bans` SET `UUID`='" + uuid + "', `Name`='" + targetname + "', `Length`='" + (length + System.currentTimeMillis()) + "', `Reason`='" + reason + "', `Punisher`='CONSOLE' WHERE `UUID`='" + uuid + "';";
                        PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                        stmt1.executeUpdate();
                        stmt1.close();
                    }
                    stmt2.close();
                    results2.close();
                } catch (SQLException e) {
                    plugin.mysqlfail(e);
                    if (plugin.testConnectionManual()) {
                        try {
                            String sql2 = "SELECT * FROM `bans` WHERE UUID='" + uuid + "'";
                            PreparedStatement stmt2 = plugin.connection.prepareStatement(sql2);
                            ResultSet results2 = stmt2.executeQuery();
                            if (!results2.next()) {
                                String sql1 = "INSERT INTO `bans` (`UUID`, `Name`, `Length`, `Reason`, `Punisher`) VALUES ('" + uuid + "', '" + targetname + "', '" + (length + System.currentTimeMillis()) + "', '" + reason + "', 'CONSOLE');";
                                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                                stmt1.executeUpdate();
                                stmt1.close();
                            } else {
                                String sql1 = "UPDATE `bans` SET `UUID`='" + uuid + "', `Name`='" + targetname + "', `Length`='" + (length + System.currentTimeMillis()) + "', `Reason`='" + reason + "', `Punisher`='CONSOLE' WHERE `UUID`='" + uuid + "';";
                                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                                stmt1.executeUpdate();
                                stmt1.close();
                            }
                            stmt2.close();
                            results2.close();
                        } catch (SQLException sqle) {
                            plugin.mysqlfail(sqle);
                            if (!plugin.testConnectionManual()) {
                                plugin.mysqlfail(sqle);
                                return;
                            }
                            return;
                        }
                    } else
                        return;
                }
                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(targetname);
                if (target != null) {
                    String banMessage = BungeeMain.PunisherConfig.getString("PermBan Message").replace("%reason%", reason);
                    target.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', banMessage)));
                }
                StaffChat.sendMessage("CONSOLE Banned: " + targetname + " for: " + reason);
                StaffChat.sendMessage("This ban is permanent and does not expire!");
                BungeeMain.Logs.info(targetname + " Was Banned for: " + reason + " by: CONSOLE");
            }
        }

    }
    public static void addRep(String targetname, String uuid, double amount) {
        if (!BungeeMain.RepStorage.contains(uuid)){
            BungeeMain.RepStorage.set(uuid, 5.0 + amount);
            BungeeMain.saveRep();
        }else {
            double currentRep = BungeeMain.RepStorage.getDouble(uuid);
            if ((currentRep + amount) > -10 && (currentRep + amount) < 10) {
                BungeeMain.RepStorage.set(uuid, (currentRep + amount));
                BungeeMain.saveRep();
            } else if ((currentRep + amount) > 10) {
                BungeeMain.RepStorage.set(uuid, 10.0);
                BungeeMain.saveRep();
            } else if ((currentRep + amount) <= -10) {
                BungeeMain.RepStorage.set(uuid, (currentRep + amount));
                BungeeMain.saveRep();
                length = (long) 1000 * 60 * 60 * 24 * 7 * 4 * 12 * 54;
                String reason = "Overly Toxic (reputation dropped below -10)";
                try {
                    String sql2 = "SELECT * FROM `bans` WHERE UUID='" + uuid + "'";
                    PreparedStatement stmt2 = plugin.connection.prepareStatement(sql2);
                    ResultSet results2 = stmt2.executeQuery();
                    if (!results2.next()) {
                        String sql1 = "INSERT INTO `bans` (`UUID`, `Name`, `Length`, `Reason`, `Punisher`) VALUES ('" + uuid + "', '" + targetname + "', '" + (length + System.currentTimeMillis()) + "', '" + reason + "', 'CONSOLE');";
                        PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                        stmt1.executeUpdate();
                        stmt1.close();
                    } else {
                        String sql1 = "UPDATE `bans` SET `UUID`='" + uuid + "', `Name`='" + targetname + "', `Length`='" + (length + System.currentTimeMillis()) + "', `Reason`='" + reason + "', `Punisher`='CONSOLE' WHERE `UUID`='" + uuid + "';";
                        PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                        stmt1.executeUpdate();
                        stmt1.close();
                    }
                    stmt2.close();
                    results2.close();
                } catch (SQLException e) {
                    plugin.mysqlfail(e);
                    if (plugin.testConnectionManual()) {
                        try {
                            String sql2 = "SELECT * FROM `bans` WHERE UUID='" + uuid + "'";
                            PreparedStatement stmt2 = plugin.connection.prepareStatement(sql2);
                            ResultSet results2 = stmt2.executeQuery();
                            if (!results2.next()) {
                                String sql1 = "INSERT INTO `bans` (`UUID`, `Name`, `Length`, `Reason`, `Punisher`) VALUES ('" + uuid + "', '" + targetname + "', '" + (length + System.currentTimeMillis()) + "', '" + reason + "', 'CONSOLE');";
                                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                                stmt1.executeUpdate();
                                stmt1.close();
                            } else {
                                String sql1 = "UPDATE `bans` SET `UUID`='" + uuid + "', `Name`='" + targetname + "', `Length`='" + (length + System.currentTimeMillis()) + "', `Reason`='" + reason + "', `Punisher`='CONSOLE' WHERE `UUID`='" + uuid + "';";
                                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                                stmt1.executeUpdate();
                                stmt1.close();
                            }
                            stmt2.close();
                            results2.close();
                        } catch (SQLException sqle) {
                            plugin.mysqlfail(sqle);
                            if (!plugin.testConnectionManual()) {
                                plugin.mysqlfail(sqle);
                                return;
                            }
                            return;
                        }
                    } else
                        return;
                }
                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(targetname);
                if (target != null) {
                    String banMessage = BungeeMain.PunisherConfig.getString("PermBan Message").replace("%reason%", reason);
                    target.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', banMessage)));
                }
                StaffChat.sendMessage("CONSOLE Banned: " + targetname + " for: " + reason);
                StaffChat.sendMessage("This ban is permanent and does not expire!");
                BungeeMain.Logs.info(targetname + " Was Banned for: " + reason + " by: CONSOLE");
            }
        }
    }
    public static void setRep(String targetname, String uuid, double amount) {
        if (amount > 10){
            BungeeMain.RepStorage.set(uuid, 10.0);
            BungeeMain.saveRep();
        }else{
            BungeeMain.RepStorage.set(uuid, amount);
            BungeeMain.saveRep();
        }
        if (!(amount > -10)) {
            BungeeMain.RepStorage.set(uuid, amount);
            BungeeMain.saveRep();
            length = (long) 1000 * 60 * 60 * 24 * 7 * 4 * 12 * 54;
            String reason = "Overly Toxic (reputation dropped below -10)";
            try {
                String sql2 = "SELECT * FROM `bans` WHERE UUID='" + uuid + "'";
                PreparedStatement stmt2 = plugin.connection.prepareStatement(sql2);
                ResultSet results2 = stmt2.executeQuery();
                if (!results2.next()) {
                    String sql1 = "INSERT INTO `bans` (`UUID`, `Name`, `Length`, `Reason`, `Punisher`) VALUES ('" + uuid + "', '" + targetname + "', '" + (length + System.currentTimeMillis()) + "', '" + reason + "', 'CONSOLE');";
                    PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                    stmt1.executeUpdate();
                    stmt1.close();
                } else {
                    String sql1 = "UPDATE `bans` SET `UUID`='" + uuid + "', `Name`='" + targetname + "', `Length`='" + (length + System.currentTimeMillis()) + "', `Reason`='" + reason + "', `Punisher`='CONSOLE' WHERE `UUID`='" + uuid + "';";
                    PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                    stmt1.executeUpdate();
                    stmt1.close();
                }
                stmt2.close();
                results2.close();
            } catch (SQLException e) {
                plugin.mysqlfail(e);
                if (plugin.testConnectionManual()) {
                    try {
                        String sql2 = "SELECT * FROM `bans` WHERE UUID='" + uuid + "'";
                        PreparedStatement stmt2 = plugin.connection.prepareStatement(sql2);
                        ResultSet results2 = stmt2.executeQuery();
                        if (!results2.next()) {
                            String sql1 = "INSERT INTO `bans` (`UUID`, `Name`, `Length`, `Reason`, `Punisher`) VALUES ('" + uuid + "', '" + targetname + "', '" + (length + System.currentTimeMillis()) + "', '" + reason + "', 'CONSOLE');";
                            PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                            stmt1.executeUpdate();
                            stmt1.close();
                        } else {
                            String sql1 = "UPDATE `bans` SET `UUID`='" + uuid + "', `Name`='" + targetname + "', `Length`='" + (length + System.currentTimeMillis()) + "', `Reason`='" + reason + "', `Punisher`='CONSOLE' WHERE `UUID`='" + uuid + "';";
                            PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                            stmt1.executeUpdate();
                            stmt1.close();
                        }
                        stmt2.close();
                        results2.close();
                    } catch (SQLException sqle) {
                        plugin.mysqlfail(sqle);
                        if (!plugin.testConnectionManual()) {
                            plugin.mysqlfail(sqle);
                            return;
                        }
                        return;
                    }
                } else
                    return;
            }
            ProxiedPlayer target = ProxyServer.getInstance().getPlayer(targetname);
            if (target != null) {
                String banMessage = BungeeMain.PunisherConfig.getString("PermBan Message").replace("%reason%", reason);
                target.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', banMessage)));
            }
            StaffChat.sendMessage("CONSOLE Banned: " + targetname + " for: " + reason);
            StaffChat.sendMessage("This ban is permanent and does not expire!");
            BungeeMain.Logs.info(targetname + " Was Banned for: " + reason + " by: CONSOLE");
        }
    }
    public static String getRep(String uuid) {
        if (BungeeMain.RepStorage.contains(uuid))
            return String.valueOf(BungeeMain.RepStorage.getDouble(uuid));
        else return null;
    }
}