package me.fiftyfour.punisher.bukkit;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import me.fiftyfour.punisher.bukkit.commands.*;
import me.fiftyfour.punisher.bukkit.listeners.PluginMessage;
import me.fiftyfour.punisher.bukkit.listeners.PostLogin;
import me.fiftyfour.punisher.bukkit.objects.LevelOnePunishMenu;
import me.fiftyfour.punisher.bukkit.objects.LevelThreePunishMenu;
import me.fiftyfour.punisher.bukkit.objects.LevelTwoPunishMenu;
import me.fiftyfour.punisher.bukkit.objects.LevelZeroPunishMenu;
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
    public static boolean update = false;
    public static Map<String, Double> repCache = new HashMap<>();
    private static BukkitMain instance;
    private static long nextRepUpdate;
    public String chatState;
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;

    public static BukkitMain getInstance() {
        return instance;
    }

    private static void setInstance(BukkitMain instance) {
        BukkitMain.instance = instance;
    }

    public static void updateRepCache() {
        nextRepUpdate = System.nanoTime() + 500000000;
        for (Player players : Bukkit.getOnlinePlayers()) {
            try {
                ByteArrayOutputStream outbytes = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(outbytes);
                out.writeUTF("getrepcache");
                out.writeUTF(players.getUniqueId().toString().replace("-", ""));
                players.sendPluginMessage(BukkitMain.getPlugin(BukkitMain.class), "punisher:minor", outbytes.toByteArray());
                out.close();
                outbytes.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    @Override
    public void onEnable() {
        //Check if version of mc is compatible
//        if (this.getServer().getVersion().contains("1.13")) {
//            getServer().getConsoleSender().sendMessage("This version of the punisher is not compatible with minecraft 1.13.x");
//            getServer().getConsoleSender().sendMessage("Please downgrade to 1.12.2 to get this version to work!");
//            getServer().getConsoleSender().sendMessage("Or update to the punisher 1.9+ to use this spigot version.");
//            getServer().getConsoleSender().sendMessage("Other compatible spigot versions include: 1.8.x, 1.9.x, 1.10.x, 1.11.x and 1.12.x!");
//            getServer().getConsoleSender().sendMessage("Plugin Disabled!");
//            this.setEnabled(false);
//            return;
//        } else
        if (!this.getServer().getVersion().contains("1.13")) {
            getServer().getConsoleSender().sendMessage("This version of the punisher is not compatible with minecraft " + this.getServer().getVersion());
            getServer().getConsoleSender().sendMessage("Please use the punisher 1.8-LEGACY for this version of minecraft!");
            getServer().getConsoleSender().sendMessage("Or consider updating to 1.13.x to have the latest new features!");
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
        } catch (Exception e) {
            getServer().getConsoleSender().sendMessage("Luck Perms not detected, Plugin has been Disabled!");
            this.setEnabled(false);
            return;
        }
        //set variables
        setInstance(this);
        chatState = "on";
        //setup punish gui menus
        LevelZeroPunishMenu.setupMenu();
        LevelOnePunishMenu.setupMenu();
        LevelTwoPunishMenu.setupMenu();
        LevelThreePunishMenu.setupMenu();
        //register commands
        getCommand("punish").setExecutor(new PunishCommand());
        getCommand("clearchat").setExecutor(new ClearChat());
        getCommand("togglechat").setExecutor(new ToggleChat());
        getCommand("bold").setExecutor(new BoldCommand());
        getCommand("punisherbukkit").setExecutor(new PunisherBukkit());
        //register plugin message channels
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "punisher:minor", new PluginMessage());
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "punisher:minor");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "punisher:main", new PunishCommand());
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "punisher:main");
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
        }
        if (this.getDescription().getVersion().contains("LEGACY")) {
            getServer().getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "You are running a LEGACY version of The Punisher");
            getServer().getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "This version is no longer updated with new features and ONLY MAJOR BUGS WILL BE FIXED!!");
            getServer().getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "It is recommended that you update your server to 1.13.2 to have the new features.");
            getServer().getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "Update checking is not needed in these versions");
            update = false;
        } else if (this.getDescription().getVersion().contains("BETA") && this.getDescription().getVersion().contains("PRE-RELEASE")
                && this.getDescription().getVersion().contains("DEV-BUILD") && this.getDescription().getVersion().contains("SNAPSHOT")) {
            getServer().getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "You are running a PRE-RELEASE version of The Punisher");
            getServer().getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "Update checking is not needed in these versions");
            update = false;
        }
        if (Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
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

        } else
            getServer().getConsoleSender().sendMessage(prefix + ChatColor.RED + "MVDWPlaceholderApi Not detected reputation placeholder will not work!");
    }
}
