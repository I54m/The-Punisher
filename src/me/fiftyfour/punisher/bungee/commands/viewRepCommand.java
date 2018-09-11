package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.fetchers.NameFetcher;
import me.fiftyfour.punisher.fetchers.UUIDFetcher;
import me.fiftyfour.punisher.systems.ReputationSystem;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class viewRepCommand extends Command {
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;

    public viewRepCommand() {
        super("viewrep", "punisher.viewrep", "vrep");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            if (strings.length == 0) {
                player.sendMessage(new ComponentBuilder(prefix).append("View a player's reputation score").color(ChatColor.RED).append("\nUsage: /viewrep <player name>").color(ChatColor.WHITE).create());
                return;
            }
            String targetuuid = UUIDFetcher.getUUID(strings[0]);
            if (!targetuuid.equals("null")) {
                String targetname = NameFetcher.getName(targetuuid);
                if (targetname.equalsIgnoreCase("null")) {
                    targetname = strings[0];
                }
                StringBuilder reputation = new StringBuilder();
                String rep = ReputationSystem.getRep(targetuuid);
                if (!(rep == null)) {
                    double repDouble;
                    try {
                        repDouble = Double.parseDouble(rep);
                    }catch(NumberFormatException e){
                        player.sendMessage(new ComponentBuilder(prefix).append("An internal error occurred: That player's reputation is not a number! Reputation System returned: ").color(ChatColor.RED).append(rep).color(ChatColor.RED).create());
                        return;
                    }
                    if (repDouble == 5){
                        reputation.append(ChatColor.WHITE).append("(").append(rep).append("/10").append(")");
                    }else if (repDouble > 5){
                        reputation.append(ChatColor.GREEN).append("(").append(rep).append("/10").append(")");
                    }else if (repDouble < 5 && repDouble > -1){
                        reputation.append(ChatColor.YELLOW).append("(").append(rep).append("/10").append(")");
                    }else if (repDouble < -1 && repDouble > -8){
                        reputation.append(ChatColor.GOLD).append("(").append(rep).append("/10").append(")");
                    }else if (repDouble < -8){
                        reputation.append(ChatColor.RED).append("(").append(rep).append("/10").append(")");
                    }
                } else {
                    reputation.append(ChatColor.WHITE).append("(").append("-").append(ChatColor.WHITE).append(")");
                }
                player.sendMessage(new ComponentBuilder(prefix).append(targetname + "'s Reputation is: ").color(ChatColor.RED).append(reputation.toString()).color(ChatColor.RED).create());
            } else {
                player.sendMessage(new ComponentBuilder(prefix).append("That is not a player's name").color(ChatColor.RED).create());
            }
        } else {
            commandSender.sendMessage(new TextComponent("You must be a player to use this command!"));
        }
    }

}
