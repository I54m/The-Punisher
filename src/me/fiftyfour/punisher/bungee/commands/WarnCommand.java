package me.fiftyfour.punisher.bungee.commands;

import com.google.common.collect.Lists;
import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.chats.StaffChat;
import me.fiftyfour.punisher.systems.Permissions;
import me.fiftyfour.punisher.systems.ReputationSystem;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WarnCommand extends Command {
    private BungeeMain plugin = BungeeMain.getInstance();
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;
    private int sqlfails = 0;

    public WarnCommand() {
        super("warn", "punisher.warn");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage(new TextComponent("You must be a player to use this command!"));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) commandSender;
        if (strings.length == 0) {
            player.sendMessage(new ComponentBuilder(prefix).append("Warn a player for breaking the rules").color(ChatColor.RED).append("\nUsage: /warn <player> [reason]").color(ChatColor.WHITE).create());
            return;
        }
        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(strings[0]);
        if (target == null) {
            player.sendMessage(new ComponentBuilder("That is not a player's name!").color(ChatColor.RED).create());
            return;
        }
        String targetuuid = target.getUniqueId().toString().replace("-", "");
        try {
            String sql = "SELECT * FROM `history` WHERE UUID='" + targetuuid + "'";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            ResultSet results = stmt.executeQuery();
            if (!results.next()) {
                String sql1 = "INSERT INTO `history` (UUID) VALUES ('"+ targetuuid + "');";
                PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                stmt1.executeUpdate();
                stmt1.close();
            }
            stmt.close();
            results.close();
            String sql2 = "SELECT * FROM `staffhistory` WHERE UUID='" + player.getUniqueId().toString().replace("-", "") + "'";
            PreparedStatement stmt2 = plugin.connection.prepareStatement(sql2);
            ResultSet results2 = stmt2.executeQuery();
            if (!results2.next()) {
                String sql3 = "INSERT INTO `staffhistory` (UUID) VALUES ('"+ player.getUniqueId().toString().replace("-", "") + "');";
                PreparedStatement stmt3 = plugin.connection.prepareStatement(sql3);
                stmt3.executeUpdate();
                stmt3.close();
            }
            stmt2.close();
            results2.close();
        }catch (SQLException e){
            plugin.getLogger().severe(prefix + e);
            sqlfails++;
            if(sqlfails > 5){
                plugin.getProxy().getPluginManager().unregisterCommand(this);
                commandSender.sendMessage(new ComponentBuilder(this.getName() + Lists.asList(strings[0], strings).toString() + " has thrown an exception more than 5 times!").color(ChatColor.RED).create());
                commandSender.sendMessage(new ComponentBuilder("Disabling command to prevent further damage to database").color(ChatColor.RED).create());
                plugin.getLogger().severe(prefix + this.getName() + Lists.asList(strings[0], strings).toString() + " has thrown an exception more than 5 times!");
                plugin.getLogger().severe(prefix + "Disabling command to prevent further damage to database!");
                BungeeMain.Logs.severe(this.getName() + " has thrown an exception more than 5 times!");
                BungeeMain.Logs.severe("Disabling command to prevent further damage to database!");
                return;
            }
            if (plugin.testConnectionManual())
                this.execute(commandSender, strings);
        }
        StringBuilder sb = new StringBuilder();
        if (strings.length == 1) {
            sb.append("Manually Warned");
        } else {
            for (int i = 1; i < strings.length; i++)
                sb.append(strings[i]).append(" ");
        }
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
                stmt1.close();
            }
            stmt.close();
            results.close();
            String sql2 = "SELECT * FROM `history` WHERE UUID='" + targetuuid + "'";
            PreparedStatement stmt2 = plugin.connection.prepareStatement(sql2);
            ResultSet results1 = stmt2.executeQuery();
            if (results1.next()) {
                int Punishmentno1 = results1.getInt("Manual Punishments");
                Punishmentno1++;
                String sql3 = "UPDATE `history` SET `Manual Punishments`='" + Punishmentno1 + "' WHERE `UUID`='" + targetuuid + "' ;";
                PreparedStatement stmt3 = plugin.connection.prepareStatement(sql3);
                stmt3.executeUpdate();
                stmt3.close();
            }
            stmt2.close();
            results1.close();
        }catch (SQLException e){
            plugin.getLogger().severe(prefix + e);
            sqlfails++;
            if(sqlfails > 5){
                plugin.getProxy().getPluginManager().unregisterCommand(this);
                commandSender.sendMessage(new ComponentBuilder(this.getName() + Lists.asList(strings[0], strings).toString() + " has thrown an exception more than 5 times!").color(ChatColor.RED).create());
                commandSender.sendMessage(new ComponentBuilder("Disabling command to prevent further damage to database").color(ChatColor.RED).create());
                plugin.getLogger().severe(prefix + this.getName() + Lists.asList(strings[0], strings).toString() + " has thrown an exception more than 5 times!");
                plugin.getLogger().severe(prefix + "Disabling command to prevent further damage to database!");
                BungeeMain.Logs.severe(this.getName() + " has thrown an exception more than 5 times!");
                BungeeMain.Logs.severe("Disabling command to prevent further damage to database!");
                return;
            }
            if (plugin.testConnectionManual())
                this.execute(commandSender, strings);
        }
        if (!Permissions.higher(player, targetuuid, target.getName())){
            player.sendMessage(new ComponentBuilder(prefix).append("You cannot punish that player!").color(ChatColor.RED).create());
            return;
        }
        StaffChat.sendMessage(player.getName() + " Warned: " + target.getName() + " for: " + sb.toString());
        try {
            ByteArrayOutputStream outbytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(outbytes);
            out.writeUTF("Punisher");
            out.writeUTF("PlayPunishSound");
            player.getServer().sendData("BungeeCord", outbytes.toByteArray());
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
        ProxyServer.getInstance().createTitle().title(new TextComponent(ChatColor.DARK_RED + "You have been Warned!!")).subTitle(new TextComponent(ChatColor.RED + "Reason: " + sb.toString())).fadeIn(5).stay(100).fadeOut(5).send(target);
        target.sendMessage(new TextComponent("\n"));
        target.sendMessage(new ComponentBuilder(prefix).append("You have been Warned, Reason: " + sb).color(ChatColor.RED).create());
        target.sendMessage(new ComponentBuilder(prefix).append("Something you did was against our server rules!").color(ChatColor.RED).create());
        target.sendMessage(new ComponentBuilder(prefix).append("Next time there may be harsher punishments!!").color(ChatColor.RED).create());
        target.sendMessage(new ComponentBuilder(prefix).append("Do /rules for the server rules to get more info!!").color(ChatColor.RED).create());
        target.sendMessage(new TextComponent("\n"));
        ReputationSystem.minusRep(target.getName(), targetuuid, 0.5);
        BungeeMain.Logs.info(target.getName() + " was Warned by: " + player.getName() + " for " + sb.toString());
    }
}