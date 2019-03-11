package me.fiftyfour.punisher.bungee.discordbot.listeners;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.discordbot.DiscordMain;
import net.dv8tion.jda.core.entities.TextChannel;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerDisconnect implements Listener {

    private static BungeeMain plugin = BungeeMain.getInstance();

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event){
        if (DiscordMain.jda == null) return;
        ProxiedPlayer player = event.getPlayer();
        if (BungeeMain.PunisherConfig.getBoolean("DiscordIntegration.EnableJoinLogging")) {
            TextChannel loggingChannel = DiscordMain.jda.getTextChannelById(BungeeMain.PunisherConfig.getString("DiscordIntegration.JoinLoggingChannelId"));
            loggingChannel.sendMessage(":heavy_minus_sign: " + player.getName() + " Left the server!").queue();
        }
    }
}
