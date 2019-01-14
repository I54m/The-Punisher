package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.chats.AdminChat;
import me.fiftyfour.punisher.bungee.discordbot.DiscordMain;
import me.fiftyfour.punisher.bungee.managers.PunishmentManager;
import me.fiftyfour.punisher.bungee.objects.Punishment;
import me.fiftyfour.punisher.universal.systems.UpdateChecker;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static me.fiftyfour.punisher.bungee.BungeeMain.update;

public class AdminCommands extends Command {
    private BungeeMain plugin = BungeeMain.getInstance();
    private int sqlfails = 0;
    private PunishmentManager punisher = PunishmentManager.getInstance();
    private Map<ProxiedPlayer, Long> confirmation = new HashMap<>();

    public AdminCommands() {
        super("punisher", "punisher.admin");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length != 0) {
            if (commandSender instanceof ProxiedPlayer) {
                ProxiedPlayer player = (ProxiedPlayer) commandSender;
                if (strings[0].equalsIgnoreCase("reseteverything") || (strings.length > 1 && strings[1].equalsIgnoreCase("reset"))){
                    if (!confirmation.containsKey(player)){
                        player.sendMessage(new ComponentBuilder(plugin.prefix).append("This command will reset settings and values to their defaults!").color(ChatColor.RED).create());
                        player.sendMessage(new ComponentBuilder(plugin.prefix).append("THIS CANNOT BE UNDONE!!").bold(true).color(ChatColor.RED).create());
                        player.sendMessage(new ComponentBuilder(plugin.prefix).append("It is recommended you use caution with this command!").color(ChatColor.RED).create());
                        player.sendMessage(new ComponentBuilder(plugin.prefix).append("To confirm, please re-type the command within 30 seconds!").color(ChatColor.RED).create());
                        confirmation.put(player, System.currentTimeMillis());
                        return;
                    }else{
                        long time = confirmation.get(player);
                        if (System.currentTimeMillis() - time > 30000){
                            player.sendMessage(new ComponentBuilder(plugin.prefix).append("This command will reset settings and values to their defaults!").color(ChatColor.RED).create());
                            player.sendMessage(new ComponentBuilder(plugin.prefix).append("THIS CANNOT BE UNDONE!!").bold(true).color(ChatColor.RED).create());
                            player.sendMessage(new ComponentBuilder(plugin.prefix).append("It is recommended you use caution with this command!").color(ChatColor.RED).create());
                            player.sendMessage(new ComponentBuilder(plugin.prefix).append("To confirm, please re-type the command within 30 seconds!").color(ChatColor.RED).create());
                            confirmation.put(player, System.currentTimeMillis());
                            return;
                        }else confirmation.remove(player);
                    }
                }
                if (strings[0].equalsIgnoreCase("punishments")) {
                    if (strings.length == 1) {
                        player.sendMessage(new ComponentBuilder(plugin.prefix).append("Usage: /punisher punishments <reset|delete>").color(ChatColor.RED).create());
                        return;
                    }
                    if (strings[1].equalsIgnoreCase("reset")) {
                        plugin.saveDefaultPunishments();
                        plugin.loadConfig();
                        BungeeMain.Logs.warning(player.getName() + " reset punishments");
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
                        AdminChat.sendMessage("The plugin will no longer punish as intended!!!");
                        AdminChat.sendMessage("To reset punishments to their defaults do: ");
                        AdminChat.sendMessage("/punisher punishments reset");
                        AdminChat.sendMessage("\n");
                        BungeeMain.Logs.warning(player.getName() + " deleted punishments");
                    }else {
                        player.sendMessage(new ComponentBuilder(plugin.prefix).append("Usage: /punisher punishments <reset|delete>").color(ChatColor.RED).create());
                    }
                } else if (strings[0].equalsIgnoreCase("bans")) {
                    if (strings.length == 1) {
                        player.sendMessage(new ComponentBuilder(plugin.prefix).append("Usage: /punisher bans reset").color(ChatColor.RED).create());
                        return;
                    }
                    if (strings[1].equalsIgnoreCase("reset")) {
                        try{
                            String sql1 = "TRUNCATE `bans`";
                            PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                            stmt1.executeUpdate();
                            stmt1.close();
                            punisher.resetCache();
                        }catch (SQLException e){
                            plugin.getLogger().severe(plugin.prefix + e);
                            sqlfails++;
                            if (sqlfails > 5) {
                                StringBuilder sb = new StringBuilder();
                                for (String args : strings){
                                    sb.append(args).append(" ");
                                }
                                plugin.getProxy().getPluginManager().unregisterCommand(this);
                                commandSender.sendMessage(new ComponentBuilder(this.getName() + " " + sb.toString() + " has thrown an exception more than 5 times!").color(ChatColor.RED).create());
                                commandSender.sendMessage(new ComponentBuilder("Disabling command to prevent further damage to database").color(ChatColor.RED).create());
                                plugin.getLogger().severe(plugin.prefix + this.getName() + " " + sb.toString() + " has thrown an exception more than 5 times!");
                                plugin.getLogger().severe(plugin.prefix + "Disabling command to prevent further damage to database!");
                                BungeeMain.Logs.severe(this.getName() + " has thrown an exception more than 5 times!");
                                BungeeMain.Logs.severe("Disabling command to prevent further damage to database!");
                                return;
                            }
                            if (plugin.testConnectionManual())
                                this.execute(commandSender, strings);
                        }
                        AdminChat.sendMessage("\n");
                        AdminChat.sendMessage(player.getName() + " has reset all bans!");
                        AdminChat.sendMessage("\n");
                        BungeeMain.Logs.warning(player.getName() + " reset bans");
                    }
                } else if (strings[0].equalsIgnoreCase("mutes")) {
                    if (strings.length == 1) {
                        player.sendMessage(new ComponentBuilder(plugin.prefix).append("Usage: /punisher mutes reset").color(ChatColor.RED).create());
                        return;
                    }
                    if (strings[1].equalsIgnoreCase("reset")) {
                        try{
                            String sql1 = "TRUNCATE `mutes`";
                            PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                            stmt1.executeUpdate();
                            stmt1.close();
                            punisher.resetCache();
                        }catch (SQLException e){
                            plugin.getLogger().severe(plugin.prefix + e);
                            sqlfails++;
                            if (sqlfails > 5) {
                                StringBuilder sb = new StringBuilder();
                                for (String args : strings){
                                    sb.append(args).append(" ");
                                }
                                plugin.getProxy().getPluginManager().unregisterCommand(this);
                                commandSender.sendMessage(new ComponentBuilder(this.getName() + " " + sb.toString() + " has thrown an exception more than 5 times!").color(ChatColor.RED).create());
                                commandSender.sendMessage(new ComponentBuilder("Disabling command to prevent further damage to database").color(ChatColor.RED).create());
                                plugin.getLogger().severe(plugin.prefix + this.getName() + " " + sb.toString() + " has thrown an exception more than 5 times!");
                                plugin.getLogger().severe(plugin.prefix + "Disabling command to prevent further damage to database!");
                                BungeeMain.Logs.severe(this.getName() + " has thrown an exception more than 5 times!");
                                BungeeMain.Logs.severe("Disabling command to prevent further damage to database!");
                                return;
                            }
                            if (plugin.testConnectionManual())
                                this.execute(commandSender, strings);
                        }
                        AdminChat.sendMessage("\n");
                        AdminChat.sendMessage(player.getName() + " has reset all mutes!");
                        AdminChat.sendMessage("\n");
                        BungeeMain.Logs.warning(player.getName() + " reset mutes");
                    }
                } else if (strings[0].equalsIgnoreCase("reload")) {
                    player.sendMessage(new ComponentBuilder(plugin.prefix).append("Reloading plugin. Please wait....").color(ChatColor.RED).create());
                    plugin.getProxy().getPluginManager().unregisterCommands(plugin);
                    plugin.getProxy().getPluginManager().unregisterListeners(plugin);
                    plugin.onDisable();
                    plugin.onEnable();
                    player.sendMessage(new ComponentBuilder(plugin.prefix).append("Plugin Reloaded!").color(ChatColor.RED).create());
                    AdminChat.sendMessage(player.getName() + " has reloaded the plugin!");
                    plugin.getProxy().getScheduler().runAsync(plugin, () -> {
                        punisher.resetCache();
                        for (ProxiedPlayer players : plugin.getProxy().getPlayers()) {
                            if (punisher.isBanned(player.getUniqueId().toString().replace("-", ""))) {
                                Punishment ban = punisher.getBan(player.getUniqueId().toString().replace("-", ""));
                                Long banleftmillis = ban.getDuration() - System.currentTimeMillis();
                                int daysleft = (int) (banleftmillis / (1000 * 60 * 60 * 24));
                                int hoursleft = (int) (banleftmillis / (1000 * 60 * 60) % 24);
                                int minutesleft = (int) (banleftmillis / (1000 * 60) % 60);
                                int secondsleft = (int) (banleftmillis / 1000 % 60);
                                String reason = ban.getMessage();
                                if (daysleft > 730) {
                                    String banMessage = BungeeMain.PunisherConfig.getString("PermBan Message").replace("%days%", String.valueOf(daysleft))
                                            .replace("%hours%", String.valueOf(hoursleft)).replace("%minutes%", String.valueOf(minutesleft))
                                            .replace("%seconds%", String.valueOf(secondsleft)).replace("%reason%", reason);
                                    players.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', banMessage)));
                                } else {
                                    String banMessage = BungeeMain.PunisherConfig.getString("TempBan Message").replace("%days%", String.valueOf(daysleft))
                                            .replace("%hours%", String.valueOf(hoursleft)).replace("%minutes%", String.valueOf(minutesleft))
                                            .replace("%seconds%", String.valueOf(secondsleft)).replace("%reason%", reason);
                                    players.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', banMessage)));
                                }
                            }
                        }
                    });
                    BungeeMain.Logs.warning(player.getName() + " reloaded the plugin ");
                } else if (strings[0].equalsIgnoreCase("testsql")){
                    plugin.testConnectionManual();
                    BungeeMain.Logs.warning(player.getName() + " tested sql connection");
                }else if (strings[0].equalsIgnoreCase("help")){
                    plugin.getProxy().getPluginManager().dispatchCommand(commandSender, "punisherhelp");
                }else if (strings[0].equalsIgnoreCase("reseteverything")){
                    try{
                        String sql1 = "DROP DATABASE `" + BungeeMain.database + "`";
                        PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                        stmt1.executeUpdate();
                        stmt1.close();
                    }catch (SQLException e){
                        plugin.getLogger().severe(plugin.prefix + e);
                        sqlfails++;
                        if (sqlfails > 5) {
                            StringBuilder sb = new StringBuilder();
                            for (String args : strings){
                                sb.append(args).append(" ");
                            }
                            plugin.getProxy().getPluginManager().unregisterCommand(this);
                            commandSender.sendMessage(new ComponentBuilder(this.getName() + " " + sb.toString() + " has thrown an exception more than 5 times!").color(ChatColor.RED).create());
                            commandSender.sendMessage(new ComponentBuilder("Disabling command to prevent further damage to database").color(ChatColor.RED).create());
                            plugin.getLogger().severe(plugin.prefix + this.getName() + " " + sb.toString() + " has thrown an exception more than 5 times!");
                            plugin.getLogger().severe(plugin.prefix + "Disabling command to prevent further damage to database!");
                            BungeeMain.Logs.severe(this.getName() + " has thrown an exception more than 5 times!");
                            BungeeMain.Logs.severe("Disabling command to prevent further damage to database!");
                            return;
                        }
                        if (plugin.testConnectionManual())
                            this.execute(commandSender, strings);
                    }
                    BungeeMain.Punishments.delete();
                    BungeeMain.Reputation.delete();
                    BungeeMain.PlayerInfo.delete();
                    if (BungeeMain.PunisherConfig.getBoolean("DiscordIntegration.Enabled")) {
                        BungeeMain.DiscordIntegration.delete();
                        DiscordMain.verifiedUsers.clear();
                        AdminChat.sendMessage("");
                        AdminChat.sendMessage(player.getName() + " has reset all linked discord users!");
                    }
                    BungeeMain.PlayerInfo.delete();
                    AdminChat.sendMessage("");
                    AdminChat.sendMessage(player.getName() + " has reset all punishments");
                    AdminChat.sendMessage("This means automatic punishments will use default values!");
                    AdminChat.sendMessage("");
                    AdminChat.sendMessage(player.getName() + " has reset all reputation scores!");
                    AdminChat.sendMessage("");
                    AdminChat.sendMessage(player.getName() + " has reset all stored playerinfo!");
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
                    plugin.getProxy().getPluginManager().unregisterCommands(plugin);
                    plugin.getProxy().getPluginManager().unregisterListeners(plugin);
                    plugin.onDisable();
                    plugin.onEnable();
                    AdminChat.sendMessage(player.getName() + " has reloaded the plugin!");
                    for (ProxiedPlayer all : ProxyServer.getInstance().getPlayers()){
                        BungeeMain.RepStorage.set(all.getUniqueId().toString().replace("-", ""), 5.0);
                    }
                    AdminChat.sendMessage("");
                    AdminChat.sendMessage("Plugin successfully reset!");
                    BungeeMain.Logs.warning(player.getName() + " reset everything");
                }else if (strings[0].equalsIgnoreCase("version")){
                    if (!update) {
                        player.sendMessage(new ComponentBuilder(plugin.prefix).append("Current Bungeecord Version: " + plugin.getDescription().getVersion()).color(ChatColor.GREEN).create());
                        player.sendMessage(new ComponentBuilder(plugin.prefix).append("This is the latest version!").color(ChatColor.GREEN).create());
                    }else{
                        player.sendMessage(new ComponentBuilder(plugin.prefix).append("Current Bungeecord Version: " + plugin.getDescription().getVersion()).color(ChatColor.RED).create());
                        player.sendMessage(new ComponentBuilder(plugin.prefix).append("Latest version: " + UpdateChecker.getCurrentVersion()).color(ChatColor.RED).create());
                    }
                }else {
                    commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("|------------").strikethrough(true).append(plugin.prefix).strikethrough(false).append("------------|").strikethrough(true).create());
                    commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("/punisher reseteverything - ").color(ChatColor.RED).append("Reset all stored data in the sql data base and on file.").color(ChatColor.WHITE).create());
                    commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("/punisher help - ").color(ChatColor.RED).append("Help command for the punisher.").color(ChatColor.WHITE).create());
                    commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("/punisher punishments - ").color(ChatColor.RED).append("Reset or delete punishments used for punishment calculation.").color(ChatColor.WHITE).create());
                    commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("/punisher bans reset - ").color(ChatColor.RED).append("Reset and delete all bans.").color(ChatColor.WHITE).create());
                    commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("/punisher mutes reset - ").color(ChatColor.RED).append("Reset and delete all mutes.").color(ChatColor.WHITE).create());
                    commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("/punisher reload - ").color(ChatColor.RED).append("Reload the config files.").color(ChatColor.WHITE).create());
                    commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("/punisher testsql - ").color(ChatColor.RED).append("perform a manual sql connection test.").color(ChatColor.WHITE).create());
                    commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("/punisher version - ").color(ChatColor.RED).append("Get the current version").color(ChatColor.WHITE).create());
                    if (BungeeMain.PunisherConfig.getBoolean("DiscordIntegration.Enabled"))
                        commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("/discord admin - ").color(ChatColor.RED).append("Admin commands for the discord integration").color(ChatColor.WHITE).create());
                }
            } else {
                commandSender.sendMessage(new TextComponent("You must be a player to use this command!"));
            }
        } else {
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("|------------").strikethrough(true).append(plugin.prefix).strikethrough(false).append("------------|").strikethrough(true).create());
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("/punisher reseteverything - ").color(ChatColor.RED).append("Reset all stored data in the sql data base and on file.").color(ChatColor.WHITE).create());
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("/punisher help - ").color(ChatColor.RED).append("Help command for the punisher.").color(ChatColor.WHITE).create());
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("/punisher punishments - ").color(ChatColor.RED).append("Reset or delete punishments used for punishment calculation.").color(ChatColor.WHITE).create());
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("/punisher bans reset - ").color(ChatColor.RED).append("Reset and delete all bans.").color(ChatColor.WHITE).create());
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("/punisher mutes reset - ").color(ChatColor.RED).append("Reset and delete all mutes.").color(ChatColor.WHITE).create());
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("/punisher reload - ").color(ChatColor.RED).append("Reload the config files.").color(ChatColor.WHITE).create());
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("/punisher testsql - ").color(ChatColor.RED).append("perform a manual sql connection test.").color(ChatColor.WHITE).create());
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("/punisher version - ").color(ChatColor.RED).append("Get the current version").color(ChatColor.WHITE).create());
            if (BungeeMain.PunisherConfig.getBoolean("DiscordIntegration.Enabled"))
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("/discord admin - ").color(ChatColor.RED).append("Admin commands for the discord integration").color(ChatColor.WHITE).create());
        }
    }
}