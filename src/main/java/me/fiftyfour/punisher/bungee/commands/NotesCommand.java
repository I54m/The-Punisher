package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.PunisherPlugin;
import me.fiftyfour.punisher.bungee.handlers.ErrorHandler;
import me.fiftyfour.punisher.universal.exceptions.DataFecthException;
import me.fiftyfour.punisher.universal.util.NameFetcher;
import me.fiftyfour.punisher.universal.util.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class NotesCommand extends Command {
    public NotesCommand() {
        super("notes", "punisher.notes", "playernotes", "playernote", "note");
    }

    private String targetuuid;
    private final PunisherPlugin plugin = PunisherPlugin.getInstance();

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length < 2){
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Add, remove or list a player's notes").color(ChatColor.RED).create());
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Usage: /notes <add|remove|list> <player>").color(ChatColor.WHITE).create());
            return;
        }
        ProxiedPlayer findTarget = ProxyServer.getInstance().getPlayer(strings[1]);
        Future<String> future = null;
        ExecutorService executorService = null;
        if (findTarget != null) {
            targetuuid = findTarget.getUniqueId().toString().replace("-", "");
        } else {
            UUIDFetcher uuidFetcher = new UUIDFetcher();
            uuidFetcher.fetch(strings[1]);
            executorService = Executors.newSingleThreadExecutor();
            future = executorService.submit(uuidFetcher);
        }
        if (future != null) {
            try {
                targetuuid = future.get(1, TimeUnit.SECONDS);
            } catch (Exception e) {
                try {
                    throw new DataFecthException("UUID Required for next step", strings[0], "UUID", this.getName(), e);
                }catch (DataFecthException dfe){
                    ErrorHandler errorHandler = ErrorHandler.getINSTANCE();
                    errorHandler.log(dfe);
                    errorHandler.alert(dfe, commandSender);
                }
                executorService.shutdown();
                return;
            }
            executorService.shutdown();
        }
        if (targetuuid == null) {
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append(strings[0] + " is not a player's name!").color(ChatColor.RED).create());
            return;
        }
        String targetname = NameFetcher.getName(targetuuid);
        if (targetname == null) {
            targetname = strings[1];
        }
        if (strings[0].equalsIgnoreCase("add")){
            if (strings.length < 3){
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Add a note to a player").color(ChatColor.RED).create());
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Usage: /notes add <player> <note to add>").color(ChatColor.WHITE).create());
                return;
            }
            StringBuilder notes = new StringBuilder();
            notes.append("\"");
            for (int i = 2; i < strings.length; i++) {
                notes.append(strings[i]).append(" ");
            }
            if (commandSender instanceof ProxiedPlayer) {
                ProxiedPlayer player = (ProxiedPlayer) commandSender;
                notes.append("\" - ").append(player.getName());
            } else {
                notes.append("\" - CONSOLE");
            }
            if (PunisherPlugin.playerInfoConfig.contains(targetuuid + ".notes")) {
                List<String> previousNotes = PunisherPlugin.playerInfoConfig.getStringList(targetuuid + ".notes");
                previousNotes.add(notes.toString());
                PunisherPlugin.playerInfoConfig.set(targetuuid + ".notes", previousNotes);
            } else {
                List<String> notesList = new ArrayList<>();
                notesList.add(notes.toString());
                PunisherPlugin.playerInfoConfig.set(targetuuid + ".notes", notesList);
            }
            PunisherPlugin.saveInfo();
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Note Added to " + targetname).color(ChatColor.GREEN).create());
        }else if (strings[0].equalsIgnoreCase("remove")){
            if (strings.length < 3){
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Remove a player's note by id").color(ChatColor.RED).create());
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Usage: /notes remove <player> <id>").color(ChatColor.WHITE).create());
                return;
            }
            int id = 1;
            try {
                id = Integer.parseInt(strings[2]);
            }catch (NumberFormatException nfe){
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append(strings[2] + " is not a valid number").color(ChatColor.RED).create());
                return;
            }
            if (!PunisherPlugin.playerInfoConfig.contains(targetuuid + ".notes")) {
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append(targetname + " Does not have any notes!").color(ChatColor.RED).create());
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Add one by doing: /notes add <player> <note>").color(ChatColor.WHITE).create());
                return;
            }
            List<String> previousNotes = PunisherPlugin.playerInfoConfig.getStringList(targetuuid + ".notes");
            previousNotes.remove(id - 1);
            PunisherPlugin.playerInfoConfig.set(targetuuid + ".notes", previousNotes);
            PunisherPlugin.saveInfo();
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Note with id " + id + " removed from " + targetname).color(ChatColor.GREEN).create());
        }else if (strings[0].equalsIgnoreCase("list")){
            if (!PunisherPlugin.playerInfoConfig.contains(targetuuid + ".notes")) {
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append(targetname + " Does not have any notes!").color(ChatColor.RED).create());
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Add one by doing: /notes add <player> <note>").color(ChatColor.WHITE).create());
                return;
            }
            List<String> previousNotes = PunisherPlugin.playerInfoConfig.getStringList(targetuuid + ".notes");
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Notes for " + targetname + ":").color(ChatColor.GREEN).create());
            for(String note : previousNotes){
                commandSender.sendMessage(new ComponentBuilder(previousNotes.indexOf(note) + ". ").color(ChatColor.GREEN).append(note).color(ChatColor.GREEN).create());
            }
        }
    }
}
