package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class GlobalCommand extends Command {
    public GlobalCommand() {
        super("broadcast", "punisher.broadcast", "global");
    }

    private BungeeMain plugin = BungeeMain.getInstance();

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage(new TextComponent("You must be a player to use this command!"));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) commandSender;
        if (BungeeMain.CooldownsConfig.contains(player.getUniqueId().toString()) ){
            long cooldowntime = BungeeMain.CooldownsConfig.getLong(player.getUniqueId().toString());
            if (cooldowntime > System.currentTimeMillis() && !player.hasPermission("punisher.cooldowns.override")) {
                Long cooldownleftmillis = cooldowntime - System.currentTimeMillis();
                int hoursleft = (int) (cooldownleftmillis / (1000 * 60 * 60));
                int minutesleft = (int) (cooldownleftmillis / (1000 * 60) % 60);
                int secondsleft = (int) (cooldownleftmillis / 1000 % 60);
                if(hoursleft > 0){
                    player.sendMessage(new ComponentBuilder("You have done that recently, please wait: " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s before doing this again!").color(ChatColor.RED).create());
                    return;
                }else{
                    if(minutesleft > 0) {
                        player.sendMessage(new ComponentBuilder("You have done that recently, please wait: " + minutesleft + "m " + secondsleft + "s before doing this again!").color(ChatColor.RED).create());
                        return;
                    }else{
                        player.sendMessage(new ComponentBuilder("You have done that recently, please wait: " + secondsleft + "s before doing this again!").color(ChatColor.RED).create());
                        return;
                    }
                }
            }else{
                if (strings.length == 0) {
                    player.sendMessage(new ComponentBuilder(plugin.prefix).append("Similar to Super Broadcast but not as powerful").color(ChatColor.RED).append("\nUsage: /global <message>").color(ChatColor.WHITE).create());
                    return;
                }
                StringBuilder sb = new StringBuilder();
                for (String message : strings)
                    sb.append(message).append(" ");
                ProxyServer.getInstance().broadcast(new ComponentBuilder("[").color(ChatColor.DARK_PURPLE).append(player.getServer().getInfo().getName()).color(ChatColor.LIGHT_PURPLE).bold(true).append("] ").color(ChatColor.DARK_PURPLE).bold(false)
                        .append(player.getName()).color(ChatColor.WHITE).bold(true).append(" » ").color(ChatColor.WHITE).bold(true).append(sb.toString()).color(ChatColor.LIGHT_PURPLE).bold(false).create());
                long setcooldowntime = BungeeMain.PunisherConfig.getLong("/global Cooldown");
                setcooldowntime = setcooldowntime * 60 * 60 * 1000;
                setcooldowntime += System.currentTimeMillis();
                BungeeMain.CooldownsConfig.set(player.getUniqueId().toString(), setcooldowntime);
                BungeeMain.saveCooldowns();
                return;
            }
        }
        if (strings.length == 0) {
            player.sendMessage(new ComponentBuilder(plugin.prefix).append("Similar to Super Broadcast but not as powerful").color(ChatColor.RED).append("\nUsage: /global <message>").color(ChatColor.WHITE).create());
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String message : strings)
            sb.append(message).append(" ");
        ProxyServer.getInstance().broadcast(new ComponentBuilder("[").color(ChatColor.DARK_PURPLE).append(player.getServer().getInfo().getName()).color(ChatColor.LIGHT_PURPLE).bold(true).append("] ").color(ChatColor.DARK_PURPLE).bold(false)
                .append(player.getName()).color(ChatColor.WHITE).bold(true).append(" » ").color(ChatColor.WHITE).bold(true).append(sb.toString()).color(ChatColor.LIGHT_PURPLE).bold(false).create());
        long setcooldowntime = BungeeMain.PunisherConfig.getLong("/global Cooldown");
        setcooldowntime = setcooldowntime * 60 * 60 * 1000;
        setcooldowntime += System.currentTimeMillis();
        BungeeMain.CooldownsConfig.set(player.getUniqueId().toString(), setcooldowntime);
        BungeeMain.saveCooldowns();
    }
}
