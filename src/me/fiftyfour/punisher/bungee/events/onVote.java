package me.fiftyfour.punisher.bungee.events;

import com.vexsoftware.votifier.bungee.events.VotifierEvent;
import com.vexsoftware.votifier.model.Vote;
import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.fetchers.UUIDFetcher;
import me.fiftyfour.punisher.systems.ReputationSystem;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.concurrent.*;


public class onVote implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerVote(VotifierEvent event){
        Vote vote = event.getVote();
        String username = vote.getUsername();
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(username);
        if (player != null && player.isConnected()){
            ReputationSystem.addRep(username, player.getUniqueId().toString().replace("-", ""), BungeeMain.PunisherConfig.getDouble("Voting.amountOfRepToGive"));
        }else{
            UUIDFetcher uuidFetcher = new UUIDFetcher();
            uuidFetcher.fetch(username);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<String> future = executorService.submit(uuidFetcher);
            try {
                ReputationSystem.addRep(username, future.get(10, TimeUnit.SECONDS), BungeeMain.PunisherConfig.getDouble("Voting.amountOfRepToGive"));
            }catch (TimeoutException te) {
                BungeeMain.Logs.severe("ERROR: Connection to mojang API took too long! Unable to fetch " + username + "'s uuid!");
                BungeeMain.Logs.severe("Error message: " + te.getMessage());
                StringBuilder stacktrace = new StringBuilder();
                for (StackTraceElement stackTraceElement : te.getStackTrace()) {
                    stacktrace.append(stackTraceElement.toString()).append("\n");
                }
                BungeeMain.Logs.severe("Stack Trace: " + stacktrace.toString());
                executorService.shutdown();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                BungeeMain.Logs.severe("ERROR: Unexpected error while trying to add rep after vote, Unable to fetch " + username + "'s uuid");
                BungeeMain.Logs.severe("Error message: " + e.getMessage());
                StringBuilder stacktrace = new StringBuilder();
                for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                    stacktrace.append(stackTraceElement.toString()).append("\n");
                }
                BungeeMain.Logs.severe("Stack Trace: " + stacktrace.toString());
                executorService.shutdown();
                return;
            }
            executorService.shutdown();
        }
    }
}
