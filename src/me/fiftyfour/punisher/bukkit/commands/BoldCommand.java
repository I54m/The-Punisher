package me.fiftyfour.punisher.bukkit.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BoldCommand implements CommandExecutor {
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] strings) {
        if (label.equalsIgnoreCase("b") || label.equalsIgnoreCase("bold")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You must be a player to use this command!");
                return false;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("punisher.bold")){
                player.sendMessage(prefix + ChatColor.RED + "You do not have permission to use this command!");
                return false;
            }
            if (strings.length < 1){
                player.sendMessage(ChatColor.RED + "Please Provide a message!");
                return false;
            }
            if (strings[0].equalsIgnoreCase("-r") && player.hasPermission("punisher.bold.red")){
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < strings.length; i++){
                    sb.append(ChatColor.DARK_RED).append(ChatColor.BOLD).append(strings[i]).append(" ");
                }
                player.chat(sb.toString());
                return true;
            }else if (!player.hasPermission("punisher.bold.red") && strings[0].equalsIgnoreCase("-r")){
                player.sendMessage(prefix + ChatColor.RED + "You do not have permission to use that command extension!");
                return false;
            }else{
                StringBuilder sb = new StringBuilder();
                for (String arg : strings){
                    sb.append(ChatColor.BOLD).append(arg).append(" ");
                }
                player.chat(sb.toString());
                return true;
            }
        }
        return false;
    }
}
