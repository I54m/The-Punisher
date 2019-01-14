package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.managers.PunishmentManager;
import me.fiftyfour.punisher.bungee.objects.Punishment;
import me.fiftyfour.punisher.universal.systems.Permissions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.SQLException;

public class WarnCommand extends Command {
    private BungeeMain plugin = BungeeMain.getInstance();
    private int sqlfails = 0;
    private PunishmentManager punishMnger = PunishmentManager.getInstance();

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
            player.sendMessage(new ComponentBuilder(plugin.prefix).append("Warn a player for breaking the rules").color(ChatColor.RED).append("\nUsage: /warn <player> [reason]").color(ChatColor.WHITE).create());
            return;
        }
        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(strings[0]);
        if (target == null) {
            player.sendMessage(new ComponentBuilder("That is not a player's name!").color(ChatColor.RED).create());
            return;
        }
        String targetuuid = target.getUniqueId().toString().replace("-", "");
        StringBuilder sb = new StringBuilder();
        if (strings.length == 1) {
            sb.append("Manually Warned");
        } else {
            for (int i = 1; i < strings.length; i++)
                sb.append(strings[i]).append(" ");
        }
        try {
            if (!Permissions.higher(player, targetuuid, target.getName())) {
                player.sendMessage(new ComponentBuilder(plugin.prefix).append("You cannot punish that player!").color(ChatColor.RED).create());
                return;
            }
        }catch (Exception e){
            player.sendMessage(new ComponentBuilder(plugin.prefix).append("ERROR: ").color(ChatColor.DARK_RED).append("Luckperms was unable to fetch permission data on: " + target.getName()).color(ChatColor.RED).create());
            player.sendMessage(new ComponentBuilder(plugin.prefix).append("This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ").color(ChatColor.RED).create());
            BungeeMain.Logs.severe("ERROR: Luckperms was unable to fetch permission data on: " + target.getName());
            BungeeMain.Logs.severe("Error message: " + e.getMessage());
            StringBuilder stacktrace = new StringBuilder();
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                stacktrace.append(stackTraceElement.toString()).append("\n");
            }
            BungeeMain.Logs.severe("Stack Trace: " + stacktrace.toString());
            return;
        }
        try {
            Punishment warn = new Punishment(Punishment.Reason.Manual, sb.toString(), null, Punishment.Type.WARN, targetuuid, player.getUniqueId().toString().replace("-", ""));
            punishMnger.issue(warn, player, target.getName(), true, true, true);
        }catch (SQLException e){
            plugin.getLogger().severe(plugin.prefix + e);
            sqlfails++;
            if(sqlfails > 5){
                plugin.getProxy().getPluginManager().unregisterCommand(this);
                StringBuilder sbError = new StringBuilder();
                for (String args : strings){
                    sbError.append(args).append(" ");
                }
                commandSender.sendMessage(new ComponentBuilder(this.getName() + sbError.toString() + " has thrown an exception more than 5 times!").color(ChatColor.RED).create());
                commandSender.sendMessage(new ComponentBuilder("Disabling command to prevent further damage to database").color(ChatColor.RED).create());
                plugin.getLogger().severe(plugin.prefix + this.getName() + sbError.toString() + " has thrown an exception more than 5 times!");
                plugin.getLogger().severe(plugin.prefix + "Disabling command to prevent further damage to database!");
                BungeeMain.Logs.severe(this.getName() + " has thrown an exception more than 5 times!");
                BungeeMain.Logs.severe("Disabling command to prevent further damage to database!");
                return;
            }
            if (plugin.testConnectionManual())
                this.execute(commandSender, strings);
        }
    }
}