package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("Help", null);
    }
    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        String text = BungeeMain.PunisherConfig.getString("helpCommand.TextToUse");
        if (text == null) return;
        ChatColor color;
        try {
            color = ChatColor.valueOf(BungeeMain.PunisherConfig.getString("helpCommand.ColorToUse").toUpperCase());
        }catch (Exception e) {
            color = ChatColor.WHITE;
        }
        commandSender.sendMessage(new ComponentBuilder(text).color(color).create());
    }
}
