package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.handlers.ErrorHandler;
import me.fiftyfour.punisher.universal.exceptions.DataFecthException;
import me.fiftyfour.punisher.universal.exceptions.PunishmentsDatabaseException;
import me.fiftyfour.punisher.universal.fetchers.NameFetcher;
import me.fiftyfour.punisher.universal.fetchers.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class IpHistCommand extends Command {

    public IpHistCommand() {
        super("iphist", "punisher.alts.ip", "ihist", "ih");
    }

    private BungeeMain plugin = BungeeMain.getInstance();
    private String targetuuid;
    private int sqlfails;

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            if (strings.length < 1) {
                player.sendMessage(new ComponentBuilder(plugin.prefix).append("Please provide a player's name!").create());
                return;
            }
            ProxiedPlayer findTarget = ProxyServer.getInstance().getPlayer(strings[0]);
            Future<String> future = null;
            ExecutorService executorService = null;
            if (findTarget != null) {
                targetuuid = findTarget.getUniqueId().toString().replace("-", "");
            } else {
                UUIDFetcher uuidFetcher = new UUIDFetcher();
                uuidFetcher.fetch(strings[0]);
                executorService = Executors.newSingleThreadExecutor();
                future = executorService.submit(uuidFetcher);
            }
            if (future != null) {
                try {
                    targetuuid = future.get(10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    try {
                        throw new DataFecthException("UUID Required for next step", strings[0], "UUID", this.getName(), e);
                    }catch (DataFecthException dfe){
                        ErrorHandler errorHandler = ErrorHandler.getInstance();
                        errorHandler.log(dfe);
                        errorHandler.alert(dfe, commandSender);
                    }
                    executorService.shutdown();
                    return;
                }
                executorService.shutdown();
            }
            if (targetuuid == null) {
                player.sendMessage(new ComponentBuilder(plugin.prefix).append("That is not a player's name!").color(ChatColor.RED).create());
                return;
            }
            String targetName = NameFetcher.getName(targetuuid);
            if (targetName == null) {
                targetName = strings[0];
            }
            try {
                String sql = "SELECT * FROM `iphist` WHERE UUID='" + targetuuid + "'";
                PreparedStatement stmt = plugin.connection.prepareStatement(sql);
                ResultSet results = stmt.executeQuery();
                TreeMap<Long, String> iphist = new TreeMap<>();
                while (results.next()) {
                    iphist.put(results.getLong("date"), results.getString("ip"));
                }
                if (iphist.isEmpty()) {
                    player.sendMessage(new ComponentBuilder(plugin.prefix).append(targetName + " does not have any ips stored in the database").color(ChatColor.RED).create());
                    return;
                }
                player.sendMessage(new ComponentBuilder("|-----").strikethrough(true).color(ChatColor.GREEN).append(" " + targetName + "'s Ip hist ")
                        .strikethrough(false).color(ChatColor.RED).append("-----|").strikethrough(true).color(ChatColor.GREEN).create());
                while (!iphist.isEmpty()) {
                    long timeago = (System.currentTimeMillis() - iphist.firstEntry().getKey());
                    String ip = iphist.firstEntry().getValue();
                    int daysago = (int) (timeago / (1000 * 60 * 60 * 24));
                    int hoursago = (int) (timeago / (1000 * 60 * 60) % 24);
                    int minutesago = (int) (timeago / (1000 * 60) % 60);
                    if (minutesago <= 0) minutesago = 1;
                    if (daysago >= 1){
                        player.sendMessage(new ComponentBuilder(ip).color(ChatColor.RED).append(" " + daysago + "d " + hoursago + "h " + minutesago + "m ago").color(ChatColor.GREEN).create());
                    }else if (hoursago >= 1){
                        player.sendMessage(new ComponentBuilder(ip).color(ChatColor.RED).append(" " + hoursago + "h " + minutesago + "m ago").color(ChatColor.GREEN).create());
                    }else {
                        player.sendMessage(new ComponentBuilder(ip).color(ChatColor.RED).append(" " + minutesago + "m ago").color(ChatColor.GREEN).create());
                    }
                    iphist.remove(iphist.firstKey());
                }
            } catch (SQLException sqle) {
                plugin.getLogger().severe(plugin.prefix + sqle);
                sqlfails++;
                if (sqlfails > 5) {
                    try {
                        throw new PunishmentsDatabaseException("Checking ip history", targetName, this.getName(), sqle, "/iphist", strings);
                    } catch (PunishmentsDatabaseException pde) {
                        ErrorHandler errorHandler = ErrorHandler.getInstance();
                        errorHandler.log(pde);
                        errorHandler.alert(pde, commandSender);
                        return;
                    }
                }
                if (plugin.testConnectionManual())
                    this.execute(commandSender, strings);
            }
        } else commandSender.sendMessage(new TextComponent("You must be a player to use this command!"));
    }
}
