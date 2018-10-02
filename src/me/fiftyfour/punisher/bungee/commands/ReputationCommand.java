package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.fetchers.NameFetcher;
import me.fiftyfour.punisher.fetchers.UUIDFetcher;
import me.fiftyfour.punisher.systems.ReputationSystem;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.concurrent.*;

public class ReputationCommand extends Command {

    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;
    private String uuid;

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
        ProxiedPlayer findTarget = ProxyServer.getInstance().getPlayer(strings[0]);
        Future<String> future = null;
        ExecutorService executorService = null;
        if (findTarget != null){
            uuid = findTarget.getUniqueId().toString().replace("-", "");
        }else {
            UUIDFetcher uuidFetcher = new UUIDFetcher();
            uuidFetcher.fetch(strings[0]);
            executorService = Executors.newSingleThreadExecutor();
            future = executorService.submit(uuidFetcher);
        }
        if (future != null) {
            try {
                uuid = future.get(10, TimeUnit.SECONDS);
            } catch (TimeoutException te) {
                player.sendMessage(new ComponentBuilder(prefix).append("ERROR: ").color(ChatColor.DARK_RED).append("Connection to mojang API took too long! Unable to fetch " + strings[0] + "'s uuid!").color(ChatColor.RED).create());
                player.sendMessage(new ComponentBuilder(prefix).append("This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ").color(ChatColor.RED).create());
                BungeeMain.Logs.severe("ERROR: Connection to mojang API took too long! Unable to fetch " + strings[0] + "'s uuid!");
                BungeeMain.Logs.severe("Error message: " + te.getMessage());
                StringBuilder stacktrace = new StringBuilder();
                for (StackTraceElement stackTraceElement : te.getStackTrace()){
                    stacktrace.append(stackTraceElement.toString()).append("\n");
                }
                BungeeMain.Logs.severe("Stack Trace: " + stacktrace.toString());
                executorService.shutdown();
                return;
            } catch (Exception e) {
                player.sendMessage(new ComponentBuilder(prefix).append("ERROR: ").color(ChatColor.DARK_RED).append("Unexpected error while executing command! Unable to fetch " + strings[0] + "'s uuid!").color(ChatColor.RED).create());
                player.sendMessage(new ComponentBuilder(prefix).append("This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ").color(ChatColor.RED).create());
                BungeeMain.Logs.severe("ERROR: Unexpected error while trying executing command in class: " + this.getName() + " Unable to fetch " + strings[0] + "'s uuid");
                BungeeMain.Logs.severe("Error message: " + e.getMessage());
                StringBuilder stacktrace = new StringBuilder();
                for (StackTraceElement stackTraceElement : e.getStackTrace()){
                    stacktrace.append(stackTraceElement.toString()).append("\n");
                }
                BungeeMain.Logs.severe("Stack Trace: " + stacktrace.toString());
                executorService.shutdown();
                return;
            }
            executorService.shutdown();
        }
        if (uuid == null) {
            player.sendMessage(new ComponentBuilder(prefix).append("That is not a player's name!").color(ChatColor.RED).create());
            return;
        }
        String name = NameFetcher.getName(uuid);
        if (name == null){
            name = strings[0];
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
