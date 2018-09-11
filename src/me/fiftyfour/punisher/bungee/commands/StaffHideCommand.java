package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class StaffHideCommand extends Command {
    public StaffHideCommand() {
        super("staffhide", "punisher.staff.hide", "sh");
    }

    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer){
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            if (strings.length >= 1) {
                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(strings[0]);
                if (player.hasPermission("punisher.staff.hide.others")) {
                    if (!BungeeMain.StaffHidden.contains(target.getUniqueId().toString())) {
                        BungeeMain.StaffHidden.set(target.getUniqueId().toString(), "hidden");
                        player.sendMessage(new ComponentBuilder(prefix).append("You are now hidden on the staff list!").color(ChatColor.GREEN).create());
                    } else {
                        BungeeMain.StaffHidden.set(target.getUniqueId().toString(), null);
                        player.sendMessage(new ComponentBuilder(prefix).append("You are no longer hidden on the staff list!").color(ChatColor.GREEN).create());
                    }
                } else if (player.equals(target)){
                    if (!BungeeMain.StaffHidden.contains(player.getUniqueId().toString())) {
                        BungeeMain.StaffHidden.set(player.getUniqueId().toString(), "hidden");
                        player.sendMessage(new ComponentBuilder(prefix).append("You are now hidden on the staff list!").color(ChatColor.GREEN).create());
                    }else {
                        BungeeMain.StaffHidden.set(player.getUniqueId().toString(), null);
                        player.sendMessage(new ComponentBuilder(prefix).append("You are no longer hidden on the staff list!").color(ChatColor.GREEN).create());
                    }
                }else{
                    player.sendMessage(new ComponentBuilder(prefix).append("You don't have permission to staffhide others!").color(ChatColor.RED).create());
                    return;
                }

            }else{
                if (!BungeeMain.StaffHidden.contains(player.getUniqueId().toString())) {
                    BungeeMain.StaffHidden.set(player.getUniqueId().toString(), "hidden");
                    player.sendMessage(new ComponentBuilder(prefix).append("You are now hidden on the staff list!").color(ChatColor.GREEN).create());
                }else {
                    BungeeMain.StaffHidden.set(player.getUniqueId().toString(), null);
                    player.sendMessage(new ComponentBuilder(prefix).append("You are no longer hidden on the staff list!").color(ChatColor.GREEN).create());
                }
            }
            BungeeMain.saveStaffHide();
        }else{
            commandSender.sendMessage(new TextComponent("You must be a player to use this command!"));
        }
    }
}
