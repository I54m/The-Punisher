package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.fetchers.NameFetcher;
import me.fiftyfour.punisher.fetchers.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HistoryCommand extends Command {
    private BungeeMain plugin = BungeeMain.getInstance();
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;

    public HistoryCommand() {
        super("history", "punisher.history", "hist");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            if (strings.length == 0) {
                player.sendMessage(new ComponentBuilder(prefix).append("View a player's punishment history").color(ChatColor.RED).append("\nUsage: /history <player name>").color(ChatColor.WHITE).create());
                return;
            }
            String targetuuid = UUIDFetcher.getUUID(strings[0]);
            if (!targetuuid.equals("null")) {
                String targetname = NameFetcher.getName(targetuuid);
                if (targetname.equalsIgnoreCase("null")) {
                    targetname = strings[0];
                }
                try {
                    Long mutetime;
                    Long bantime;
                    String sql = "SELECT * FROM `history` WHERE UUID='" + targetuuid + "'";
                    PreparedStatement stmt = plugin.connection.prepareStatement(sql);
                    ResultSet results = stmt.executeQuery();
                    if (results.next()) {
                        String sql1 = "SELECT * FROM `mutes` WHERE UUID='" + targetuuid + "'";
                        PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                        ResultSet results1 = stmt1.executeQuery();
                        String sql2 = "SELECT * FROM `bans` WHERE UUID='" + targetuuid + "'";
                        PreparedStatement stmt2 = plugin.connection.prepareStatement(sql2);
                        ResultSet results2 = stmt2.executeQuery();
                        TextComponent status;
                        if (results2.next()) {
                            bantime = results2.getLong("Length");
                            Long banleftmillis = bantime - System.currentTimeMillis();
                            long daysleft = banleftmillis / (1000 * 60 * 60 * 24);
                            long hoursleft = (long) Math.floor(banleftmillis / (1000 * 60 * 60) % 24);
                            long minutesleft = (long) Math.floor(banleftmillis / (1000 * 60) % 60);
                            long secondsleft = (long) Math.floor(banleftmillis / 1000 % 60);
                            String reason = results2.getString("Reason");
                            String punisher = results2.getString("Punisher");
                            status = new TextComponent("banned for " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s. Reason: " + reason + " by: " + punisher);
                            status.setColor(ChatColor.RED);
                        } else if (results1.next()) {
                            mutetime = results1.getLong("Length");
                            Long muteleftmillis = mutetime - System.currentTimeMillis();
                            long daysleft = muteleftmillis / (1000 * 60 * 60 * 24);
                            long hoursleft = (long) Math.floor(muteleftmillis / (1000 * 60 * 60) % 24);
                            long minutesleft = (long) Math.floor(muteleftmillis / (1000 * 60) % 60);
                            long secondsleft = (long) Math.floor(muteleftmillis / 1000 % 60);
                            String reason = results1.getString("Reason");
                            String punisher = results1.getString("Punisher");
                            status = new TextComponent("Muted for " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s. Reason: " + reason + " by: " + punisher);
                            status.setColor(ChatColor.RED);
                        } else {
                            status = new TextComponent("No currently active punishments!");
                            status.setColor(ChatColor.GREEN);
                        }
                        player.sendMessage(new ComponentBuilder("|-------------").color(ChatColor.GREEN).strikethrough(true).append("History for: " + targetname).color(ChatColor.RED).strikethrough(false)
                                .append("-------------|").color(ChatColor.GREEN).strikethrough(true).create());
                        if (results.getInt("Minor Chat Offence") != 0) player.sendMessage(new ComponentBuilder("Been Punished for Minor Chat Offence: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Minor Chat Offence"))).color(ChatColor.RED).create());
                        if (results.getInt("Major Chat Offence") != 0) player.sendMessage(new ComponentBuilder("Been Punished for Major Chat Offence: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Major Chat Offence"))).color(ChatColor.RED).create());
                        if (results.getInt("DDoS/DoX Threats") != 0) player.sendMessage(new ComponentBuilder("Been Punished for DDoS/DoX Threats: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("DDoS/DoX Threats"))).color(ChatColor.RED).create());
                        if (results.getInt("Inappropriate Link") != 0) player.sendMessage(new ComponentBuilder("Been Punished for Inappropriate Link: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Inappropriate Link"))).color(ChatColor.RED).create());
                        if (results.getInt("Scamming") != 0) player.sendMessage(new ComponentBuilder("Been Punished for Scamming: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Scamming"))).color(ChatColor.RED).create());
                        if (results.getInt("X-Raying") != 0) player.sendMessage(new ComponentBuilder("Been Punished for X-Raying: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("X-Raying"))).color(ChatColor.RED).create());
                        if (results.getInt("AutoClicker(non PvP)") != 0) player.sendMessage(new ComponentBuilder("Been Punished for AutoClicker(Non PvP): ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("AutoClicker(non PvP)"))).color(ChatColor.RED).create());
                        if (results.getInt("Fly/Speed Hacking") != 0) player.sendMessage(new ComponentBuilder("Been Punished for Fly/Speed Hacking: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Fly/Speed Hacking"))).color(ChatColor.RED).create());
                        if (results.getInt("Malicious PvP Hacks") != 0) player.sendMessage(new ComponentBuilder("Been Punished for Malicious PvP Hacks: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Malicious PvP Hacks"))).color(ChatColor.RED).create());
                        if (results.getInt("Disallowed Mods") != 0) player.sendMessage(new ComponentBuilder("Been Punished for Disallowed Mods: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Disallowed Mods"))).color(ChatColor.RED).create());
                        if (results.getInt("Server Advertisment") != 0) player.sendMessage(new ComponentBuilder("Been Punished for Server Advertisment: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Server Advertisment"))).color(ChatColor.RED).create());
                        if (results.getInt("Greifing") != 0) player.sendMessage(new ComponentBuilder("Been Punished for Greifing: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Greifing"))).color(ChatColor.RED).create());
                        if (results.getInt("Exploiting") != 0) player.sendMessage(new ComponentBuilder("Been Punished for Exploiting: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Exploiting"))).color(ChatColor.RED).create());
                        if (results.getInt("Tpa-Trapping") != 0) player.sendMessage(new ComponentBuilder("Been Punished for TPA-Trapping: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Tpa-Trapping"))).color(ChatColor.RED).create());
                        if (results.getInt("Impersonation") != 0) player.sendMessage(new ComponentBuilder("Been Punished for Player_Impersonation: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Impersonation"))).color(ChatColor.RED).create());
                        if (results.getInt("Manual Punishments") != 0) player.sendMessage(new ComponentBuilder("Been Punished Manually: ").color(ChatColor.GREEN).append(String.valueOf(results.getInt("Manual Punishments"))).color(ChatColor.RED).create());
                        player.sendMessage(new ComponentBuilder("Current Status: ").color(ChatColor.GREEN).append(status).create());
                    } else {
                        player.sendMessage(new ComponentBuilder(prefix).append(targetname + " has not been punished yet!").color(ChatColor.RED).create());
                    }
                } catch (SQLException e) {
                    plugin.mysqlfail(e);
                    if (plugin.testConnectionManual())
                        this.execute(commandSender, strings);
                }
            } else {
                player.sendMessage(new ComponentBuilder(prefix).append("That is not a player's name").color(ChatColor.RED).create());
            }
        } else {
            commandSender.sendMessage(new TextComponent("You must be a player to use this command!"));
        }
    }
}
