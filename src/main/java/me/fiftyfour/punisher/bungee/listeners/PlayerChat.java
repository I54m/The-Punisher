package me.fiftyfour.punisher.bungee.listeners;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.chats.StaffChat;
import me.fiftyfour.punisher.bungee.handlers.ErrorHandler;
import me.fiftyfour.punisher.bungee.managers.PunishmentManager;
import me.fiftyfour.punisher.bungee.objects.Punishment;
import me.fiftyfour.punisher.universal.exceptions.PunishmentsDatabaseException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerChat implements Listener {
    private BungeeMain plugin = BungeeMain.getInstance();
    private HashMap<ProxiedPlayer, Integer> sqlfails = new HashMap<>();
    private PunishmentManager punishmentManager = PunishmentManager.getInstance();

    @EventHandler
    public void onChat(ChatEvent event) {
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        int sqlfails;
        if (this.sqlfails.get(player) == null)
            sqlfails = 0;
        else
            sqlfails = this.sqlfails.get(player);
        ServerInfo server = player.getServer().getInfo();
        if (PluginMessage.chatOffServers.contains(server)){
            if(!player.hasPermission("punisher.togglechat.bypass")){
                if (event.isCommand()){
                    String[] args = event.getMessage().split(" ");
                    List<String> mutedcommands;
                    mutedcommands = BungeeMain.PunisherConfig.getStringList("Muted Commands");
                    if (mutedcommands.contains(args[0])) {
                        event.setCancelled(true);
                        player.sendMessage(new ComponentBuilder(plugin.prefix).append("You may not use that command at this time!").color(ChatColor.RED).create());
                    }else{
                        event.setCancelled(false);
                    }
                    return;
                }
                event.setCancelled(true);
                player.sendMessage(new ComponentBuilder(plugin.prefix).append("You may not chat at this time!").color(ChatColor.RED).create());
                return;
            }else {
                event.setCancelled(false);
                return;
            }
        }
        UUID uuid = player.getUniqueId();
        String fetcheduuid = uuid.toString().replace("-", "");
        String targetname = player.getName();
        try {
            if (punishmentManager.isMuted(fetcheduuid)) {
                Punishment mute = punishmentManager.getMute(fetcheduuid);
                if (player.hasPermission("punisher.bypass")) {
                    punishmentManager.revoke(mute, null, targetname, true, false);
                    StaffChat.sendMessage(player.getName() + " Bypassed their mute, Unmuting...");
                    return;
                }
                long mutetime = mute.getDuration();
                long muteleftmillis = mutetime - System.currentTimeMillis();
                int daysleft = (int) (muteleftmillis / (1000 * 60 * 60 * 24));
                int hoursleft = (int) (muteleftmillis / (1000 * 60 * 60) % 24);
                int minutesleft = (int) (muteleftmillis / (1000 * 60) % 60);
                int secondsleft = (int) (muteleftmillis / 1000 % 60);
                if (System.currentTimeMillis() > mutetime) {
                    punishmentManager.revoke(mute, null, targetname, false, false);
                    player.sendMessage(new ComponentBuilder(plugin.prefix).append("Your Mute has expired!").color(ChatColor.GREEN).create());
                    BungeeMain.Logs.info(player.getName() + "'s mute expired so was unmuted");
                } else {
                    if (event.isCommand()) {
                        String[] args = event.getMessage().split(" ");
                        List<String> mutedcommands;
                        mutedcommands = BungeeMain.PunisherConfig.getStringList("Muted Commands");
                        if (!mutedcommands.contains(args[0])) {
                            event.setCancelled(true);
                            String muteMessage;
                            if (daysleft > 730) {
                                muteMessage = BungeeMain.PunisherConfig.getString("PermMute Deny Message").replace("%reason%", mute.getMessage())
                                        .replace("%days%", String.valueOf(daysleft)).replace("%hours%", String.valueOf(hoursleft))
                                        .replace("%minutes%", String.valueOf(minutesleft)).replace("%seconds%", String.valueOf(secondsleft));
                            } else {
                                muteMessage = BungeeMain.PunisherConfig.getString("TempMute Deny Message").replace("%reason%", mute.getMessage())
                                        .replace("%days%", String.valueOf(daysleft)).replace("%hours%", String.valueOf(hoursleft))
                                        .replace("%minutes%", String.valueOf(minutesleft)).replace("%seconds%", String.valueOf(secondsleft));
                            }
                            player.sendMessage(new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', muteMessage)).create());
                        }
                    }else {
                        event.setCancelled(true);
                        String muteMessage;
                        if (daysleft > 730) {
                            muteMessage = BungeeMain.PunisherConfig.getString("PermMute Deny Message").replace("%reason%", mute.getMessage())
                                    .replace("%days%", String.valueOf(daysleft)).replace("%hours%", String.valueOf(hoursleft))
                                    .replace("%minutes%", String.valueOf(minutesleft)).replace("%seconds%", String.valueOf(secondsleft));
                        } else {
                            muteMessage = BungeeMain.PunisherConfig.getString("TempMute Deny Message").replace("%reason%", mute.getMessage())
                                    .replace("%days%", String.valueOf(daysleft)).replace("%hours%", String.valueOf(hoursleft))
                                    .replace("%minutes%", String.valueOf(minutesleft)).replace("%seconds%", String.valueOf(secondsleft));
                        }
                        player.sendMessage(new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', muteMessage)).create());
                        if (BungeeMain.PunisherConfig.getBoolean("SendPlayersMessageToStaffChatOnMuteDeny"))
                            StaffChat.sendMessage(player.getName() + " Tried to speak but is muted: " + event.getMessage());
                        else if (BungeeMain.PunisherConfig.getBoolean("StaffChatOnMuteDeny"))
                            StaffChat.sendMessage(player.getName() + " Tried to speak but is muted!");
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe(plugin.prefix + e);
            sqlfails++;
            this.sqlfails.put(player, sqlfails);
            if (sqlfails > 5) {
                try {
                    throw new PunishmentsDatabaseException("Issuing ban on a player", targetname, this.getClass().getName(), e);
                } catch (PunishmentsDatabaseException pde) {
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.log(pde);
                    errorHandler.alert(pde, player);
                    errorHandler.adminChatAlert(pde, player);
                    event.setCancelled(true);
                    return;
                }
            }
            if(plugin.testConnectionManual())
                this.onChat(new ChatEvent(event.getSender(), event.getReceiver(), event.getMessage()));
        }
    }
}