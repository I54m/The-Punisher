package me.fiftyfour.punisher.bungee.fetchers;

import me.fiftyfour.punisher.bungee.managers.PunishmentManager;
import me.fiftyfour.punisher.universal.fetchers.NameFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.concurrent.Callable;

public class Status implements Callable<TextComponent> {

    private String targetuuid;
    private PunishmentManager punishMnger = PunishmentManager.getInstance();

    public void setTargetuuid(String targetuuid) {
        this.targetuuid = targetuuid;
    }

    @Override
    public TextComponent call() {
        TextComponent status;
        if (punishMnger.isBanned(targetuuid)) {
            long bantime = punishMnger.getBan(targetuuid).getDuration();
            long banleftmillis = bantime - System.currentTimeMillis();
            int daysleft = (int) (banleftmillis / (1000 * 60 * 60 * 24));
            int hoursleft = (int) (banleftmillis / (1000 * 60 * 60) % 24);
            int minutesleft = (int) (banleftmillis / (1000 * 60) % 60);
            int secondsleft = (int) (banleftmillis / 1000 % 60);
            String reason = punishMnger.getBan(targetuuid).getMessage();
            if (reason == null)
                reason = punishMnger.getBan(targetuuid).getReason().toString().replace("_", " ");
            String punisher = NameFetcher.getName(punishMnger.getBan(targetuuid).getPunisherUUID());
            if (secondsleft <= 0){
                status = new TextComponent("No currently active punishments!");
                status.setColor(ChatColor.GREEN);
            }else {
                status = new TextComponent("Banned for " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s. Reason: " + reason + " by: " + punisher);
                status.setColor(ChatColor.RED);
            }
        } else if (punishMnger.isMuted(targetuuid)) {
            long mutetime = punishMnger.getMute(targetuuid).getDuration();
            long muteleftmillis = mutetime - System.currentTimeMillis();
            int daysleft = (int) (muteleftmillis / (1000 * 60 * 60 * 24));
            int hoursleft = (int) (muteleftmillis / (1000 * 60 * 60) % 24);
            int minutesleft = (int) (muteleftmillis / (1000 * 60) % 60);
            int secondsleft = (int) (muteleftmillis / 1000 % 60);
            String reason = punishMnger.getMute(targetuuid).getMessage();
            if (reason == null)
                reason = punishMnger.getMute(targetuuid).getReason().toString().replace("_", " ");
            String punisher = NameFetcher.getName(punishMnger.getMute(targetuuid).getPunisherUUID());
            if (secondsleft <= 0){
                status = new TextComponent("No currently active punishments!");
                status.setColor(ChatColor.GREEN);
            }else {
                status = new TextComponent("Muted for " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s. Reason: " + reason + " by: " + punisher);
                status.setColor(ChatColor.RED);
            }
        } else {
            status = new TextComponent("No currently active punishments!");
            status.setColor(ChatColor.GREEN);
        }
        return status;
    }
}
