package me.fiftyfour.punisher.bukkit;

import me.fiftyfour.punisher.bukkit.commands.BoldCommand;
import me.fiftyfour.punisher.bukkit.commands.ClearChat;
import me.fiftyfour.punisher.bukkit.commands.ToggleChat;
import me.fiftyfour.punisher.systems.UpdateChecker;
import me.lucko.luckperms.api.LuckPermsApi;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitMain extends JavaPlugin implements Listener {
    private static BukkitMain instance;
    public String chatState;

    public static BukkitMain getInstance() {
        return instance;
    }

    private static void setInstance(BukkitMain instance) {
        BukkitMain.instance = instance;
    }
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;

    @Override
    public void onEnable() {
        //BETA warning
        if (this.getDescription().getVersion().contains("BETA"))
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "Warning: This is a beta version and some systems may be disabled or not function as intended!");
        //set variables
        setInstance(this);
        chatState = "on";
        //register commands
        this.getCommand("punish").setExecutor(new PunishGUI());
        this.getCommand("clearchat").setExecutor(new ClearChat());
        this.getCommand("togglechat").setExecutor(new ToggleChat());
        this.getCommand("bold").setExecutor(new BoldCommand());
        //register plugin message channels
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PunishGUI());
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        //check for dependencies
        if (Bukkit.getServicesManager().getRegistration(LuckPermsApi.class) == null){
            getLogger().warning("Luck Perms not detected, Plugin has been Disabled!");
            this.setEnabled(false);
        }
        getServer().getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "Checking for updates...");
        try {
            if (UpdateChecker.getCurrentVersion() == null) {
                getServer().getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "Could not check for update!");
            } else if (UpdateChecker.check()) {
                getServer().getConsoleSender().sendMessage(prefix + ChatColor.RED + "Update checker found an update, current version: " + this.getDescription().getVersion() + " latest version: " + UpdateChecker.getCurrentVersion());
                getServer().getConsoleSender().sendMessage(prefix + ChatColor.RED + "This update was released on: " + UpdateChecker.getRealeaseDate());
                getServer().getConsoleSender().sendMessage(prefix + ChatColor.RED + "This may fix some bugs and enhance features.");
                getServer().getConsoleSender().sendMessage(prefix + ChatColor.RED + "You will no longer receive support for this version!");
            } else {
                getServer().getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "Plugin is up to date!");
            }
        }catch (Exception e){
            getLogger().severe(e.getMessage());
        }
    }
}
