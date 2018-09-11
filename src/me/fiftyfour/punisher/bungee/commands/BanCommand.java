package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.bungeeMain;
import me.fiftyfour.punisher.bungee.fetchers.NameFetcher;
import me.fiftyfour.punisher.bungee.fetchers.UUIDFetcher;
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
import java.time.LocalDateTime;

public class BanCommand extends Command {
    private bungeeMain plugin = bungeeMain.getInstance();
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;
    private long length;

    public BanCommand() {
        super("ban", "punisher.ban", "tempban", "ipban", "banip");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage(new TextComponent("You must be a player to use this command!"));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) commandSender;
        if (strings.length == 0) {
            player.sendMessage(new ComponentBuilder(prefix).append("Ban a player from the server").color(ChatColor.RED).append("\nUsage: /ban <player> [length<s|m|h|d|w|M|y>] [reason]").color(ChatColor.WHITE).create());
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
            e.printStackTrace();
            return;
        }
        boolean duration;
        try {
            if (strings.length == 1 || strings[1].toLowerCase().endsWith("y")) {
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
            player.sendMessage(new ComponentBuilder(prefix).append("Ban a player from the server").color(ChatColor.RED).append("\nUsage: /ban <player> [length<s|m|h|d|w|M|y>] [reason]").color(ChatColor.WHITE).create());
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
            reason.append("Manually Banned");
        }
        try {
            String sql = "SELECT * FROM `staffhistory` WHERE UUID='" + player.getUniqueId().toString().replace("-", "") + "'";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            ResultSet results = stmt.executeQuery();
            if (!results.next()) {
                int Punishmentno = results.getInt("Manual Punishments");
                Punishmentno++;
                String sql1 =  "UPDATE `staffhistory` SET `Manual Punishments`=" + Punishmentno + ";";
                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                stmt1.executeUpdate();
            }else {
                int Punishmentno = results.getInt("Manual Punishments");
                Punishmentno++;
                String sql1 = "UPDATE `staffhistory` SET `Manual Punishments`=" + Punishmentno + ";";
                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                stmt1.executeUpdate();
            }
            String sql2 = "SELECT * FROM `history` WHERE UUID='" + targetuuid + "'";
            PreparedStatement stmt2 = plugin.connection.prepareStatement(sql2);
            ResultSet results1 = stmt2.executeQuery();
            if (!results1.next()) {
                int Punishmentno1 = results1.getInt("Manual Punishments");
                Punishmentno1++;
                String sql3 = "UPDATE `history` SET `Manual Punishments`=" + Punishmentno1 + ";";
                PreparedStatement stmt3 = plugin.connection.prepareStatement(sql3);
                stmt3.executeUpdate();
            }else{
                int Punishmentno1 = results1.getInt("Manual Punishments");
                Punishmentno1++;
                String sql3 = "UPDATE `history` SET `Manual Punishments`=" + Punishmentno1 + ";";
                PreparedStatement stmt3 = plugin.connection.prepareStatement(sql3);
                stmt3.executeUpdate();
            }
        }catch (SQLException e){
            e.printStackTrace();
            return;
        }
        try {
            String sql = "SELECT * FROM `bans` WHERE UUID='" + targetuuid + "'";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            ResultSet results = stmt.executeQuery();
            if (!results.next()) {
                String sql1 = "INSERT INTO `bans` (`UUID`, `Name`, `Length`, `Reason`) VALUES ('"+ targetuuid + "', '" + targetname + "', '" + (length + System.currentTimeMillis()) + "', '" + reason + "');";
                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                stmt1.executeUpdate();
            }else{
                String sql1 = "UPDATE `bans` SET (`UUID`, `Name`, `Length`, `Reason`) VALUES ('"+ targetuuid + "', '" + targetname + "', '" + (length + System.currentTimeMillis()) + "', '" + reason + "');";
                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                stmt1.executeUpdate();
            }
        }catch (SQLException e){
            e.printStackTrace();
            return;
        }
        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(targetname);
        Long bantime;
        try{
            String sql = "SELECT * FROM `bans` WHERE UUID='" + targetuuid + "'";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            ResultSet results = stmt.executeQuery();
            if (!results.next()) {
                bantime = results.getLong("Length");
            }else{
                bantime = results.getLong("Length");
            }
        }catch (SQLException e){
            e.printStackTrace();
            return;
        }
        Long banleftmillis = bantime - System.currentTimeMillis();
        long daysleft = banleftmillis / (1000 * 60 * 60 * 24);
        long hoursleft = (long) Math.floor(banleftmillis / (1000 * 60 * 60) % 24);
        long minutesleft = (long) Math.floor(banleftmillis / (1000 * 60) % 60);
        long secondsleft = (long) Math.floor(banleftmillis / 1000 % 60);
        if (target != null) {
            if (daysleft > 365) {
                target.disconnect(new TextComponent(ChatColor.RED + "You have been Banned from the server!" + "\n"
                        + "You were Banned for the reason: " + reason.toString() + "\n"
                        + "This ban does not expire!" + "\n"
                        + "You have done something against our server rules!" + "\n"
                        + "Some of our punishments result in a ban with no warning!"
                ));
            } else {
                target.disconnect(new TextComponent(ChatColor.RED + "You have been Banned from the server!" + "\n"
                        + "You were Banned for the reason: " + reason.toString() + "\n"
                        + "This ban expires in: " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s" + "\n"
                        + "You have done something against our server rules!" + "\n"//todo make it so that this message is configurable
                        + "Some of our punishments result in a ban with no warning!"
                ));
            }
        }
        for (ProxiedPlayer all : ProxyServer.getInstance().getPlayers()) {
            if (all.hasPermission("punisher.staffchat")) {
                all.sendMessage(new ComponentBuilder("[").color(ChatColor.GRAY).append("Staff Chat").color(ChatColor.RED).bold(true).append("]").color(ChatColor.GRAY).bold(false)
                        .append(" ").color(ChatColor.RESET).append("[").color(ChatColor.GRAY).bold(false).append(player.getServer().getInfo().getName()).color(ChatColor.RED).bold(true)
                        .append("] ").color(ChatColor.GRAY).bold(false).append(player.getName() + " Banned: " + targetname + " for: " + reason.toString()).color(ChatColor.RED).bold(false).create());
                if (daysleft > 365)
                    all.sendMessage(new ComponentBuilder("[").color(ChatColor.GRAY).append("Staff Chat").color(ChatColor.RED).bold(true).append("]").color(ChatColor.GRAY).bold(false)
                        .append(" ").color(ChatColor.RESET).append("[").color(ChatColor.GRAY).bold(false).append(player.getServer().getInfo().getName()).color(ChatColor.RED).bold(true)
                        .append("] ").color(ChatColor.GRAY).bold(false).append("This ban is permanent and does not expire!").color(ChatColor.RED).bold(false).create());
                else
                    all.sendMessage(new ComponentBuilder("[").color(ChatColor.GRAY).append("Staff Chat").color(ChatColor.RED).bold(true).append("]").color(ChatColor.GRAY).bold(false)
                            .append(" ").color(ChatColor.RESET).append("[").color(ChatColor.GRAY).bold(false).append(player.getServer().getInfo().getName()).color(ChatColor.RED).bold(true)
                            .append("] ").color(ChatColor.GRAY).bold(false).append("This ban expires in: " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s").color(ChatColor.RED).bold(false).create());
            }
        }
        bungeeMain.PunishmentLogs.set(targetname + "." + LocalDateTime.now(), "was banned by: " + player.getName() + " for: " + reason);
        bungeeMain.saveLogs();
    }
}