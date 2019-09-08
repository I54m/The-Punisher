package me.fiftyfour.punisher.bukkit.commands;


import me.fiftyfour.punisher.bukkit.BukkitMain;
import me.fiftyfour.punisher.bukkit.objects.LevelOnePunishMenu;
import me.fiftyfour.punisher.bukkit.objects.LevelThreePunishMenu;
import me.fiftyfour.punisher.bukkit.objects.LevelTwoPunishMenu;
import me.fiftyfour.punisher.bukkit.objects.LevelZeroPunishMenu;
import me.fiftyfour.punisher.universal.exceptions.DataFecthException;
import me.fiftyfour.punisher.universal.fetchers.NameFetcher;
import me.fiftyfour.punisher.universal.fetchers.UUIDFetcher;
import me.fiftyfour.punisher.universal.fetchers.UserFetcher;
import me.fiftyfour.punisher.universal.systems.Permissions;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Contexts;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.caching.PermissionData;
import me.lucko.luckperms.api.context.ContextManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.*;


public class PunishCommand implements PluginMessageListener, CommandExecutor {
    private static String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;
    private BukkitMain plugin = BukkitMain.getInstance();

    private static void sendPluginMessage(@NotNull Player player, String channel, @NotNull String... messages) {
        try {
            ByteArrayOutputStream outbytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(outbytes);
            for (String msg : messages) {
                out.writeUTF(msg);
            }
            player.sendPluginMessage(BukkitMain.getPlugin(BukkitMain.class), channel, outbytes.toByteArray());
            out.close();
            outbytes.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void punishmentSelected(String toPunishuuid, String targetName, int slot, String item, Player clicker) {
        try {
            User user = LuckPerms.getApi().getUser(targetName);
            if (user == null) {
                UserFetcher userFetcher = new UserFetcher();
                userFetcher.setUuid(UUIDFetcher.formatUUID(toPunishuuid));
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                Future<User> userFuture = executorService.submit(userFetcher);
                user = userFuture.get(5, TimeUnit.SECONDS);
                executorService.shutdown();
                if (user == null) {
                    throw new IllegalArgumentException("User cannot be null!");
                }
            }
            ContextManager cm = LuckPerms.getApi().getContextManager();
            Contexts contexts = cm.lookupApplicableContexts(user).orElse(cm.getStaticContexts());
            PermissionData permissionData = user.getCachedData().getPermissionData(contexts);
            if (permissionData.getPermissionValue("punisher.bypass").asBoolean() || Permissions.higher(clicker, user)) {
                clicker.playSound(clicker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 100, 1.5f);
                if ((slot == 10 || slot == 11) && item.contains("Spam, Flood ETC"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Minor_Chat_Offence", "Minor Chat Offence");
                else if ((slot == 11 || slot == 12) && item.contains("Racism, Disrespect ETC"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Major_Chat_Offence", "Major Chat Offence");
                else if (slot == 13 && item.contains("For Other Not Listed Offences"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Other_Offence", "Other Offence");
                else if (slot == 14 && item.contains("Includes Hinting At It and"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "DDoS_DoX_Threats", "DDoS/DoX Threats");
                else if (slot == 15 && item.contains("Includes Pm's"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Inappropriate_Link", "Inappropriate Link");
                else if (slot == 16 && item.contains("When a player is unfairly taking a player's"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Scamming", "Scamming");
                else if (slot == 17 && item.contains("Mining Straight to Ores/Bases/Chests"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "X_Raying", "X-Raying");
                else if (slot == 18 && item.contains("Using AutoClicker to Farm Mobs ETC"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "AutoClicker", "AutoClicker(Non PvP)");
                else if (slot == 19 && item.contains("Includes Hacks Such as Jesus and Spider"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Fly_Speed_Hacking", "Fly/Speed Hacking");
                else if (slot == 20 && item.contains("Includes Hacks Such as Kill Aura and Reach"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Malicious_PvP_Hacks", "Malicious PvP Hacks");
                else if (slot == 21 && item.contains("Includes Hacks Such as Derp and Headless"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Disallowed_Mods", "Disallowed Mods");
                else if (slot == 22 && item.contains("Excludes Cobble Monstering and Bypassing land claims"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Greifing", "Greifing");
                else if (slot == 23 && item.contains("Warning: Must Also Clear Chat After you have proof!"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Server_Advertisment", "Server Advertisment");
                else if (slot == 24 && item.contains("Includes Bypassing Land Claims and Cobble Monstering"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Exploiting", "Exploiting");
                else if (slot == 25 && item.contains("Sending a TPA Request to Someone"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Tpa_Trapping", "TPA-Trapping");
                else if (slot == 30 && item.contains("Includes Inappropriate IGN's and Other Major Offences"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Other_Major_Offence", "Other Major Offence");
                else if (slot == 31 && item.contains("For Other Minor Offences"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Other_Minor_Offence", "Other Minor Offence");
                else if (slot == 32 && item.contains("Any type of Impersonation"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Impersonation", "Player Impersonation");
                else if (slot == 36 && item.contains("Manually Warn the Player"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Manual", "WARN", "Manually Warned");
                else if (slot == 37 && item.contains("Manually Mute the Player For 1 Hour"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Manual_Hour", "MUTE", "Manually Muted for 1 Hour");
                else if (slot == 38 && item.contains("Manually Mute the Player For 1 Day"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Manual_1_Day", "MUTE", "Manually Muted for 1 Day");
                else if (slot == 39 && item.contains("Manually Mute the Player For 3 Days"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Manual_3_Day", "MUTE", "Manually Muted for 3 Days");
                else if (slot == 40 && item.contains("Manually Mute the Player For 1 Week"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Manual_1_Week", "MUTE", "Manually Muted for 1 Week");
                else if (slot == 41 && item.contains("Manually Mute the Player For 2 Weeks"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Manual_2_Week", "MUTE", "Manually Muted for 2 Weeks");
                else if (slot == 42 && item.contains("Manually Mute the Player For 3 Weeks"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Manual_3_Week", "MUTE", "Manually Muted for 3 Weeks");
                else if (slot == 43 && item.contains("Manually Mute the Player For 1 Month"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Manual_1_Month", "MUTE", "Manually Muted for 1 Month");
                else if (slot == 44 && item.contains("Manually Mute the Player Permanently"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Manual_Permanently", "MUTE", "Manually Muted Permanently");
                else if (slot == 45 && item.contains("Manually Kick the Player"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Manual", "KICK", "Manually Kicked");
                else if (slot == 46 && item.contains("Manually Ban The Player For 1 Hour"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Manual_Hour", "BAN", "Manually Banned for 1 Hour");
                else if (slot == 47 && item.contains("Manually Ban the Player For 1 Day"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Manual_1_Day", "BAN", "Manually Banned for 1 Day");
                else if (slot == 48 && item.contains("Manually Ban the Player For 3 Days"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Manual_3_Day", "BAN", "Manually Banned for 3 Days");
                else if (slot == 49 && item.contains("Manually Ban the Player For 1 Week"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Manual_1_Week", "BAN", "Manually Banned for 1 Week");
                else if (slot == 50 && item.contains("Manually Ban the Player For 2 Weeks"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Manual_2_Week", "BAN", "Manually Banned for 2 Weeks");
                else if (slot == 51 && item.contains("Manually Ban the Player For 3 Weeks"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Manual_3_Week", "BAN", "Manually Banned for 3 Weeks");
                else if (slot == 52 && item.contains("Manually Ban the Player For 1 Month"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Manual_1_Month", "BAN", "Manually Banned For 1 Month");
                else if (slot == 53 && item.contains("Manually Ban the Player Permanently"))
                    sendPluginMessage(clicker, "punisher:main", "punish", targetName, toPunishuuid, "Manual_Permanently", "BAN", "Manually Banned Permanently");
            } else {
                clicker.sendMessage(prefix + ChatColor.RED + "You cannot Punish that player!");
            }
        } catch (Exception e) {
            try {
                throw new DataFecthException("User instance required for punishment level checking", targetName, "User Instance", Permissions.class.getName(), e);
            } catch (DataFecthException dfe) {
                clicker.sendMessage(prefix + ChatColor.DARK_RED + "ERROR: " + ChatColor.RED + "Luckperms was unable to fetch permission data on: " + targetName);
                clicker.sendMessage(prefix + ChatColor.RED + "This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ");
                sendPluginMessage(clicker, "punisher:minor", "log", "SEVERE", "ERROR: Luckperms was unable to fetch permission data on: " + targetName);
                sendPluginMessage(clicker, "punisher:minor", "log", "SEVERE", "Error message: " + dfe.getMessage());
                sendPluginMessage(clicker, "punisher:minor", "log", "SEVERE", "Cause Error message: " + dfe.getCause().getMessage());
                StringBuilder stacktrace = new StringBuilder();
                for (StackTraceElement stackTraceElement : dfe.getStackTrace()) {
                    stacktrace.append(stackTraceElement.toString()).append("\n");
                }
                sendPluginMessage(clicker, "punisher:minor", "log", "SEVERE", "Stack Trace: " + stacktrace.toString());
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("punish") || label.equalsIgnoreCase("p") || label.equalsIgnoreCase("pun")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You must be a player to use this command!");
                return false;
            }
            Player p = (Player) sender;
            if (p.hasPermission("punisher.punish.level.0")) {
                if (args.length <= 0) {
                    p.sendMessage(ChatColor.RED + "Please provide a player's name!");
                    return false;
                }
                Player findTarget = Bukkit.getPlayer(args[0]);
                String targetuuid = null;
                Future<String> future = null;
                ExecutorService executorService = null;
                if (findTarget != null) {
                    targetuuid = findTarget.getUniqueId().toString().replace("-", "");
                    UUID formatedUuid = UUIDFetcher.formatUUID(targetuuid);
                    if (formatedUuid.equals(p.getUniqueId())) {
                        p.sendMessage(prefix + ChatColor.RED + "You may not punish yourself!");
                        return false;
                    }
                } else {
                    UUIDFetcher uuidFetcher = new UUIDFetcher();
                    uuidFetcher.fetch(args[0]);
                    executorService = Executors.newSingleThreadExecutor();
                    future = executorService.submit(uuidFetcher);
                }
                if (future != null) {
                    try {
                        targetuuid = future.get(10, TimeUnit.SECONDS);
                    } catch (TimeoutException te) {
                        p.sendMessage(prefix + ChatColor.DARK_RED + "ERROR: " + ChatColor.RED + "Connection to mojang API took too long! Unable to fetch " + args[0] + "'s uuid!");
                        p.sendMessage(prefix + ChatColor.RED + "This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ");
                        sendPluginMessage(p, "punisher:minor", "log", "SEVERE", "ERROR: Connection to mojang API took too long! Unable to fetch " + args[0] + "'s uuid!");
                        sendPluginMessage(p, "punisher:minor", "log", "SEVERE", "Error message: " + te.getMessage());
                        StringBuilder stacktrace = new StringBuilder();
                        for (StackTraceElement stackTraceElement : te.getStackTrace()) {
                            stacktrace.append(stackTraceElement.toString()).append("\n");
                        }
                        sendPluginMessage(p, "punisher:minor", "log", "SEVERE", "Stack Trace: " + stacktrace.toString());
                        executorService.shutdown();
                        return false;
                    } catch (Exception e) {
                        p.sendMessage(prefix + ChatColor.DARK_RED + "ERROR: " + ChatColor.RED + "Unexpected Error while setting up GUI! Unable to fetch " + args[0] + "'s uuid!");
                        p.sendMessage(prefix + ChatColor.RED + "This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ");
                        sendPluginMessage(p, "punisher:minor", "log", "SEVERE", "ERROR: Unexpected error while setting up GUI! Unable to fetch " + args[0] + "'s uuid!");
                        sendPluginMessage(p, "punisher:minor", "log", "SEVERE", "Error message: " + e.getMessage());
                        StringBuilder stacktrace = new StringBuilder();
                        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                            stacktrace.append(stackTraceElement.toString()).append("\n");
                        }
                        sendPluginMessage(p, "punisher:minor", "log", "SEVERE", "Stack Trace: " + stacktrace.toString());
                        executorService.shutdown();
                        return false;
                    }
                    executorService.shutdown();
                }
                if (targetuuid == null) {
                    p.sendMessage(ChatColor.RED + "That is not a player's name!");
                    return false;
                }
                String targetName = NameFetcher.getName(targetuuid);
                if (targetName == null) {
                    targetName = args[0];
                }
                if (!BukkitMain.repCache.containsKey(targetuuid)) {
                    sendPluginMessage(p, "punisher:main", "getrep", targetuuid, targetName);
                } else {
                    double rep = BukkitMain.repCache.get(targetuuid);
                    String repString = new DecimalFormat("##.##").format(rep);
                    StringBuilder reputation = new StringBuilder();
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
                    openGUI(p, targetuuid, targetName, reputation.toString());
                }

            } else {
                p.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return false;
            }
        }
        return false;
    }

    @Override
    public void onPluginMessageReceived(String channel, final Player player, byte[] message) {
        if (channel.equals("punisher:main")) {
            try {
                ByteArrayInputStream inBytes = new ByteArrayInputStream(message);
                DataInputStream in = new DataInputStream(inBytes);
                String subchannel = in.readUTF();
                if (subchannel.equals("rep")) {
                    String repstring = in.readUTF();
                    double rep;
                    String targetuuid = in.readUTF();
                    String targetname = in.readUTF();
                    StringBuilder reputation = new StringBuilder();
                    try {
                        rep = Double.parseDouble(repstring);
                    } catch (NumberFormatException e) {
                        reputation.append(ChatColor.WHITE).append("(").append("-").append("/10").append(")");
                        openGUI(player, targetuuid, targetname, reputation.toString());
                        in.close();
                        inBytes.close();
                        return;
                    }
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
                    openGUI(player, targetuuid, targetname, reputation.toString());
                }
                in.close();
                inBytes.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private void openGUI(final Player p, String targetuuid, String targetName, String reputation) {
        if (p.hasPermission("punisher.punish.level.3")) {
            LevelThreePunishMenu punishMenu = new LevelThreePunishMenu();
            punishMenu.open(p, targetuuid, targetName, reputation);
        } else if (p.hasPermission("punisher.punish.level.2")) {
            LevelTwoPunishMenu punishMenu = new LevelTwoPunishMenu();
            punishMenu.open(p, targetuuid, targetName, reputation);
        } else if (p.hasPermission("punisher.punish.level.1")) {
            LevelOnePunishMenu punishMenu = new LevelOnePunishMenu();
            punishMenu.open(p, targetuuid, targetName, reputation);
        } else if (p.hasPermission("punisher.punish.level.0")) {
            LevelZeroPunishMenu punishMenu = new LevelZeroPunishMenu();
            punishMenu.open(p, targetuuid, targetName, reputation);
        } else {
            p.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
        }
    }
}