package me.fiftyfour.punisher.bungee.discordbot.listeners;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.discordbot.DiscordMain;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;

public class ServerConnect implements Listener {

    private static BungeeMain plugin = BungeeMain.getInstance();

    @EventHandler
    public void onServerConnect(ServerConnectEvent event){
        ProxiedPlayer player = event.getPlayer();
        if (BungeeMain.PunisherConfig.getBoolean("DiscordIntegration.EnableRoleSync") && DiscordMain.verifiedUsers.containsKey(player.getUniqueId()) && event.getReason() == ServerConnectEvent.Reason.JOIN_PROXY){
            ProxyServer.getInstance().getScheduler().runAsync(plugin, () ->{
                ArrayList<Role> rolestoadd = new ArrayList<>();
                ArrayList<Role> rolestoremove = new ArrayList<>();
                User user = DiscordMain.jda.getUserById(DiscordMain.verifiedUsers.get(player.getUniqueId()));
                Guild guild = DiscordMain.jda.getGuildById(BungeeMain.PunisherConfig.getString("DiscordIntegration.GuildId"));
                for (String roleids : BungeeMain.PunisherConfig.getStringList("DiscordIntegration.RolesToSync")){
                    guild.getMember(user).getRoles().forEach((role -> {
                        if (roleids.equals(role.getId()) && !player.hasPermission("punisher.discord.role." + roleids)){
                            rolestoremove.add(role);
                        }
                    }));
                    if (player.hasPermission("punisher.discord.role." + roleids)){
                        rolestoadd.add(DiscordMain.jda.getRoleById(roleids));
                    }
                }
                guild.getController().addRolesToMember(guild.getMember(user), rolestoadd).queue();
                guild.getController().removeRolesFromMember(guild.getMember(user), rolestoremove).queue();
            });
        }
        if (BungeeMain.PunisherConfig.getBoolean("DiscordIntegration.EnableJoinLogging")) {
            TextChannel loggingChannel = DiscordMain.jda.getTextChannelById(BungeeMain.PunisherConfig.getString("DiscordIntegration.JoinLoggingChannelId"));
            switch (event.getReason()) {
                case JOIN_PROXY:
                    loggingChannel.sendMessage(":heavy_plus_sign: " + player.getName() + " **Joined the server!**").queue();
                    return;
                case SERVER_DOWN_REDIRECT:
                    loggingChannel.sendMessage(":x: " + player.getName() + " **Was previously on a server but it went down!**").queue();
                    return;
                case KICK_REDIRECT:
                    loggingChannel.sendMessage(":boot: " + player.getName() + " **Was previously on a server but was kicked!**").queue();
                    return;
                case LOBBY_FALLBACK:
                    loggingChannel.sendMessage(":electric_plug: " + player.getName() + " **Was unable to join the default server and was redirected to a fallback server!**").queue();
                    return;
                case PLUGIN:
                    loggingChannel.sendMessage(":gear: **A plugin caused " + player.getName() + " to be redirected!**").queue();
            }
        }
    }
}
