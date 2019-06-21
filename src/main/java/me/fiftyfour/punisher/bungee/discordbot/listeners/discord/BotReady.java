package me.fiftyfour.punisher.bungee.discordbot.listeners.discord;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.discordbot.DiscordMain;
import me.fiftyfour.punisher.bungee.listeners.ServerConnect;
import me.fiftyfour.punisher.bungee.managers.PunishmentManager;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.md_5.bungee.api.ProxyServer;

import java.util.concurrent.TimeUnit;

public class BotReady extends ListenerAdapter {

    private BungeeMain plugin = BungeeMain.getInstance();
    private PunishmentManager punishMngr = PunishmentManager.getInstance();
    private int status = 1;

    @Override
    public void onReady(ReadyEvent event) {
        DiscordMain.updateTasks.add(ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            switch (status) {
                case 1:
                    DiscordMain.jda.getPresence().setGame(Game.watching(punishMngr.totalBans() + " Bans!"));
                    status++;
                    return;
                case 2:
                    DiscordMain.jda.getPresence().setGame(Game.listening(punishMngr.totalMutes() + " Mutes!"));
                    status++;
                    return;
                case 3:
                    DiscordMain.jda.getPresence().setGame(Game.playing(BungeeMain.PunisherConfig.getString("DiscordIntegration.Playing")));
                    status++;
                    return;
                case 4:
                    DiscordMain.jda.getPresence().setGame(Game.watching(DiscordMain.verifiedUsers.size() + " Linked Users!"));
                    status++;
                    return;
                case 5:
                    DiscordMain.jda.getPresence().setGame(Game.watching(ServerConnect.lastJoinId + " Total Joins!"));
                    status++;
                    return;
                case 6:
                    DiscordMain.jda.getPresence().setGame(Game.watching("For Rule Breakers!"));
                    status++;
                    return;
                case 7:
                    DiscordMain.jda.getPresence().setGame(Game.watching("For Punishments!"));
                    status++;
                    return;
                case 8:
                    DiscordMain.jda.getPresence().setGame(Game.playing("With The Ban Hammer!"));
                    status++;
                    return;
                case 9:
                    DiscordMain.jda.getPresence().setGame(Game.listening("54mpenguin"));
                    status ++;
                    return;
                case 10:
                    DiscordMain.jda.getPresence().setGame(Game.playing("v" + plugin.getDescription().getVersion()));
                    status = 1;

            }
        }, 1, 10, TimeUnit.SECONDS));
    }
}
