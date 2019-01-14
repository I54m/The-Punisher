package me.fiftyfour.punisher.bungee.discordbot;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.discordbot.commands.DiscordCommand;
import me.fiftyfour.punisher.bungee.discordbot.listeners.*;
import me.fiftyfour.punisher.bungee.managers.PunishmentManager;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.yaml.snakeyaml.Yaml;

import javax.security.auth.login.LoginException;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DiscordMain {

    private static BungeeMain plugin = BungeeMain.getInstance();
    public static JDA jda;
    public static Map<UUID, String> userCodes = new HashMap<>();
    public static Map<UUID, String> verifiedUsers = new HashMap<>();
    private static final Yaml YAML_LOADER = new Yaml();
    private static List<ScheduledTask> updateTasks = new ArrayList<>();
    private static PunishmentManager punishMngr = PunishmentManager.getInstance();

    public static void startBot(){
        plugin.getLogger().info(plugin.prefix + ChatColor.GREEN + "Starting Discord bot...");
        try {
            jda = new JDABuilder(AccountType.BOT).setToken(BungeeMain.PunisherConfig.getString("DiscordIntegration.BotToken")).build();
            jda.addEventListener(new BotReady());
            jda.addEventListener(new PrivateMessageReceived());
            ProxyServer.getInstance().getPluginManager().registerCommand(plugin, new DiscordCommand());
            if (BungeeMain.PunisherConfig.getBoolean("DiscordIntegration.EnableJoinLogging")) {
                ProxyServer.getInstance().getPluginManager().registerListener(plugin, new ServerConnected());
                ProxyServer.getInstance().getPluginManager().registerListener(plugin, new PlayerDisconnect());
                ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                    TextChannel loggingChannel = DiscordMain.jda.getTextChannelById(BungeeMain.PunisherConfig.getString("DiscordIntegration.JoinLoggingChannelId"));
                    loggingChannel.getManager().setTopic(ProxyServer.getInstance().getPlayers().size() + " players online | "
                            + me.fiftyfour.punisher.bungee.listeners.ServerConnect.lastJoinId + " unique players ever joined").queue();
                }, 10, 5, TimeUnit.SECONDS);
            }
            if (BungeeMain.PunisherConfig.getBoolean("DiscordIntegration.EnableRoleSync")){
                updateTasks.add(ProxyServer.getInstance().getScheduler().schedule(plugin, () ->
                    ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
                        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                            if (verifiedUsers.containsKey(player.getUniqueId())) {
                                ArrayList<Role> rolestoadd = new ArrayList<>();
                                ArrayList<Role> rolestoremove = new ArrayList<>();
                                User user = jda.getUserById(verifiedUsers.get(player.getUniqueId()));
                                for (String roleids : BungeeMain.PunisherConfig.getStringList("DiscordIntegration.RolesToSync")) {
                                    user.getMutualGuilds().forEach((guild -> guild.getMember(user).getRoles().forEach((role -> {
                                        if (roleids.equals(role.getId()) && !player.hasPermission("punisher.discord.role." + roleids)) {
                                            rolestoremove.add(role);
                                        }
                                    }))));
                                    if (player.hasPermission("punisher.discord.role." + roleids)) {
                                        rolestoadd.add(jda.getRoleById(roleids));
                                    }
                                }
                                user.getMutualGuilds().forEach((guild -> guild.getController().addRolesToMember(guild.getMember(user), rolestoadd).queue()));
                                user.getMutualGuilds().forEach((guild -> guild.getController().removeRolesFromMember(guild.getMember(user), rolestoremove).queue()));
                            }
                        }
                    })
                , 10, 30, TimeUnit.SECONDS));
            }
            if (BungeeMain.PunisherConfig.getBoolean("DiscordIntegration.EnableRoleSync") || BungeeMain.PunisherConfig.getBoolean("DiscordIntegration.EnableJoinLogging")){
                ProxyServer.getInstance().getPluginManager().registerListener(plugin, new ServerConnect());
            }
            updateTasks.add(ProxyServer.getInstance().getScheduler().schedule(plugin, () ->
                verifiedUsers.forEach(((uuid, id) -> {
                    User user = jda.getUserById(id);
                    user.getMutualGuilds().forEach((guild -> {
                        List<Role> rolesToAdd = new ArrayList<>();
                        for (String roleids : BungeeMain.PunisherConfig.getStringList("DiscordIntegration.RolesIdsToAddToLinkedUser")){
                            rolesToAdd.add(guild.getRoleById(roleids));
                        }
                        guild.getController().addRolesToMember(guild.getMember(user), rolesToAdd).queue();
                    }));
                }))
            , 1, 1, TimeUnit.MINUTES));
            try {
                Object obj = YAML_LOADER.load(new FileInputStream(BungeeMain.DiscordIntegration));
                if (obj instanceof HashMap)
                    verifiedUsers = (HashMap<UUID, String>) obj;
            }catch (IOException ioe){
                ioe.printStackTrace();
            }
            plugin.getLogger().info(plugin.prefix + ChatColor.GREEN + "Discord bot Started!");
        } catch (LoginException e) {
            plugin.getLogger().severe(plugin.prefix + ChatColor.RED + "Could not start Discord bot!");
            e.printStackTrace();
        }
    }

    public static void shutdown(){
        if (jda != null){
            plugin.getLogger().info(plugin.prefix + ChatColor.GREEN + "Shutting down Discord bot...");
            try {
                YAML_LOADER.dump(verifiedUsers, new FileWriter(BungeeMain.DiscordIntegration));
            }catch (IOException ioe){
                ioe.printStackTrace();
            }
            jda.shutdownNow();
            jda = null;
            for (ScheduledTask task : updateTasks)
                task.cancel();
            ProxyServer.getInstance().getPluginManager().unregisterCommand(new DiscordCommand());
            plugin.getLogger().info(plugin.prefix + ChatColor.GREEN + "Discord bot Shut down!");
        }
    }
}
