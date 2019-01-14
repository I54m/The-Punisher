package me.fiftyfour.punisher.bukkit;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import me.fiftyfour.punisher.bukkit.commands.*;
import me.fiftyfour.punisher.bukkit.events.PluginMessage;
import me.fiftyfour.punisher.bukkit.events.PostLogin;
import me.fiftyfour.punisher.universal.systems.UpdateChecker;
import me.lucko.luckperms.api.LuckPermsApi;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class BukkitMain extends JavaPlugin implements Listener {
    private static BukkitMain instance;
    public String chatState;
    public static boolean update = false;
    public static Map<String, Double> repCache = new HashMap<>();
    private static long nextRepUpdate;

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
            getServer().getConsoleSender().sendMessage("This version of the punisher is not compatible with minecraft 1.13.x");
            getServer().getConsoleSender().sendMessage("Due to major data changes in 1.13 there are items in the gui that no longer work");
            getServer().getConsoleSender().sendMessage("Please downgrade to 1.12.2 to get this version to work!");
            getServer().getConsoleSender().sendMessage("Other compatible versions include: 1.9.x, 1.10.x, 1.11.x and 1.12.x!");
            getServer().getConsoleSender().sendMessage("Plugin Disabled!");
            this.setEnabled(false);
            return;
        }else if (!this.getServer().getVersion().contains("1.12") && !this.getServer().getVersion().contains("1.11") && !this.getServer().getVersion().contains("1.10")
        && !this.getServer().getVersion().contains("1.9") && !this.getServer().getVersion().contains("1.8")){
            getServer().getConsoleSender().sendMessage("This version of the punisher is not compatible with minecraft " + this.getServer().getVersion());
            getServer().getConsoleSender().sendMessage("Due to items that are not available in this version there are items in the gui that no longer work");
            getServer().getConsoleSender().sendMessage("Please upgrade to 1.8.8 to get this version to work!");
            getServer().getConsoleSender().sendMessage("Other compatible versions include: 1.9.x, 1.10.x, 1.11.x and 1.12.x!");
            getServer().getConsoleSender().sendMessage("Plugin Disabled!");
            this.setEnabled(false);
            return;
        }
        //check for dependencies
        try {
            if (Bukkit.getServicesManager().getRegistration(LuckPermsApi.class) == null) {
                getServer().getConsoleSender().sendMessage("Luck Perms not detected, Plugin has been Disabled!");
                this.setEnabled(false);
                return;
            }
        }catch(Exception e){
            getServer().getConsoleSender().sendMessage("Luck Perms not detected, Plugin has been Disabled!");
            this.setEnabled(false);
            return;
        }
        //set variables
        setInstance(this);
        chatState = "on";
        //register commands
        this.getCommand("punish").setExecutor(new PunishCommand());
        this.getCommand("clearchat").setExecutor(new ClearChat());
        this.getCommand("togglechat").setExecutor(new ToggleChat());
        this.getCommand("bold").setExecutor(new BoldCommand());
        this.getCommand("punisherbukkit").setExecutor(new PunisherBukkit());
        //register plugin message channels
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PunishCommand());
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PluginMessage());
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        //check for update
        getServer().getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "Checking for updates...");
        if (!this.getDescription().getVersion().contains("BETA") && !this.getDescription().getVersion().contains("PRE-RELEASE")
                && !this.getDescription().getVersion().contains("DEV-BUILD") && !this.getDescription().getVersion().contains("SNAPSHOT")) {
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
            } catch (Exception e) {
                getServer().getConsoleSender().sendMessage(ChatColor.RED + e.getMessage());
            }
        }else{
            getServer().getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "You are running a PRE-RELEASE version of The Punisher");
            getServer().getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "Update checking is not needed in these versions");
            update = false;
        }
        if (Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")){
            getServer().getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "MVdWPlaceholderApi detected, enabling reputation placeholder...");
            Bukkit.getPluginManager().registerEvents(new PostLogin(), this);
            PlaceholderAPI.registerPlaceholder(this, "punisher_reputation", (event) -> {
                if (event.isOnline() && event.getPlayer() != null) {
                    if (nextRepUpdate <= System.nanoTime())
                        updateRepCache();
                    Player player = event.getPlayer();
                    String uuid = player.getUniqueId().toString().replace("-", "");
                    if (repCache.get(uuid) != null) {
                        //format rep with color
                        StringBuilder reputation = new StringBuilder();
                        double rep = repCache.get(uuid);
                        String repString = new DecimalFormat("##.##").format(rep);
                        if (rep == 5) {
                            reputation.append(ChatColor.WHITE).append("(").append(repString).append("/10").append(")");
                        } else if (rep > 5) {
                            reputation.append(ChatColor.GREEN).append("(").append(repString).append("/10").append(")");
                        } else if (rep < 5 && rep > -1) {
                            reputation.append(ChatColor.YELLOW).append("(").append(repString).append("/10").append(")");
                        } else if (rep < -1 && rep > -8) {
                            reputation.append(ChatColor.GOLD).append("(").append(repString).append("/10").append(")");
                        } else if (rep < -8) {
                            reputation.append(ChatColor.RED).append("(").append(repString).append("/10").append(")");
                        }
                        return reputation.toString();
                    }
                }
                return "-";
            });

        }else getServer().getConsoleSender().sendMessage(prefix + ChatColor.RED + "MVDWPlaceholderApi Not detected reputation placeholder will not work!");
    }

    public static void updateRepCache(){
        nextRepUpdate = System.nanoTime() + 500000000;
        for (Player players : Bukkit.getOnlinePlayers()){
            try {
                ByteArrayOutputStream outbytes = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(outbytes);
                out.writeUTF("getrepcache");
                out.writeUTF(players.getUniqueId().toString().replace("-", ""));
                players.sendPluginMessage(BukkitMain.getPlugin(BukkitMain.class), "BungeeCord", outbytes.toByteArray());
                out.close();
                outbytes.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
