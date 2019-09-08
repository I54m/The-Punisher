package me.fiftyfour.punisher.bukkit.commands;

import me.fiftyfour.punisher.bukkit.BukkitMain;
import me.fiftyfour.punisher.universal.systems.UpdateChecker;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class PunisherBukkit implements CommandExecutor {
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;
    private BukkitMain plugin = BukkitMain.getInstance();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender.hasPermission("punisher.bukkit.admin")) {
            if (strings.length <= 0) {
                commandSender.sendMessage(prefix + ChatColor.GREEN + "/punisherbukkit version");
                return true;
            }
            if (strings[0].equalsIgnoreCase("version")) {
                if (!BukkitMain.update) {
                    commandSender.sendMessage(prefix + ChatColor.GREEN + "Current Bukkit Version: " + plugin.getDescription().getVersion());
                    commandSender.sendMessage(prefix + ChatColor.GREEN + "This is the latest version!");
                } else {
                    commandSender.sendMessage(prefix + ChatColor.RED + "Current Bukkit Version: " + plugin.getDescription().getVersion());
                    commandSender.sendMessage(prefix + ChatColor.RED + "Latest version: " + UpdateChecker.getCurrentVersion());
                }
                return true;
            } else {
                commandSender.sendMessage(prefix + ChatColor.RED + "Check the current bukkit version");
                commandSender.sendMessage(prefix + ChatColor.RED + "Usage: /punisherbukkit version");
            }
            return false;
        }else{
            commandSender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return false;
        }
    }
}
