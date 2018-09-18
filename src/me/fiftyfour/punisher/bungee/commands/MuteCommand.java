package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.chats.StaffChat;
import me.fiftyfour.punisher.fetchers.NameFetcher;
import me.fiftyfour.punisher.fetchers.UUIDFetcher;
import me.fiftyfour.punisher.systems.Permissions;
import me.fiftyfour.punisher.systems.ReputationSystem;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MuteCommand extends Command {

    private BungeeMain plugin = BungeeMain.getInstance();
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;
    private long length;

    public MuteCommand() {
        super("mute", "punisher.mute", "tempmute");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage(new TextComponent("You must be a player to use this command!"));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) commandSender;
        if (strings.length == 0) {
            player.sendMessage(new ComponentBuilder(prefix).append("Mute a player from speaking").color(ChatColor.RED).append("\nUsage: /mute <player> [length<s|m|h|d|w|M|perm>] [reason]").color(ChatColor.WHITE).create());
            return;
        }
        String targetuuid = UUIDFetcher.getUUID(strings[0]);
        if (targetuuid.equalsIgnoreCase("null")) {
            player.sendMessage(new ComponentBuilder("That is not a player's name!").color(ChatColor.RED).create());
            return;
        }
        String targetname = NameFetcher.getName(targetuuid);
        if (targetname.equalsIgnoreCase("null")) {
            targetname = strings[0];
        }
        if (!Permissions.higher(player, targetname)){
            player.sendMessage(new ComponentBuilder(prefix).append("You cannot punish that player!").color(ChatColor.RED).create());
            return;
        }
        try {
            String sql = "SELECT * FROM `history` WHERE UUID='" + targetuuid + "'";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            ResultSet results = stmt.executeQuery();
            if (!results.next()) {
                String sql1 = "INSERT INTO `history` (UUID) VALUES ('"+ targetuuid + "');";
                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                stmt1.executeUpdate();
            }
            String sql2 = "SELECT * FROM `staffhistory` WHERE UUID='" + player.getUniqueId().toString().replace("-", "") + "'";
            PreparedStatement stmt2 = plugin.connection.prepareStatement(sql2);
            ResultSet results2 = stmt2.executeQuery();
            if (!results2.next()) {
                String sql3 = "INSERT INTO `staffhistory` (UUID) VALUES ('"+ player.getUniqueId().toString().replace("-", "") + "');";
                PreparedStatement stmt3 = plugin.connection.prepareStatement(sql3);
                stmt3.executeUpdate();
            }
        }catch (SQLException e){
            plugin.mysqlfail(e);
            if (plugin.testConnectionManual())
                this.execute(commandSender, strings);
            return;
        }
        boolean duration;
        try {
            if (strings.length == 1 || strings[1].toLowerCase().endsWith("perm")) {
                length = (long) 1000 * 60 * 60 * 24 * 7 * 4 * 12 * 54;
                duration = true;
            } else if (strings[1].endsWith("M")) {
                length = (long) 1000 * 60 * 60 * 24 * 7 * 4 * (long) Integer.parseInt(strings[1].replace("M", ""));
                duration = true;
            } else if (strings[1].toLowerCase().endsWith("w")) {
                length = 1000 * 60 * 60 * 24 * 7 * (long) Integer.parseInt(strings[1].replace("w", ""));
                duration = true;
            } else if (strings[1].toLowerCase().endsWith("d")) {
                length = 1000 * 60 * 60 * 24 * (long) Integer.parseInt(strings[1].replace("d", ""));
                duration = true;
            } else if (strings[1].toLowerCase().endsWith("h")) {
                length = 1000 * 60 * 60 * (long) Integer.parseInt(strings[1].replace("h", ""));
                duration = true;
            } else if (strings[1].endsWith("m")) {
                length = 1000 * 60 * (long) Integer.parseInt(strings[1].replace("m", ""));
                duration = true;
            } else if (strings[1].toLowerCase().endsWith("s")) {
                length = 1000 * (long) Integer.parseInt(strings[1].replace("s", ""));
                duration = true;
            }else {
                duration = false;
            }
        }catch(NumberFormatException e){
            player.sendMessage(new ComponentBuilder(prefix).append(strings[1] + " is not a valid duration!").color(ChatColor.RED).create());
            player.sendMessage(new ComponentBuilder(prefix).append("Mute a player from speaking").color(ChatColor.RED).append("\nUsage: /mute <player> [length<s|m|h|d|w|M|perm>] [reason]").color(ChatColor.WHITE).create());
            return;
        }
        StringBuilder reason = new StringBuilder();
        if (strings.length > 2 && duration) {
            for (int i = 2; i < strings.length; i++) {
                reason.append(strings[i]).append(" ");
            }
        } else if (!duration) {
            for (int i = 1; i < strings.length; i++) {
                reason.append(strings[i]).append(" ");
            }
            length = (long) 1000 * 60 * 60 * 24 * 7 * 4 * 12 * 54;
        }else {
            reason.append("Manually Muted");
        }
        String reasonString = reason.toString().replace("\"", "'");
        try {
            String sql = "SELECT * FROM `staffhistory` WHERE UUID='" + player.getUniqueId().toString().replace("-", "") + "'";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                int Punishmentno = results.getInt("Manual Punishments");
                Punishmentno++;
                String sql1 = "UPDATE `staffhistory` SET `Manual Punishments`=" + Punishmentno + " WHERE UUID='" + player.getUniqueId().toString().replace("-", "") + "';";
                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                stmt1.executeUpdate();
            }
            String sql2 = "SELECT * FROM `history` WHERE UUID='" + targetuuid + "'";
            PreparedStatement stmt2 = plugin.connection.prepareStatement(sql2);
            ResultSet results1 = stmt2.executeQuery();
            if (results1.next()) {
                int Punishmentno1 = results1.getInt("Manual Punishments");
                Punishmentno1++;
                String sql3 = "UPDATE `history` SET `Manual Punishments`='" + Punishmentno1 + "' WHERE `UUID`='" + targetuuid + "' ;";
                PreparedStatement stmt3 = plugin.connection.prepareStatement(sql3);
                stmt3.executeUpdate();
            }
        }catch (SQLException e){
            plugin.mysqlfail(e);
            if (plugin.testConnectionManual())
                this.execute(commandSender, strings);
            return;
        }
        try {
            String sql = "SELECT * FROM `mutes` WHERE UUID='" + targetuuid + "'";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            ResultSet results = stmt.executeQuery();
            if (!results.next()) {
                String sql1 = "INSERT INTO `mutes` (`UUID`, `Name`, `Length`, `Reason`, `Punisher`) VALUES ('"+ targetuuid + "', '" + targetname + "', '" + (length + System.currentTimeMillis()) + "', \"" + reasonString + "\", '" + player.getName() + "');";
                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                stmt1.executeUpdate();
            }else{
                String sql1 = "UPDATE `mutes` SET `UUID`='" + targetuuid + "', `Name`='" + targetname + "', `Length`='" + (length + System.currentTimeMillis()) + "', `Reason`=\"" + reasonString + "\", `Punisher`='" + player.getName() + "' WHERE `UUID`='" + targetuuid + "' ;";
                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                stmt1.executeUpdate();
            }
        }catch (SQLException e){
            plugin.mysqlfail(e);
            if (plugin.testConnectionManual())
                this.execute(commandSender, strings);
            return;
        }
        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(targetname);
        Long muteleftmillis = length;
        long daysleft = muteleftmillis / (1000 * 60 * 60 * 24);
        long hoursleft = (long) Math.floor(muteleftmillis / (1000 * 60 * 60) % 24);
        long minutesleft = (long) Math.floor(muteleftmillis / (1000 * 60) % 60);
        long secondsleft = (long) Math.floor(muteleftmillis / 1000 % 60);
        if (target != null) {
            ProxyServer.getInstance().createTitle().subTitle(new TextComponent(ChatColor.DARK_RED + "You have been Muted!!")).fadeIn(5).stay(100).fadeOut(5).send(target);
            target.sendMessage(new TextComponent("\n"));
            target.sendMessage(new ComponentBuilder(prefix).append("You have been Muted! Reason: " + reason.toString()).color(ChatColor.RED).create());
            target.sendMessage(new ComponentBuilder(prefix).append("Something you did was against our server rules!").color(ChatColor.RED).create());
            target.sendMessage(new ComponentBuilder(prefix).append("Do /rules for more info!").color(ChatColor.RED).create());
            if (daysleft > 500) {
                target.sendMessage(new ComponentBuilder(prefix).append("This mute is permanent and does not expire").color(ChatColor.RED).create());
                target.sendMessage(new TextComponent("\n"));
            } else {
                target.sendMessage(new ComponentBuilder(prefix).append("This mute expires in: " + daysleft + "d " + hoursleft + "hr " + minutesleft + "m " + secondsleft + "s").color(ChatColor.RED).create());
                target.sendMessage(new TextComponent("\n"));
            }
        }
        StaffChat.sendMessage(player.getName() + " Muted: " + targetname + " for: " + reason.toString());
        if (daysleft > 500)
            StaffChat.sendMessage("This mute is permanent and does not expire!");
        else
            StaffChat.sendMessage("This mute expires in: " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s");
        ReputationSystem.minusRep(targetname, targetuuid, 2);
        BungeeMain.Logs.info(targetname + " was muted by " + player.getName() + " for: " + reason.toString());
    }
}
