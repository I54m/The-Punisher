package me.fiftyfour.punisher.bukkit;

import me.fiftyfour.punisher.bukkit.commands.BoldCommand;
import me.fiftyfour.punisher.bukkit.commands.ClearChat;
import me.fiftyfour.punisher.bukkit.commands.PunisherBukkit;
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
    public static boolean update = false;

    public static BukkitMain getInstance() {
        return instance;
    }

    private static void setInstance(BukkitMain instance) {
        BukkitMain.instance = instance;
    }
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;

    @Override
    public void onEnable() {
        //Check if version of mc is compatible
        if (this.getServer().getVersion().contains("1.13")){
            getLogger().severe("This version of the punisher is not compatible with minecraft 1.13.x");
            getLogger().severe("Due to major data changes in 1.13 there are items in the gui that no longer work");
            getLogger().severe("Please downgrade to 1.12.2 to get this version to work!");
            getLogger().severe("Other compatible versions include: 1.9.x, 1.10.x, 1.11.x and 1.12.x!");
            getLogger().severe("Plugin Disabled!");
            this.setEnabled(false);
            return;
        }else if (!this.getServer().getVersion().contains("1.12") && !this.getServer().getVersion().contains("1.11") && !this.getServer().getVersion().contains("1.10")
        && !this.getServer().getVersion().contains("1.9") && !this.getServer().getVersion().contains("1.8")){
            getLogger().severe("This version of the punisher is not compatible with minecraft " + this.getServer().getVersion());
            getLogger().severe("Due to items that are not available in this version there are items in the gui that no longer work");
            getLogger().severe("Please upgrade to 1.8.8 to get this version to work!");
            getLogger().severe("Other compatible versions include: 1.9.x, 1.10.x, 1.11.x and 1.12.x!");
            getLogger().severe("Plugin Disabled!");
            this.setEnabled(false);
            return;
        }
        //check for dependencies
        try {
            if (Bukkit.getServicesManager().getRegistration(LuckPermsApi.class) == null) {
                getLogger().severe("Luck Perms not detected, Plugin has been Disabled!");
                this.setEnabled(false);
                return;
            }
        }catch(Exception e){
            getLogger().severe("Luck Perms not detected, Plugin has been Disabled!");
            this.setEnabled(false);
            return;
        }
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
        this.getCommand("punisherbukkit").setExecutor(new PunisherBukkit());
        //register plugin message channels
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PunishGUI());
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PluginMessageEvent());
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        //check for updates
        getServer().getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "Checking for updates...");
        try {
            if (UpdateChecker.getCurrentVersion() == null) {
                getServer().getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "Could not check for update!");
                update = false;
            } else if (UpdateChecker.check()) {
                getServer().getConsoleSender().sendMessage(prefix + ChatColor.RED + "Update checker found an update, current version: " + this.getDescription().getVersion() + " latest version: " + UpdateChecker.getCurrentVersion());
                getServer().getConsoleSender().sendMessage(prefix + ChatColor.RED + "This update was released on: " + UpdateChecker.getRealeaseDate());
                getServer().getConsoleSender().sendMessage(prefix + ChatColor.RED + "This may fix some bugs and enhance features.");
                getServer().getConsoleSender().sendMessage(prefix + ChatColor.RED + "You will no longer receive support for this version!");
                update = true;
            } else {
                getServer().getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "Plugin is up to date!");
                update = false;
            }
        }catch (Exception e){
            getLogger().severe(e.getMessage());
        }
    }
}
