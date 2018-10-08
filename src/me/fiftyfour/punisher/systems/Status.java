package me.fiftyfour.punisher.systems;

import me.fiftyfour.punisher.bungee.BungeeMain;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Callable;

public class Status implements Callable<TextComponent> {

    private String targetuuid;
    private BungeeMain plugin = BungeeMain.getInstance();

    public void setTargetuuid(String targetuuid) {
        this.targetuuid = targetuuid;
    }

    @Override
    public TextComponent call() throws Exception {
        TextComponent status;
        String sql3 = "SELECT * FROM `mutes` WHERE UUID='" + targetuuid + "'";
        PreparedStatement stmt3 = plugin.connection.prepareStatement(sql3);
        ResultSet results3 = stmt3.executeQuery();
        String sql4 = "SELECT * FROM `bans` WHERE UUID='" + targetuuid + "'";
        PreparedStatement stmt4 = plugin.connection.prepareStatement(sql4);
        ResultSet results4 = stmt4.executeQuery();
        if (results4.next()) {
            Long bantime = results4.getLong("Length");
            Long banleftmillis = bantime - System.currentTimeMillis();
            int daysleft = (int) (banleftmillis / (1000 * 60 * 60 * 24));
            int hoursleft = (int) (banleftmillis / (1000 * 60 * 60) % 24);
            int minutesleft = (int) (banleftmillis / (1000 * 60) % 60);
            int secondsleft = (int) (banleftmillis / 1000 % 60);
            String reason = results4.getString("Reason");
            String punisher = results4.getString("Punisher");
            status = new TextComponent("banned for " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s. Reason: " + reason + " by: " + punisher);
            status.setColor(ChatColor.RED);
        } else if (results3.next()) {
            Long mutetime = results3.getLong("Length");
            Long muteleftmillis = mutetime - System.currentTimeMillis();
            int daysleft = (int) (muteleftmillis / (1000 * 60 * 60 * 24));
            int hoursleft = (int) (muteleftmillis / (1000 * 60 * 60) % 24);
            int minutesleft = (int) (muteleftmillis / (1000 * 60) % 60);
            int secondsleft = (int) (muteleftmillis / 1000 % 60);
            String reason = results3.getString("Reason");
            String punisher = results3.getString("Punisher");
            status = new TextComponent("Muted for " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s. Reason: " + reason + " by: " + punisher);
            status.setColor(ChatColor.RED);
        } else {
            status = new TextComponent("No currently active punishments!");
            status.setColor(ChatColor.GREEN);
        }
        stmt3.close();
        stmt4.close();
        results3.close();
        results4.close();
        return status;
    }
}
