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

public class ReputationCommand extends Command {

    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;

    public ReputationCommand() {
        super("reputation", "punisher.reputation", "rep");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage(new TextComponent("You must be a player to use this command!"));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) commandSender;
        if (strings.length <= 2) {
            player.sendMessage(new ComponentBuilder(prefix).append("Add/Minus/Set a player's reputation score").color(ChatColor.RED).append("\nUsage: /reputation <player> <add|minus|set> <amount>").color(ChatColor.WHITE).create());
            return;
        }
        String uuid = UUIDFetcher.getUUID(strings[0]);
        String name = NameFetcher.getName(uuid);
        if (uuid.equals("null")) {
            player.sendMessage(new ComponentBuilder(prefix).append("That is not a player's name!").color(ChatColor.RED).create());
            return;
        }
        if (strings[1].equalsIgnoreCase("add")) {
            try {
                double amount = Double.parseDouble(strings[2]);
                ReputationSystem.addRep(name, uuid, amount);
                String currentRep = ReputationSystem.getRep(uuid);
                player.sendMessage(new ComponentBuilder(prefix).append("Added: " + amount + " to: " + name + "'s reputation").color(ChatColor.RED).create());
                player.sendMessage(new ComponentBuilder(prefix).append("New Reputation: " + currentRep).color(ChatColor.RED).create());
            }catch (NumberFormatException e){
                player.sendMessage(new ComponentBuilder(prefix).append("That is not a valid amount!").color(ChatColor.RED).create());
                player.sendMessage(new ComponentBuilder(prefix).append("Add/Minus/Set a player's reputation score").color(ChatColor.RED).append("\nUsage: /reputation <player> <add|minus|set> <amount>").color(ChatColor.WHITE).create());
            }
        } else if (strings[1].equalsIgnoreCase("minus")) {
            try {
                double amount = Double.parseDouble(strings[2]);
                ReputationSystem.minusRep(name, uuid, amount);
                String currentRep = ReputationSystem.getRep(uuid);
                player.sendMessage(new ComponentBuilder(prefix).append("Removed: " + amount + " from: " + name + "'s reputation").color(ChatColor.RED).create());
                player.sendMessage(new ComponentBuilder(prefix).append("New Reputation: " + currentRep).color(ChatColor.RED).create());
            }catch (NumberFormatException e){
                player.sendMessage(new ComponentBuilder(prefix).append("That is not a valid amount!").color(ChatColor.RED).create());
                player.sendMessage(new ComponentBuilder(prefix).append("Add/Minus/Set a player's reputation score").color(ChatColor.RED).append("\nUsage: /reputation <player> <add|minus|set> <amount>").color(ChatColor.WHITE).create());
            }
        } else if (strings[1].equalsIgnoreCase("set")) {
            try {
                double amount = Double.parseDouble(strings[2]);
                ReputationSystem.setRep(name, uuid, amount);
                player.sendMessage(new ComponentBuilder(prefix).append("set: " + name + "'s reputation to: " + amount).color(ChatColor.RED).create());
                player.sendMessage(new ComponentBuilder(prefix).append("New Reputation: " + amount).color(ChatColor.RED).create());
            } catch (NumberFormatException e) {
                player.sendMessage(new ComponentBuilder(prefix).append("That is not a valid amount!").color(ChatColor.RED).create());
                player.sendMessage(new ComponentBuilder(prefix).append("Add/Minus/Set a player's reputation score").color(ChatColor.RED).append("\nUsage: /reputation <player> <add|minus|set> <amount>").color(ChatColor.WHITE).create());
            }
        } else {
            player.sendMessage(new ComponentBuilder(prefix).append("Add/Minus/Set a player's reputation score").color(ChatColor.RED).append("\nUsage: /reputation <player> <add|minus|set> <amount>").color(ChatColor.WHITE).create());
        }
    }

}
