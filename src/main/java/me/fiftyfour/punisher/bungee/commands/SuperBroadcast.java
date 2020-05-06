package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.PunisherPlugin;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class SuperBroadcast extends Command {
    public SuperBroadcast() {
        super("superbroadcast", "punisher.superbroadcast", "sb", "alert");
    }

    private final PunisherPlugin plugin = PunisherPlugin.getInstance();

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage(new TextComponent("You must be a player to use this command!"));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) commandSender;
        if (strings.length == 0) {
            player.sendMessage(new ComponentBuilder(plugin.prefix).append("Send a message to all servers").color(ChatColor.RED).append("\nUsage: /superbroadcast <message>").color(ChatColor.WHITE).create());
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String message : strings)
            sb.append(message).append(" ");
        ProxyServer.getInstance().broadcast(new TextComponent("\n"));
        ProxyServer.getInstance().broadcast(new ComponentBuilder("Announcement: ").color(ChatColor.AQUA).bold(true).append(sb.toString()).color(ChatColor.WHITE).create());
        ProxyServer.getInstance().broadcast(new TextComponent("\n"));
        for (ProxiedPlayer all : ProxyServer.getInstance().getPlayers()) {
            ProxyServer.getInstance().createTitle().title().subTitle(new TextComponent(ChatColor.AQUA + "An Announcement has been made in chat!")).fadeIn(15).stay(75).fadeOut(15).send(all);
        }
    }
}