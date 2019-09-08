package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.chats.AdminChat;
import me.fiftyfour.punisher.bungee.discordbot.DiscordMain;
import me.fiftyfour.punisher.bungee.handlers.ErrorHandler;
import me.fiftyfour.punisher.bungee.listeners.PlayerLogin;
import me.fiftyfour.punisher.bungee.listeners.ServerConnect;
import me.fiftyfour.punisher.bungee.managers.PunishmentManager;
import me.fiftyfour.punisher.bungee.objects.Punishment;
import me.fiftyfour.punisher.universal.exceptions.PunishmentsDatabaseException;
import me.fiftyfour.punisher.universal.systems.UpdateChecker;
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
                        AdminChat.sendMessage("\n");
                        plugin.saveDefaultPunishments();
                        BungeeMain.Logs.warning(player.getName() + " reset punishments");
                        AdminChat.sendMessage(player.getName() + " has reset all punishments");
                        AdminChat.sendMessage("This means automatic punishments will use default values!");
                        try {
                            plugin.loadConfig();
                        } catch (Exception e) {
                            player.sendMessage(new ComponentBuilder(plugin.prefix).append("Unable to reload config files, try restarting your server and try again.").color(ChatColor.RED).create());
                            AdminChat.sendMessage("Plugin failed to reload config! Try restarting the server and try again!");
                        }
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
                        AdminChat.sendMessage(" ");
                        AdminChat.sendMessage(player.getName() + " has REMOVED all automatic punishment values!");
                        AdminChat.sendMessage("Automatic punishments will not longer work!");
                        AdminChat.sendMessage("To reset punishments to their defaults do: ");
                        AdminChat.sendMessage("/punisher punishments reset");
                        AdminChat.sendMessage(" ");
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
                                try {
                                    throw new PunishmentsDatabaseException("Resetting bans", null, this.getName(), e);
                                } catch (PunishmentsDatabaseException pde) {
                                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                                    errorHandler.log(pde);
                                    errorHandler.alert(pde, commandSender);
                                    return;
                                }
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
                                try {
                                    throw new PunishmentsDatabaseException("Resetting mutes", null, this.getName(), e);
                                } catch (PunishmentsDatabaseException pde) {
                                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                                    errorHandler.log(pde);
                                    errorHandler.alert(pde, commandSender);
                                    return;
                                }
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
                    plugin.onDisable();
                    plugin.onEnable();
                    player.sendMessage(new ComponentBuilder(plugin.prefix).append("Plugin Reloaded!").color(ChatColor.RED).create());
                    AdminChat.sendMessage(player.getName() + " has reloaded the plugin!");
                    plugin.getProxy().getScheduler().runAsync(plugin, this::reloadRecovery);
                    BungeeMain.Logs.warning(player.getName() + " reloaded the plugin ");
                } else if (strings[0].equalsIgnoreCase("testsql")){
                    plugin.testConnectionManual();
                    BungeeMain.Logs.warning(player.getName() + " tested sql connection");
                }else if (strings[0].equalsIgnoreCase("help")){
                    plugin.getProxy().getPluginManager().dispatchCommand(commandSender, "punisherhelp");
                }else if (strings[0].equalsIgnoreCase("reseteverything")){
                    AdminChat.sendMessage(player.getName() + " has initiated a full reset of everything, please wait....");
                    BungeeMain.Logs.warning(player.getName() + " has initiated a full reset of everything!");
                    AdminChat.sendMessage("Plugin command and event listeners will be unloaded during this process!");
                    plugin.getProxy().getPluginManager().unregisterCommands(plugin);
                    plugin.getProxy().getPluginManager().unregisterListeners(plugin);
                    try{
                        String sql1 = "DROP DATABASE `" + BungeeMain.database + "`";
                        PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                        stmt1.executeUpdate();
                        stmt1.close();
                    }catch (SQLException e){
                        plugin.getLogger().severe(plugin.prefix + e);
                        sqlfails++;
                        if (sqlfails > 5) {
                            try {
                                throw new PunishmentsDatabaseException("Resetting entire punishment database", null, this.getName(), e);
                            } catch (PunishmentsDatabaseException pde) {
                                ErrorHandler errorHandler = ErrorHandler.getInstance();
                                errorHandler.log(pde);
                                errorHandler.alert(pde, commandSender);
                                AdminChat.sendMessage("An error occurred during the reset process, nothing was reset!");
                                AdminChat.sendMessage("Reloading plugin, please wait...");
                                BungeeMain.Logs.warning("An error occurred during the reset process, nothing was reset!");
                                plugin.onDisable();
                                plugin.onEnable();
                                AdminChat.sendMessage("Plugin reloaded!");
                                return;
                            }
                        }
                        if (plugin.testConnectionManual())
                            this.execute(commandSender, strings);
                    }
                    AdminChat.sendMessage("Reset all mutes!");
                    AdminChat.sendMessage("Reset all bans!");
                    AdminChat.sendMessage("Reset all stored ips!");
                    AdminChat.sendMessage("Reset all player history!");
                    AdminChat.sendMessage("Reset all staff history!");
                    plugin.onDisable();
                    BungeeMain.Punishments.delete();
                    AdminChat.sendMessage("Reset all punishments!");
                    AdminChat.sendMessage("This means automatic punishments will use default values!");
                    BungeeMain.Reputation.delete();
                    AdminChat.sendMessage("Reset all reputation scores!");
                    BungeeMain.PlayerInfo.delete();
                    AdminChat.sendMessage("Reset all stored playerinfo!");
                    if (BungeeMain.PunisherConfig.getBoolean("DiscordIntegration.Enabled")) {
                        BungeeMain.DiscordIntegration.delete();
                        DiscordMain.verifiedUsers.clear();
                        DiscordMain.userCodes.clear();
                        AdminChat.sendMessage("Reset all linked discord users!");
                    }
                    AdminChat.sendMessage("");
                    AdminChat.sendMessage("Reloading plugin please wait....");
                    AdminChat.sendMessage("");
                    plugin.onEnable();
                    AdminChat.sendMessage("Reloaded the plugin!");
                    AdminChat.sendMessage("Warning, plugin is in an out of the box state and will need to be configured properly!");
                    AdminChat.sendMessage("Collecting required info on online players in order to solve errors..");
                    ServerConnect.lastJoinId = 0;
                    for (ProxiedPlayer all : ProxyServer.getInstance().getPlayers()){
                        BungeeMain.RepStorage.set(all.getUniqueId().toString().replace("-", ""), 5.0);
                        BungeeMain.InfoConfig.set(all.getUniqueId().toString().replace("-", "") + ".lastlogin", System.currentTimeMillis());
                        BungeeMain.InfoConfig.set(all.getUniqueId().toString().replace("-", "") + ".lastserver", all.getServer().getInfo().getName());
                        BungeeMain.InfoConfig.set("lastjoinid", (ServerConnect.lastJoinId + 1));
                        BungeeMain.InfoConfig.set(String.valueOf((ServerConnect.lastJoinId + 1)), player.getUniqueId().toString().replace("-", ""));
                        ServerConnect.lastJoinId++;
                        BungeeMain.saveInfo();
                        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
                            String fetcheduuid = all.getUniqueId().toString().replace("-", "");
                            String targetName = all.getName();
                            try {
                                //update ip and make sure there is one in the database
                                String sqlip = "SELECT * FROM `altlist` WHERE UUID='" + fetcheduuid + "'";
                                PreparedStatement stmtip = plugin.connection.prepareStatement(sqlip);
                                ResultSet resultsip = stmtip.executeQuery();
                                if (resultsip.next()) {
                                    if (!resultsip.getString("ip").equals(all.getAddress().getHostString())) {
                                        String oldip = resultsip.getString("ip");
                                        String sqlipadd = "UPDATE `altlist` SET `ip`='" + all.getAddress().getAddress().getHostAddress() + "' WHERE `ip`='" + oldip + "' ;";
                                        PreparedStatement stmtipadd = plugin.connection.prepareStatement(sqlipadd);
                                        stmtipadd.executeUpdate();
                                        stmtipadd.close();
                                    }
                                } else {
                                    String sql1 = "INSERT INTO `altlist` (`UUID`, `ip`) VALUES ('" + fetcheduuid + "', '" + all.getAddress().getAddress().getHostAddress() + "');";
                                    PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                                    stmt1.executeUpdate();
                                    stmt1.close();
                                }
                                stmtip.close();
                                resultsip.close();
                            } catch (SQLException sqle) {
                                try {
                                    throw new PunishmentsDatabaseException("Updating ip in altlist", targetName, PlayerLogin.class.getName(), sqle);
                                } catch (PunishmentsDatabaseException pde) {
                                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                                    errorHandler.log(pde);
                                    errorHandler.adminChatAlert(sqle, all);
                                }
                            }
                            try {
                                //update ip hist
                                String sql = "SELECT * FROM `iphist` WHERE UUID='" + fetcheduuid + "'";
                                PreparedStatement stmt = plugin.connection.prepareStatement(sql);
                                ResultSet results = stmt.executeQuery();
                                if (results.next()) {
                                    if (!results.getString("ip").equals(all.getAddress().getHostString())) {
                                        String addip = "INSERT INTO `iphist` (`UUID`, `date`, `ip`) VALUES ('" + fetcheduuid + "', '" + System.currentTimeMillis() + "', '" + all.getAddress().getAddress().getHostAddress() + "');";
                                        PreparedStatement addipstmt = plugin.connection.prepareStatement(addip);
                                        addipstmt.executeUpdate();
                                        addipstmt.close();
                                    }
                                } else {
                                    String addip = "INSERT INTO `iphist` (`UUID`, `date`, `ip`) VALUES ('" + fetcheduuid + "', '" + System.currentTimeMillis() + "', '" + all.getAddress().getAddress().getHostAddress() + "');";
                                    PreparedStatement addipstmt = plugin.connection.prepareStatement(addip);
                                    addipstmt.executeUpdate();
                                    addipstmt.close();
                                }
                            } catch (SQLException sqle) {
                                try {
                                    throw new PunishmentsDatabaseException("Updating ip in iphist", targetName, PlayerLogin.class.getName(), sqle);
                                } catch (PunishmentsDatabaseException pde) {
                                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                                    errorHandler.log(pde);
                                    errorHandler.adminChatAlert(sqle, all);
                                }
                            }
                        });
                    }
                    AdminChat.sendMessage("Required info collected!");
                    AdminChat.sendMessage("");
                    AdminChat.sendMessage("Plugin successfully reset!");
                    AdminChat.sendMessage("");
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

    private void reloadRecovery() {
        punisher.resetCache();
        for (ProxiedPlayer players : plugin.getProxy().getPlayers()) {
            if (punisher.isBanned(players.getUniqueId().toString().replace("-", ""))) {
                Punishment ban = punisher.getBan(players.getUniqueId().toString().replace("-", ""));
                long banleftmillis = ban.getDuration() - System.currentTimeMillis();
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
    }
}