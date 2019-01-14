package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class IpCommand extends Command {
    public IpCommand() {
        super("ip", "punisher.alts.ip", "address");
    }

    private BungeeMain plugin = BungeeMain.getInstance();

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length <= 0){
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Please provide a player's name!").color(ChatColor.RED).create());
            return;
        }
        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(strings[0]);
        if (target == null){
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("That is not a player's name!").color(ChatColor.RED).create());
            return;
        }
        commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append(target.getName() + "'s ip address is: " + target.getAddress().getHostString()).color(ChatColor.RED).create());
    }
}
