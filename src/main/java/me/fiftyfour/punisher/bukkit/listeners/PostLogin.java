package me.fiftyfour.punisher.bukkit.listeners;

import me.fiftyfour.punisher.bukkit.BukkitMain;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PostLogin implements Listener {

    @EventHandler
    public void onPostLogin(PlayerLoginEvent event){
       BukkitMain.updateRepCache();
    }
}
