package me.fiftyfour.punisher.bungee.handlers;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.chats.AdminChat;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;

public class ErrorHandler {
    private BungeeMain plugin = BungeeMain.getInstance();

    private static final ErrorHandler INSTANCE = new ErrorHandler();
    private ErrorHandler(){}
    public static ErrorHandler getInstance(){return INSTANCE;}

    public void log(Throwable e){
        BungeeMain.Logs.severe("Error Message: " + e.getMessage());
        StringBuilder stacktrace = new StringBuilder();
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            stacktrace.append(stackTraceElement.toString()).append("\n");
        }
        BungeeMain.Logs.severe("Stack Trace: " + stacktrace.toString());
        if (e.getCause() != null)
            BungeeMain.Logs.severe("Error Cause Message: " + e.getCause().getMessage());
        ProxyServer.getInstance().getLogger().warning(" ");
        ProxyServer.getInstance().getLogger().warning(plugin.prefix + ChatColor.RED + "An error was encountered and debug info was logged to log file!");
        ProxyServer.getInstance().getLogger().warning(plugin.prefix + ChatColor.RED + "Error Message: " + e.getMessage());
        if (e.getCause() != null)
            ProxyServer.getInstance().getLogger().warning(plugin.prefix + ChatColor.RED + "Error Cause Message: " + e.getCause().getMessage());
        ProxyServer.getInstance().getLogger().warning(" ");
    }

    private void detailedAlert(Throwable e, CommandSender sender){
        sender.sendMessage(new ComponentBuilder(plugin.prefix).append("ERROR: ").color(ChatColor.DARK_RED).append(e.getMessage()).color(ChatColor.RED).create());
        sender.sendMessage(new ComponentBuilder(plugin.prefix).append("This error will be logged! Please inform a dev asap, this plugin may no longer function as intended!").color(ChatColor.RED).create());
    }

    public void alert(Throwable e, CommandSender sender){
        if (sender.hasPermission("punisher.admin")) detailedAlert(e, sender);
        else {
            sender.sendMessage(new ComponentBuilder(plugin.prefix).append("ERROR: ").color(ChatColor.DARK_RED).append("An unexpected error occurred while trying to perform that action!").color(ChatColor.RED).create());
            sender.sendMessage(new ComponentBuilder(plugin.prefix).append("This error will be logged! Please inform an admin+ asap, this plugin may no longer function as intended!").color(ChatColor.RED).create());
            adminChatAlert(e, sender);
        }
    }

    public void adminChatAlert(Throwable e, CommandSender sender){
        AdminChat.sendMessage(sender.getName() + " ENCOUNTERED AN ERROR: " + e.getMessage());
        AdminChat.sendMessage("This error will be logged! Please inform a dev asap, this plugin may no longer function as intended!");
    }

    public void loginError(LoginEvent event){
        event.setCancelled(true);
        event.setCancelReason(new TextComponent(ChatColor.RED + "ERROR: An error occurred during your login process and we were unable to fetch required data.\n Please inform an admin+ asap this plugin may no longer function as intended!"));
    }
}
