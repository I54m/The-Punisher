package me.fiftyfour.punisher.bungee;

import com.google.common.io.ByteStreams;
import com.zaxxer.hikari.HikariDataSource;
import me.fiftyfour.punisher.bungee.chats.AdminChat;
import me.fiftyfour.punisher.bungee.chats.StaffChat;
import me.fiftyfour.punisher.bungee.commands.*;
import me.fiftyfour.punisher.bungee.events.*;
import me.fiftyfour.punisher.systems.UpdateChecker;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class BungeeMain extends Plugin implements Listener {
//setup variables
    private static BungeeMain instance;
    private static File Cooldowns, StaffHide;
    public static File Reputation, Punishments;
    public static Configuration PunishmentsConfig, PunisherConfig, CooldownsConfig, RepStorage, StaffHidden;
    private File Punisher;
    public static Logger Logs = Logger.getLogger("Punisher Logs");
    public static boolean update;
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;
    private String host, username, password, extraArguments;
    public static String database;
    private int port;
    public Connection connection;
    public static BungeeMain getInstance() {
        return instance;
    }
    private static void setInstance(BungeeMain instance) {
        BungeeMain.instance = instance;
    }
    private HikariDataSource hikari;

    @Override
    public void onEnable() {
        long startTime = System.nanoTime();
        //set instance
        setInstance(this);
        //startup messages
        if (this.getDescription().getVersion().contains("BETA"))
            getLogger().warning(prefix + ChatColor.RED + "Warning: This is a beta version and some systems may be disabled or not function as intended!");
        //register commands
        getProxy().getPluginManager().registerCommand(this, new AdminCommands());
        getProxy().getPluginManager().registerCommand(this, new AltsCommand());
        getProxy().getPluginManager().registerCommand(this, new BanCommand());
        getProxy().getPluginManager().registerCommand(this, new PunHelpCommand());
        getProxy().getPluginManager().registerCommand(this, new HistoryCommand());
        getProxy().getPluginManager().registerCommand(this, new IpCommand());
        getProxy().getPluginManager().registerCommand(this, new KickCommand());
        getProxy().getPluginManager().registerCommand(this, new KickAllCommand());
        getProxy().getPluginManager().registerCommand(this, new MuteCommand());
        getProxy().getPluginManager().registerCommand(this, new PingCommand());
        getProxy().getPluginManager().registerCommand(this, new GlobalCommand());
        getProxy().getPluginManager().registerCommand(this, new ReportCommand());
        getProxy().getPluginManager().registerCommand(this, new ReputationCommand());
        getProxy().getPluginManager().registerCommand(this, new StaffCommand());
        getProxy().getPluginManager().registerCommand(this, new StaffHideCommand());
        getProxy().getPluginManager().registerCommand(this, new StaffhistoryCommand());
        getProxy().getPluginManager().registerCommand(this, new SuperBroadcast());
        getProxy().getPluginManager().registerCommand(this, new UnbanCommand());
        getProxy().getPluginManager().registerCommand(this, new UnmuteCommand());
        getProxy().getPluginManager().registerCommand(this, new UnpunishCommand());
        getProxy().getPluginManager().registerCommand(this, new viewRepCommand());
        getProxy().getPluginManager().registerCommand(this, new WarnCommand());
        getProxy().getPluginManager().registerCommand(this, new StaffChat());
        getProxy().getPluginManager().registerCommand(this, new AdminChat());
        //register event listeners
        getProxy().getPluginManager().registerListener(this, new onChat());
        getProxy().getPluginManager().registerListener(this, new onServerConnect());
        getProxy().getPluginManager().registerListener(this, new onPluginMessage());
        getProxy().getPluginManager().registerListener(this, new onTabComplete());
        //register calculation system listeners
        getProxy().getPluginManager().registerListener(this, new PunishmentCalc());
        //check for dependencies
        if (getProxy().getPluginManager().getPlugin("LuckPerms") == null){
            getLogger().warning(prefix + ChatColor.RED + "Luck Perms not detected, Plugin has been Disabled!");
            getProxy().getPluginManager().unregisterCommands(this);
            getProxy().getPluginManager().unregisterListeners(this);
        }
        //load all configs and create them if they don't exist
        loadConfig();
        //check config version
        if (!PunisherConfig.getString("Configversion").equals(this.getDescription().getVersion())){
            getLogger().warning(prefix + ChatColor.RED + "Old config.yml detected!");
            getLogger().warning(prefix + ChatColor.RED + "Renaming old config to old_config.yml!");
            File newfile = new File(getDataFolder(), "old_config.yml");
            try {
                Files.move(Punisher.toPath(), newfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Punisher.createNewFile();
            }catch (IOException ioe){
                ioe.printStackTrace();
            }
            saveDefaultConfig();
            PunisherConfig.set("Configversion", this.getDescription().getVersion());
        }
        //check if rep on vote should be enabled
        if (getProxy().getPluginManager().getPlugin("Votifier") != null && PunisherConfig.getBoolean("Voting.addRepOnVote")){
            getLogger().info(prefix + ChatColor.GREEN + "Enabled Rep on Vote feature!");
            getProxy().getPluginManager().registerListener(this, new onVote());
        }
        //start logging to logs file
        Logs.info("****START OF LOGS BEGINNING DATE: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "****");
        //check if help command should be enabled
        if (PunisherConfig.getBoolean("helpCommand.Enabled")){
            getProxy().getPluginManager().registerCommand(this, new HelpCommand());
        }
        //check for update
        getLogger().info(prefix + ChatColor.GREEN + "Checking for updates...");
        try {
            if (UpdateChecker.getCurrentVersion() == null) {
                getLogger().info(prefix + ChatColor.GREEN + "Could not check for update!");
                update = false;
            } else if (UpdateChecker.check()) {
                getLogger().warning(prefix + ChatColor.RED + "Update checker found an update, current version: " + this.getDescription().getVersion() + " latest version: " + UpdateChecker.getCurrentVersion());
                getLogger().warning(prefix + ChatColor.RED + "This update was released on: " + UpdateChecker.getRealeaseDate());
                getLogger().warning(prefix + ChatColor.RED + "This may fix some bugs and enhance features.");
                getLogger().warning(prefix + ChatColor.RED + "You will no longer receive support for this version!");
                Logs.warning("Update checker found an update current version: " + this.getDescription().getVersion() + " latest version: " + UpdateChecker.getCurrentVersion());
                Logs.warning("This update was released on: " + UpdateChecker.getRealeaseDate());
                Logs.warning("This may fix some bugs and enhance features.");
                Logs.warning("You will no longer receive support for this version!");
                update = true;
            } else {
                getLogger().info(prefix + ChatColor.GREEN + "Plugin is up to date!");
                update = false;
            }
        }catch (Exception e){
            getLogger().severe(ChatColor.RED + e.getMessage());
        }
        //setup mysql connection
        getLogger().info(prefix + ChatColor.GREEN + "Establishing MYSQL connection...");
        host = PunisherConfig.getString("MySQL.host");
        database = PunisherConfig.getString("MySQL.database");
        username = PunisherConfig.getString("MySQL.username");
        password = PunisherConfig.getString("MySQL.password");
        port = PunisherConfig.getInt("MySQL.port");
        extraArguments = PunisherConfig.getString("MySQL.extraArguments");
        hikari = new HikariDataSource();
        hikari.addDataSourceProperty("serverName", host);
        hikari.addDataSourceProperty("port", port);
        hikari.setPassword(password);
        hikari.setUsername(username);
        hikari.setJdbcUrl("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.extraArguments);
        hikari.setPoolName("The Punisher-hikari");
        hikari.setMaximumPoolSize(10);
        hikari.setMinimumIdle(10);
        try {
            openConnection();
        } catch (SQLException e) {
            getLogger().severe(prefix + ChatColor.RED + "MYSQL Connection failed!!! (SQLException)");
            mysqlfail(e);
            return;
        }
        //setup mysql
        setupmysql();
        testConnection();
        long duration = (System.nanoTime()-startTime)/1000000;
        getLogger().info(prefix + ChatColor.GREEN + "Start Completed! (took " + duration + "ms)");
    }
    public void onDisable() {
        Logs.info("****END OF LOGS ENDING DATE: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "****");
        try {
            getLogger().severe(prefix + ChatColor.GREEN + "Closing Storage....");
            if (hikari!=null && !hikari.isClosed())
                hikari.close();
        } catch(Exception e) {
            getLogger().severe(prefix + ChatColor.RED + "Could not Close Storage!");
            e.printStackTrace();
        }
    }
    public void loadConfig() {
        StaffHide = new File(getDataFolder(), "/data/staffhide.yml");
        Reputation = new File(getDataFolder(), "/data/reputation.yml");
        Punishments = new File(getDataFolder(), "punishments.yml");
        Punisher = new File(getDataFolder(), "config.yml");
        Cooldowns = new File(getDataFolder(), "/data/cooldowns.yml");
        File logsdir = new File(getDataFolder() + "/logs/");
        File datadir = new File(getDataFolder() + "/data/");
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }
            if (!logsdir.exists()){
                logsdir.mkdir();
            }
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
            FileHandler LogsHandler = new FileHandler(getDataFolder() + "/logs/" + format.format(Calendar.getInstance().getTime()) + ".log");
            LogsHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    SimpleDateFormat logTime = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
                    Calendar cal = new GregorianCalendar();
                    cal.setTimeInMillis(record.getMillis());
                    return "\n" + record.getLevel() + " "
                            + logTime.format(cal.getTime())
                            + ": "
                            + record.getMessage();
                }
            });
            Logs.setUseParentHandlers(false);
            Logs.addHandler(LogsHandler);
            if (!datadir.exists()){
                datadir.mkdir();
            }
            if (!Punishments.exists()) {
                Punishments.createNewFile();
                saveDefaultPunishments();
            }
            if (!Punisher.exists()) {
                Punisher.createNewFile();
                saveDefaultConfig();
            }
            if (!Cooldowns.exists()){
                Cooldowns.createNewFile();
            }
            if (!Reputation.exists()){
                Reputation.createNewFile();
            }
            if (!StaffHide.exists()){
                StaffHide.createNewFile();
            }
            PunishmentsConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(Punishments);
            PunisherConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(Punisher);
            CooldownsConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(Cooldowns);
            RepStorage = ConfigurationProvider.getProvider(YamlConfiguration.class).load(Reputation);
            StaffHidden = ConfigurationProvider.getProvider(YamlConfiguration.class).load(StaffHide);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void saveConfig() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(PunishmentsConfig, Punishments);
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(PunisherConfig, Punisher);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void saveCooldowns() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(CooldownsConfig, Cooldowns);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void saveRep() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(RepStorage, Reputation);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void saveDefaultPunishments() {
        try {
            InputStream is = getResourceAsStream("punishments.yml");
            OutputStream os = new FileOutputStream(Punishments);
            ByteStreams.copy(is, os);
            is.close();
            os.close();
        } catch (IOException e) {
            throw new RuntimeException("Unable to create configuration file", e);
        }
    }
    private void saveDefaultConfig() {
        try {
            InputStream is = getResourceAsStream("BungeeConfig.yml");
            OutputStream os = new FileOutputStream(Punisher);
            ByteStreams.copy(is, os);
            is.close();
            os.close();
        } catch (IOException e) {
            throw new RuntimeException("Unable to create configuration file", e);
        }
    }
    public static void saveStaffHide(){
        try{
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(StaffHidden, StaffHide);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private void openConnection() throws SQLException {
        if (connection != null && !connection.isClosed() || hikari == null)
            return;
        connection = hikari.getConnection();
        getLogger().info(prefix + ChatColor.GREEN + "MYSQL Connected to server: " + host + ":" + port + " with user: " + username + "!");
    }
    public HikariDataSource getHikari(){
        return hikari;
    }
    public void setupmysql(){
        try {
            getLogger().info(prefix + ChatColor.GREEN + "Setting up MYSQL...");
            String createdb = "CREATE DATABASE IF NOT EXISTS " + database;
            String bans = "CREATE TABLE IF NOT EXISTS `" + database + "`.`bans` ( `UUID` VARCHAR(32) NOT NULL , `Name` VARCHAR(16) NOT NULL , `Length` BIGINT NOT NULL , " +
                    "`Reason` TEXT NOT NULL, `Punisher` VARCHAR(16) NOT NULL ) ENGINE = InnoDB CHARSET=utf8 COLLATE utf8_general_ci;";
            String mutes = "CREATE TABLE IF NOT EXISTS `" + database + "`.`mutes` ( `UUID` VARCHAR(32) NOT NULL , `Name` VARCHAR(16) NOT NULL , `Length` BIGINT NOT NULL , " +
                    "`Reason` TEXT NOT NULL, `Punisher` VARCHAR(16) NOT NULL ) ENGINE = InnoDB CHARSET=utf8 COLLATE utf8_general_ci;";
            String history = "CREATE TABLE IF NOT EXISTS`" + database + "`.`history` ( `UUID` VARCHAR(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ," +
                    " `Minor Chat Offence` TINYINT NOT NULL DEFAULT '0' , `Major Chat Offence` TINYINT NOT NULL DEFAULT '0' , `DDoS/DoX Threats` TINYINT NOT NULL DEFAULT '0' ," +
                    " `Inappropriate Link` TINYINT NOT NULL DEFAULT '0' , `Scamming` TINYINT NOT NULL DEFAULT '0' , `X-Raying` TINYINT NOT NULL DEFAULT '0' ," +
                    " `AutoClicker(non PvP)` TINYINT NOT NULL DEFAULT '0' , `Fly/Speed Hacking` TINYINT NOT NULL DEFAULT '0' , `Malicious PvP Hacks` TINYINT NOT NULL DEFAULT '0' ," +
                    " `Disallowed Mods` TINYINT NOT NULL DEFAULT '0' , `Server Advertisment` TINYINT NOT NULL DEFAULT '0' , `Greifing` TINYINT NOT NULL DEFAULT '0' ," +
                    " `Exploiting` TINYINT NOT NULL DEFAULT '0' , `Tpa-Trapping` TINYINT NOT NULL DEFAULT '0' , `Impersonation` TINYINT NOT NULL DEFAULT '0' , `Manual Punishments` TINYINT NOT NULL DEFAULT '0' ) ENGINE = InnoDB;";
            String staffhist = "CREATE TABLE IF NOT EXISTS`" + database + "`.`staffhistory` ( `UUID` VARCHAR(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ," +
                    " `Minor Chat Offence` TINYINT NOT NULL DEFAULT '0' , `Major Chat Offence` TINYINT NOT NULL DEFAULT '0' , `DDoS/DoX Threats` TINYINT NOT NULL DEFAULT '0' ," +
                    " `Inappropriate Link` TINYINT NOT NULL DEFAULT '0' , `Scamming` TINYINT NOT NULL DEFAULT '0' , `X-Raying` TINYINT NOT NULL DEFAULT '0' ," +
                    " `AutoClicker(non PvP)` TINYINT NOT NULL DEFAULT '0' , `Fly/Speed Hacking` TINYINT NOT NULL DEFAULT '0' , `Malicious PvP Hacks` TINYINT NOT NULL DEFAULT '0' ," +
                    " `Disallowed Mods` TINYINT NOT NULL DEFAULT '0' , `Server Advertisment` TINYINT NOT NULL DEFAULT '0' , `Greifing` TINYINT NOT NULL DEFAULT '0' ," +
                    " `Exploiting` TINYINT NOT NULL DEFAULT '0' , `Tpa-Trapping` TINYINT NOT NULL DEFAULT '0' , `Impersonation` TINYINT NOT NULL DEFAULT '0' , `Manual Punishments` TINYINT NOT NULL DEFAULT '0' ) ENGINE = InnoDB;";
            String ip = "CREATE TABLE IF NOT EXISTS`" + database + "`.`iplist` ( `UUID` VARCHAR(32) NOT NULL , `ip` VARCHAR(32) NOT NULL ) ENGINE = InnoDB;";
            String usedb = "USE " + database;
            PreparedStatement stmt = connection.prepareStatement(createdb);
            PreparedStatement stmt1 = connection.prepareStatement(bans);
            PreparedStatement stmt2 = connection.prepareStatement(mutes);
            PreparedStatement stmt3 = connection.prepareStatement(history);
            PreparedStatement stmt4 = connection.prepareStatement(staffhist);
            PreparedStatement stmt5 = connection.prepareStatement(ip);
            PreparedStatement stmt6 = connection.prepareStatement(usedb);
            stmt.executeUpdate();
            stmt.close();
            getLogger().info(prefix + ChatColor.GREEN + database + " Database Created!");
            stmt1.executeUpdate();
            stmt1.close();
            stmt2.executeUpdate();
            stmt2.close();
            stmt3.executeUpdate();
            stmt3.close();
            stmt4.executeUpdate();
            stmt4.close();
            stmt5.executeUpdate();
            getLogger().info(prefix + ChatColor.GREEN + "Tables Created!");
            stmt6.executeUpdate();
            stmt6.close();
            getLogger().info(prefix + ChatColor.GREEN + "Database Set to: " + database);
            getLogger().info(prefix + ChatColor.GREEN + "MYSQL setup!");
            getLogger().info("");
            getLogger().info(prefix + ChatColor.GREEN + "SQL Connection is now online!");
            getLogger().info("");
        }catch (SQLException e){
            getLogger().severe(prefix + ChatColor.RED + "Could not Setup MYSQL!!");
            mysqlfail(e);
        }
    }
    public void mysqlfail(Exception e){
        getLogger().severe(prefix + e);
        getProxy().getPluginManager().unregisterListeners(this);
        getProxy().getPluginManager().unregisterCommands(this);
        getLogger().severe(prefix + ChatColor.RED + "Plugin Disabled!");
    }
    private void testConnection () {
        getProxy().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                if (!testConnectionManual()){
                    getProxy().getPluginManager().unregisterListeners(getInstance());
                    getProxy().getPluginManager().unregisterCommands(getInstance());
                    getLogger().severe(prefix + ChatColor.RED + "Plugin disabled!");
                }
            }
        }, 1, 60, TimeUnit.MINUTES);
    }
    public boolean testConnectionManual(){
        try {
            if (connection != null && !connection.isClosed()) {
                if (PunisherConfig.getBoolean("MySQL.debugMode"))
                getLogger().info(prefix + ChatColor.GREEN + "MYSQL Connection is still open, Testing validity of connection....");
                try {
                    if (connection.isValid(10)) {
                        if (PunisherConfig.getBoolean("MySQL.debugMode"))
                        getLogger().info(prefix + ChatColor.GREEN + "Connection Valid, no need to reset!");
                        return true;
                    } else {
                        if (PunisherConfig.getBoolean("MySQL.debugMode"))
                        getLogger().info(prefix + ChatColor.RED + "Connection Invalid after: 60 minutes, resetting connection...");
                        connection.close();
                        openConnection();
                        setupmysql();
                        return true;
                    }
                }catch (SQLException e){
                    try {
                        if (PunisherConfig.getBoolean("MySQL.debugMode"))
                        getLogger().info(prefix + ChatColor.RED + "Connection Invalid, resetting connection...");
                        connection.close();
                        openConnection();
                        setupmysql();
                        return true;
                    }catch (Exception ex){
                        if (PunisherConfig.getBoolean("MySQL.debugMode"))
                        getLogger().info(prefix + ChatColor.RED + "MYSQL reconnect failed twice!");
                        mysqlfail(ex);
                        return false;
                    }
                }
            }else{
                if (PunisherConfig.getBoolean("MySQL.debugMode")) {
                    getLogger().info(prefix + ChatColor.RED + "SQL Connection not connected after: 60 minutes, reconnecting!");
                    getLogger().info(prefix + ChatColor.GREEN + "Establishing New MYSQL connection...");
                }
                host = PunisherConfig.getString("MySQL.host");
                database = PunisherConfig.getString("MySQL.database");
                username = PunisherConfig.getString("MySQL.username");
                password = PunisherConfig.getString("MySQL.password");
                port = PunisherConfig.getInt("MySQL.port");
                extraArguments = PunisherConfig.getString("MySQL.extraArguments");
                try {
                    openConnection();
                } catch (SQLException e) {
                    getLogger().severe(prefix + ChatColor.RED + "MYSQL Connection failed!!! (SQLException)");
                    mysqlfail(e);
                    return false;
                }
                //setup mysql
                setupmysql();
                return true;
            }
        }catch (Exception e) {
            if (PunisherConfig.getBoolean("MySQL.debugMode"))
            getLogger().severe(prefix + ChatColor.RED + "MYSQL Connection test failed!!!");
            try {
                if (PunisherConfig.getBoolean("MySQL.debugMode")) {
                    getLogger().info(prefix + ChatColor.RED + "SQL Connection not stable, resetting!");
                    getLogger().info(prefix + ChatColor.GREEN + "Closing MYSQL connection...");
                }
                connection.close();
                if (PunisherConfig.getBoolean("MySQL.debugMode")) {
                    getLogger().info(prefix + ChatColor.GREEN + "Connection closed!");
                    getLogger().info(prefix + ChatColor.GREEN + "Establishing New MYSQL connection...");
                }
                host = PunisherConfig.getString("host");
                database = PunisherConfig.getString("database");
                username = PunisherConfig.getString("username");
                password = PunisherConfig.getString("password");
                port = PunisherConfig.getInt("port");
                try {
                    openConnection();
                } catch (SQLException ex) {
                    if (PunisherConfig.getBoolean("MySQL.debugMode"))
                    getLogger().severe(prefix + ChatColor.RED + "MYSQL Connection failed!!! (SQLException)");
                    mysqlfail(ex);
                    return false;
                }
                //setup mysql
                setupmysql();
                return true;
            } catch (Exception ex) {
                if (PunisherConfig.getBoolean("MySQL.debugMode"))
                getLogger().info(prefix + ChatColor.RED + "MYSQL reconnect failed twice!");
                mysqlfail(ex);
                return false;
            }
        }
    }
}