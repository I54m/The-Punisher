package me.fiftyfour.punisher.bungee.listeners;

import com.vexsoftware.votifier.bungee.events.VotifierEvent;
import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.handlers.ErrorHandler;
import me.fiftyfour.punisher.bungee.systems.ReputationSystem;
import me.fiftyfour.punisher.universal.exceptions.DataFecthException;
import me.fiftyfour.punisher.universal.fetchers.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class PlayerVote implements Listener {

    private BungeeMain plugin = BungeeMain.getInstance();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVote(VotifierEvent event){
        com.vexsoftware.votifier.model.Vote vote = event.getVote();
        String username = vote.getUsername();
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(username);
        if (player != null){
            ReputationSystem.addRep(username, player.getUniqueId().toString().replace("-", ""), BungeeMain.PunisherConfig.getDouble("Voting.amountOfRepToAdd"));
            player.sendMessage(new ComponentBuilder(plugin.prefix).append("Thanks for Voting, " + BungeeMain.PunisherConfig.getDouble("Voting.amountOfRepToAdd") + " Reputation added!").color(ChatColor.GREEN).create());
        }else{
            UUIDFetcher uuidFetcher = new UUIDFetcher();
            uuidFetcher.fetch(username);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<String> future = executorService.submit(uuidFetcher);
            try {
                ReputationSystem.addRep(username, future.get(10, TimeUnit.SECONDS), BungeeMain.PunisherConfig.getDouble("Voting.amountOfRepToAdd"));
            } catch (Exception e) {
                try {
                    throw new DataFecthException("UUID Required for next step", username, "UUID", PlayerVote.class.getName(), e);
                } catch (DataFecthException dfe) {
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.log(dfe);
                    errorHandler.adminChatAlert(dfe, plugin.getProxy().getConsole());
                }
                executorService.shutdown();
                return;
            }
            executorService.shutdown();
        }
    }
}
