package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.chats.AdminChat;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AdminCommands extends Command {
    private BungeeMain plugin = BungeeMain.getInstance();
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;

    public AdminCommands() {
        super("punisher", "punisher.admin");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length != 0) {
            if (commandSender instanceof ProxiedPlayer) {
                ProxiedPlayer player = (ProxiedPlayer) commandSender;
                if (strings[0].equalsIgnoreCase("punishments")) {
                    if (strings.length == 1) {
                        player.sendMessage(new ComponentBuilder(prefix).append("Usage: /punisher <help|punishments|bans|mutes|reload|testsql>").color(ChatColor.RED).create());
                        return;
                    }
                    if (strings[1].equalsIgnoreCase("reset")) {
                        plugin.saveDefaultPunishments();
                        plugin.loadConfig();
                        AdminChat.sendMessage("\n");
                        AdminChat.sendMessage(player.getName() + " has reset all punishments");
                        AdminChat.sendMessage("This means automatic punishments will use default values!");
                        AdminChat.sendMessage("\n");
                    } else if (strings[1].equalsIgnoreCase("delete")) {
                        BungeeMain.PunishmentsConfig.set("Minor_Chat_Offence", null);
                        BungeeMain.PunishmentsConfig.set("Major_Chat_Offence", null);
                        BungeeMain.PunishmentsConfig.set("DDoS/DoX_Threats", null);
                        BungeeMain.PunishmentsConfig.set("Inappropriate_Link", null);
                        BungeeMain.PunishmentsConfig.set("Impersonating_Staff", null);
                        BungeeMain.PunishmentsConfig.set("X-Raying", null);
                        BungeeMain.PunishmentsConfig.set("AutoClicker(Non_PvP)", null);
                        BungeeMain.PunishmentsConfig.set("Fly/Speed_Hacking", null);
                        BungeeMain.PunishmentsConfig.set("Malicious_PvP_Hacks", null);
                        BungeeMain.PunishmentsConfig.set("Other_Hacks", null);
                        BungeeMain.PunishmentsConfig.set("Server_Advertisment", null);
                        BungeeMain.PunishmentsConfig.set("Exploiting", null);
                        BungeeMain.PunishmentsConfig.set("TPA-Trapping", null);
                        BungeeMain.PunishmentsConfig.set("Other_Major_Offence", null);
                        BungeeMain.PunishmentsConfig.set("Other_Minor_Offence", null);
                        BungeeMain.PunishmentsConfig.set("Player_Impersonation", null);
                        plugin.saveConfig();
                        plugin.loadConfig();
                        AdminChat.sendMessage("\n");
                        AdminChat.sendMessage(player.getName() + " has REMOVED all punishments!!!");
                        AdminChat.sendMessage("THIS IS NOT RECOMMENDED AND CAN NOT BE UNDONE!!!");
                        AdminChat.sendMessage("The plugin will no longer punish automatically!!!");
                        AdminChat.sendMessage("To reset punishments to their defaults do: ");
                        AdminChat.sendMessage("/punisher punishments reset");
                        AdminChat.sendMessage("\n");
                    }else {
                        player.sendMessage(new ComponentBuilder(prefix).append("Usage: /punisher punishments <reset|delete>").color(ChatColor.RED).create());
                    }
                } else if (strings[0].equalsIgnoreCase("bans")) {
                    if (strings.length == 1) {
                        player.sendMessage(new ComponentBuilder(prefix).append("Usage: /punisher bans reset").color(ChatColor.RED).create());
                        return;
                    }
                    if (strings[1].equalsIgnoreCase("reset")) {
                        try{
                            String sql1 = "TRUNCATE `bans`";
                            PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                            stmt1.executeUpdate();
                        }catch (SQLException e){
                            plugin.mysqlfail(e);
                            if (plugin.testConnectionManual())
                                this.execute(commandSender, strings);
                            return;
                        }
                        AdminChat.sendMessage("\n");
                        AdminChat.sendMessage(player.getName() + " has reset all bans!");
                        AdminChat.sendMessage("\n");
                    }
                } else if (strings[0].equalsIgnoreCase("mutes")) {
                    if (strings.length == 1) {
                        player.sendMessage(new ComponentBuilder(prefix).append("Usage: /punisher mutes reset").color(ChatColor.RED).create());
                        return;
                    }
                    if (strings[1].equalsIgnoreCase("reset")) {
                        try{
                            String sql1 = "TRUNCATE `mutes`";
                            PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                            stmt1.executeUpdate();
                        }catch (SQLException e){
                            plugin.mysqlfail(e);
                            if (plugin.testConnectionManual())
                                this.execute(commandSender, strings);
                            return;
                        }
                        AdminChat.sendMessage("\n");
                        AdminChat.sendMessage(player.getName() + " has reset all mutes!");
                        AdminChat.sendMessage("\n");
                    }
                } else if (strings[0].equalsIgnoreCase("reload")) {
                    plugin.loadConfig();
                    AdminChat.sendMessage(player.getName() + " has reloaded the plugin!");
                } else if (strings[0].equalsIgnoreCase("testsql")){
                    plugin.testConnectionManual();
                }else if (strings[0].equalsIgnoreCase("help")){
                    PunHelpCommand.executeCommand(commandSender, strings);
                }else if (strings[0].equalsIgnoreCase("reseteverything")){
                    try{
                        String sql1 = "DROP DATABASE `" + BungeeMain.database + "`";
                        PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                        stmt1.executeUpdate();
                    }catch (SQLException e){
                        plugin.mysqlfail(e);
                        if (plugin.testConnectionManual())
                            this.execute(commandSender, strings);
                        return;
                    }
                    BungeeMain.Punishments.delete();
                    BungeeMain.Reputation.delete();
                    AdminChat.sendMessage("");
                    AdminChat.sendMessage(player.getName() + " has reset all punishments");
                    AdminChat.sendMessage("This means automatic punishments will use default values!");
                    AdminChat.sendMessage("");
                    AdminChat.sendMessage(player.getName() + " has reset all mutes!");
                    AdminChat.sendMessage("");
                    AdminChat.sendMessage(player.getName() + " has reset all bans!");
                    AdminChat.sendMessage("");
                    AdminChat.sendMessage(player.getName() + " has reset all stored ips!");
                    AdminChat.sendMessage("");
                    AdminChat.sendMessage(player.getName() + " has reset all player history!");
                    AdminChat.sendMessage("");
                    AdminChat.sendMessage(player.getName() + " has reset all staff history!");
                    AdminChat.sendMessage("");
                    AdminChat.sendMessage(player.getName() + " has reset all reputation scores!");
                    AdminChat.sendMessage("");
                    AdminChat.sendMessage("Reloading plugin please wait....");
                    AdminChat.sendMessage("");
                    plugin.setupmysql();
                    plugin.loadConfig();
                    for (ProxiedPlayer all : ProxyServer.getInstance().getPlayers()){
                        BungeeMain.RepStorage.set(all.getUniqueId().toString().replace("-", ""), 5.0);
                    }
                    AdminChat.sendMessage("Plugin successfully reset!");
                    AdminChat.sendMessage("\n");
                }else {
                    player.sendMessage(new ComponentBuilder(prefix).append("Usage: /punisher <reseteverything|help|punishments|bans|mutes|reload|testsql>").color(ChatColor.RED).create());
                }
            } else {
                commandSender.sendMessage(new TextComponent("You must be a player to use this command!"));
            }
        } else {
            commandSender.sendMessage(new ComponentBuilder(prefix).append("Usage: /punisher <reseteverything|help|punishments|bans|mutes|reload|testsql>").color(ChatColor.RED).create());
        }
    }
}