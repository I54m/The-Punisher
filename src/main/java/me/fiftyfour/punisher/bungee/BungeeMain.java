package me.fiftyfour.punisher.bungee;

import com.zaxxer.hikari.HikariDataSource;
import me.fiftyfour.punisher.bungee.chats.AdminChat;
import me.fiftyfour.punisher.bungee.chats.StaffChat;
import me.fiftyfour.punisher.bungee.commands.*;
import me.fiftyfour.punisher.bungee.discordbot.DiscordMain;
import me.fiftyfour.punisher.bungee.listeners.*;
import me.fiftyfour.punisher.bungee.managers.PunishmentManager;
import me.fiftyfour.punisher.universal.systems.UpdateChecker;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class BungeeMain extends Plugin implements Listener {

    public static File Reputation, Punishments, PlayerInfo, DiscordIntegration;
    public static Configuration PunishmentsConfig, PunisherConfig, CooldownsConfig, RepStorage, StaffHidden, InfoConfig;
    public static Logger Logs = Logger.getLogger("Punisher Logs");
    public static boolean update;
    public static String database;
    private static BungeeMain instance;
    private static File Cooldowns, StaffHide;
    private static FileHandler LogsHandler;
    public String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;
    public Connection connection;
    private File Punisher;
    private String host, username, password, extraArguments;
    private int port;
    private HikariDataSource hikari;
    private PunishmentManager punManager = PunishmentManager.getInstance();

    public static BungeeMain getInstance() {
        return instance;
    }

    private static void setInstance(BungeeMain instance) {
        BungeeMain.instance = instance;
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

    public static void saveInfo() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(InfoConfig, PlayerInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveStaffHide() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(StaffHidden, StaffHide);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        try {
            long startTime = System.nanoTime();
            //set instance
            setInstance(this);
            if (instance == null || getInstance() == null) throw new NullPointerException("Plugin Instance is null!");
            //register commands
            getLogger().info(prefix + ChatColor.GREEN + "Registering Commands...");
            getProxy().getPluginManager().registerCommand(this, new AdminCommands());
            getProxy().getPluginManager().registerCommand(this, new AltsCommand());
            getProxy().getPluginManager().registerCommand(this, new BanCommand());
            getProxy().getPluginManager().registerCommand(this, new GlobalCommand());
            getProxy().getPluginManager().registerCommand(this, new HistoryCommand());
            getProxy().getPluginManager().registerCommand(this, new IpCommand());
            getProxy().getPluginManager().registerCommand(this, new IpHistCommand());
            getProxy().getPluginManager().registerCommand(this, new KickAllCommand());
            getProxy().getPluginManager().registerCommand(this, new KickCommand());
            getProxy().getPluginManager().registerCommand(this, new MuteCommand());
            getProxy().getPluginManager().registerCommand(this, new NotesCommand());
            getProxy().getPluginManager().registerCommand(this, new PingCommand());
            getProxy().getPluginManager().registerCommand(this, new PlayerInfoCommand());
            getProxy().getPluginManager().registerCommand(this, new PunHelpCommand());
            getProxy().getPluginManager().registerCommand(this, new ReportCommand());
            getProxy().getPluginManager().registerCommand(this, new ReputationCommand());
            getProxy().getPluginManager().registerCommand(this, new SeenCommand());
            getProxy().getPluginManager().registerCommand(this, new StaffCommand());
            getProxy().getPluginManager().registerCommand(this, new StaffHideCommand());
            getProxy().getPluginManager().registerCommand(this, new StaffhistoryCommand());
            getProxy().getPluginManager().registerCommand(this, new SuperBroadcast());
            getProxy().getPluginManager().registerCommand(this, new UnbanCommand());
            getProxy().getPluginManager().registerCommand(this, new UnmuteCommand());
            getProxy().getPluginManager().registerCommand(this, new UnpunishCommand());
            getProxy().getPluginManager().registerCommand(this, new viewRepCommand());
            getProxy().getPluginManager().registerCommand(this, new WarnCommand());
            //register chat commands
            getProxy().getPluginManager().registerCommand(this, new StaffChat());
            getProxy().getPluginManager().registerCommand(this, new AdminChat());
            //register event listeners
            getLogger().info(prefix + ChatColor.GREEN + "Registering Listeners...");
            getProxy().getPluginManager().registerListener(this, new PlayerChat());
            getProxy().getPluginManager().registerListener(this, new PlayerLogin());
            getProxy().getPluginManager().registerListener(this, new PluginMessage());
            getProxy().getPluginManager().registerListener(this, new TabComplete());
            getProxy().getPluginManager().registerListener(this, new ServerConnect());
            getProxy().getPluginManager().registerListener(this, new PlayerDisconnect());
            //register legacy punishment calculation system listeners
//        getProxy().getPluginManager().registerListener(this, new PunishmentCalc());
            //check for dependencies
            if (getProxy().getPluginManager().getPlugin("LuckPerms") == null) {
                getLogger().warning(prefix + ChatColor.RED + "Luck Perms not detected, Plugin has been Disabled!");
                getProxy().getPluginManager().unregisterCommands(this);
                getProxy().getPluginManager().unregisterListeners(this);
                return;
            }
            //load all configs and create them if they don't exist
            getLogger().info(prefix + ChatColor.GREEN + "Loading Configs...");
            loadConfig();
            //make sure info config has last join id set
            if (!InfoConfig.contains("lastjoinid")) {
                InfoConfig.set("lastjoinid", 0);
                saveInfo();
            }
            ServerConnect.lastJoinId = 0;
            ServerConnect.lastJoinId = BungeeMain.InfoConfig.getInt("lastjoinid");
            //check for update
            getLogger().info(prefix + ChatColor.GREEN + "Checking for updates...");
            if (!this.getDescription().getVersion().contains("BETA") && !this.getDescription().getVersion().contains("PRE-RELEASE")
                    && !this.getDescription().getVersion().contains("DEV-BUILD") && !this.getDescription().getVersion().contains("SNAPSHOT")
                    && !this.getDescription().getVersion().contains("LEGACY")) {
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
                } catch (Exception e) {
                    getLogger().severe(ChatColor.RED + e.getMessage());
                }
            } else if (this.getDescription().getVersion().contains("LEGACY")) {
                getLogger().info(prefix + ChatColor.GREEN + "You are running a LEGACY version of The Punisher");
                getLogger().info(prefix + ChatColor.GREEN + "This version is no longer updated with new features and ONLY MAJOR BUGS WILL BE FIXED!!");
                getLogger().info(prefix + ChatColor.GREEN + "It is recommended that you update your server to 1.13.2 to have new features.");
                getLogger().info(prefix + ChatColor.GREEN + "Update checking is not needed in these versions");
                update = false;
            } else {
                getLogger().info(prefix + ChatColor.GREEN + "You are running a PRE-RELEASE version of The Punisher");
                getLogger().info(prefix + ChatColor.GREEN + "Update checking is not needed in these versions");
                update = false;
            }
            //check config version
            if (!PunisherConfig.getString("Configversion").equals(this.getDescription().getVersion())) {
                getLogger().warning(prefix + ChatColor.RED + "Old config.yml detected!");
                getLogger().warning(prefix + ChatColor.RED + "Renaming old config to old_config.yml!");
                File newfile = new File(getDataFolder(), "old_config.yml");
                try {
                    Files.move(Punisher.toPath(), newfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Punisher.createNewFile();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                saveDefaultConfig();
                PunisherConfig.set("Configversion", this.getDescription().getVersion());
                saveConfig();
            }
            //check if rep on vote should be enabled
            if ((getProxy().getPluginManager().getPlugin("NuVotifier") != null || getProxy().getPluginManager().getPlugin("Votifier") != null) && PunisherConfig.getBoolean("Voting.addRepOnVote")) {
                getLogger().info(prefix + ChatColor.GREEN + "Enabled Rep on Vote feature!");
                getProxy().getPluginManager().registerListener(this, new PlayerVote());
            }
            //check if discord integration is enabled
            if (getProxy().getPluginManager().getPlugin("AlonsoJDA") == null && PunisherConfig.getBoolean("DiscordIntegration.Enabled")) {
                getLogger().warning(prefix + ChatColor.RED + "Discord integration is enabled but couldn't find AlonsoJDA!");
                getLogger().warning(prefix + ChatColor.RED + "If you want to use Discord integration please make sure AlonsoJDA is enabled!");
            } else if (getProxy().getPluginManager().getPlugin("AlonsoJDA") != null && PunisherConfig.getBoolean("DiscordIntegration.Enabled"))
                DiscordMain.startBot();
            //check if help command should be enabled
            if (PunisherConfig.getBoolean("helpCommand.Enabled")) {
                getProxy().getPluginManager().registerCommand(this, new HelpCommand());
            }
            //start logging to logs file
            getLogger().info(prefix + ChatColor.GREEN + "Beginning Logging...");
            Logs.info("****START OF LOGS BEGINNING DATE: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "****");
            //register plugin channels
            getProxy().registerChannel("punisher:main");
            getProxy().registerChannel("punisher:minor");
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
                throw new Exception("Mysql connection failed", e);
            }
            //setup mysql
            setupmysql();
            testConnection();
            //start caching punishments
            getLogger().info(prefix + ChatColor.GREEN + "Beginning punishment caching...");
            punManager.startCaching();
            long duration = (System.nanoTime() - startTime) / 1000000;
            getLogger().info(prefix + ChatColor.GREEN + "Successfully enabled The Punisher v" + this.getDescription().getVersion() + " By 54mpenguin (took " + duration + "ms)");
        } catch (Exception e) {
            onDisable();
            getLogger().severe(prefix + ChatColor.RED + "Could not enable The Punisher v" + this.getDescription().getVersion() + "!!");
            if (e.getCause() != null)
                getLogger().severe(prefix + ChatColor.RED + "Error cause: " + e.getCause());
            if (e.getMessage() != null)
                getLogger().severe(prefix + ChatColor.RED + "Error message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onDisable() {
        if (punManager.cacheTask != null)
            punManager.cacheTask.cancel();
        ProxyServer.getInstance().getPluginManager().unregisterListeners(this);
        ProxyServer.getInstance().getPluginManager().unregisterCommands(this);
        DiscordMain.shutdown();
        Logs.info("****END OF LOGS ENDING DATE: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "****");
        LogsHandler.close();
        Logs.removeHandler(LogsHandler);
        try {
            if (hikari != null && !hikari.isClosed()) {
                getLogger().info(prefix + ChatColor.GREEN + "Closing Storage....");
                hikari.close();
                connection = null;
                hikari = null;
                getLogger().info(prefix + ChatColor.GREEN + "Storage Closed");
            }
        } catch (Exception e) {
            getLogger().severe(prefix + ChatColor.RED + "Could not Close Storage!");
            e.printStackTrace();
        }
    }

    public void loadConfig() throws Exception {
        Punishments = new File(getDataFolder(), "punishments.yml");
        Punisher = new File(getDataFolder(), "config.yml");
        StaffHide = new File(getDataFolder(), "/data/staffhide.yml");
        Reputation = new File(getDataFolder(), "/data/reputation.yml");
        Cooldowns = new File(getDataFolder(), "/data/cooldowns.yml");
        PlayerInfo = new File(getDataFolder(), "/data/playerinfo.yml");
        DiscordIntegration = new File(getDataFolder(), "/data/discordintegration.yml");
        File logsdir = new File(getDataFolder() + "/logs/");
        File datadir = new File(getDataFolder() + "/data/");
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        if (!logsdir.exists()) {
            logsdir.mkdir();
        }
        if (!datadir.exists()) {
            datadir.mkdir();
        }
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
        LogsHandler = new FileHandler(getDataFolder() + "/logs/" + format.format(Calendar.getInstance().getTime()) + ".log");
        LogsHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                SimpleDateFormat logTime = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
                Calendar cal = new GregorianCalendar();
                cal.setTimeInMillis(record.getMillis());
                if (record.getLevel().equals(Level.SEVERE)) {
                    ProxyServer.getInstance().getLogger().warning(" ");
                    ProxyServer.getInstance().getLogger().warning(prefix + ChatColor.RED + "An error was detected and logged to log file!");
                    ProxyServer.getInstance().getLogger().warning(" ");
                }
                return "\n" + record.getLevel() + " "
                        + logTime.format(cal.getTime())
                        + ": "
                        + record.getMessage();
            }
        });
        Logs.setUseParentHandlers(false);
        Logs.addHandler(LogsHandler);
        if (!Punishments.exists()) {
            Punishments.createNewFile();
            saveDefaultPunishments();
        }
        if (!Punisher.exists()) {
            Punisher.createNewFile();
            saveDefaultConfig();
        }
        if (!Cooldowns.exists()) {
            Cooldowns.createNewFile();
        }
        if (!Reputation.exists()) {
            Reputation.createNewFile();
        }
        if (!PlayerInfo.exists()) {
            PlayerInfo.createNewFile();
        }
        if (!DiscordIntegration.exists()) {
            DiscordIntegration.createNewFile();
        }
        if (!StaffHide.exists()) {
            StaffHide.createNewFile();
        }
        try {
            PunishmentsConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(Punishments);
        } catch (YAMLException YAMLE) {
            getLogger().severe(prefix + ChatColor.RED + "Error: Could not load punishments config!");
            getLogger().severe(prefix + ChatColor.RED + "Error message: " + YAMLE.getMessage());
            throw new Exception("Could not load punishments config!", YAMLE);
        }
        try {
            PunisherConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(Punisher);
        } catch (YAMLException YAMLE) {
            getLogger().severe(prefix + ChatColor.RED + "Error: Could not load main config!");
            getLogger().severe(prefix + ChatColor.RED + "Error message: " + YAMLE.getMessage());
            throw new Exception("Could not load main config!", YAMLE);
        }
        try {
            InfoConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(PlayerInfo);
            CooldownsConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(Cooldowns);
            StaffHidden = ConfigurationProvider.getProvider(YamlConfiguration.class).load(StaffHide);
            RepStorage = ConfigurationProvider.getProvider(YamlConfiguration.class).load(Reputation);
        } catch (YAMLException YAMLE) {
            getLogger().severe(prefix + ChatColor.RED + "Error: Could not load data config files!");
            getLogger().severe(prefix + ChatColor.RED + "These configs are not meant to be altered!");
            getLogger().severe(prefix + ChatColor.RED + "If you have not altered them this may be a bug!");
            getLogger().severe(prefix + ChatColor.RED + "Error message: " + YAMLE.getMessage());
            throw new Exception("Could not load data config files!", YAMLE);
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

    public void saveDefaultPunishments() {
        try {
            Files.copy(getResourceAsStream("punishments.yml"), Punishments.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create configuration file", e);
        }
    }

    private void saveDefaultConfig() {
        try {
            Files.copy(getResourceAsStream("BungeeConfig.yml"), Punisher.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create configuration file", e);
        }
    }

    private void openConnection() throws SQLException {
        if (connection != null && !connection.isClosed() || hikari == null)
            return;
        connection = hikari.getConnection();
        getLogger().info(prefix + ChatColor.GREEN + "MYSQL Connected to server: " + host + ":" + port + " with user: " + username + "!");
    }

    public HikariDataSource getDataSource() {
        return hikari;
    }

    private void setupmysql() {
        try {
            getLogger().info(prefix + ChatColor.GREEN + "Setting up MYSQL...");
            String createdb = "CREATE DATABASE IF NOT EXISTS " + database;
            String bans = "CREATE TABLE IF NOT EXISTS `" + database + "`.`bans` ( `UUID` VARCHAR(32) NOT NULL , `Name` VARCHAR(16) NOT NULL , `Length` BIGINT NOT NULL , " +
                    "`Reason` TEXT NOT NULL, `Message` TEXT NOT NULL, `Punisher` VARCHAR(32) NOT NULL ) ENGINE = InnoDB CHARSET=utf8 COLLATE utf8_general_ci;";
            String mutes = "CREATE TABLE IF NOT EXISTS `" + database + "`.`mutes` ( `UUID` VARCHAR(32) NOT NULL , `Name` VARCHAR(16) NOT NULL , `Length` BIGINT NOT NULL , " +
                    "`Reason` TEXT NOT NULL, `Message` TEXT NOT NULL, `Punisher` VARCHAR(32) NOT NULL ) ENGINE = InnoDB CHARSET=utf8 COLLATE utf8_general_ci;";
            String history = "CREATE TABLE IF NOT EXISTS`" + database + "`.`history` ( `UUID` VARCHAR(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ," +
                    " `Minor_Chat_Offence` TINYINT NOT NULL DEFAULT '0' , `Major_Chat_Offence` TINYINT NOT NULL DEFAULT '0' , `DDoS_DoX_Threats` TINYINT NOT NULL DEFAULT '0' ," +
                    " `Inappropriate_Link` TINYINT NOT NULL DEFAULT '0' , `Scamming` TINYINT NOT NULL DEFAULT '0' , `X_Raying` TINYINT NOT NULL DEFAULT '0' ," +
                    " `AutoClicker` TINYINT NOT NULL DEFAULT '0' , `Fly_Speed_Hacking` TINYINT NOT NULL DEFAULT '0' , `Malicious_PvP_Hacks` TINYINT NOT NULL DEFAULT '0' ," +
                    " `Disallowed_Mods` TINYINT NOT NULL DEFAULT '0' , `Server_Advertisment` TINYINT NOT NULL DEFAULT '0' , `Greifing` TINYINT NOT NULL DEFAULT '0' ," +
                    " `Exploiting` TINYINT NOT NULL DEFAULT '0' , `Tpa_Trapping` TINYINT NOT NULL DEFAULT '0' , `Impersonation` TINYINT NOT NULL DEFAULT '0' , `Manual_Punishments` TINYINT NOT NULL DEFAULT '0' ) ENGINE = InnoDB;";
            String staffhist = "CREATE TABLE IF NOT EXISTS`" + database + "`.`staffhistory` ( `UUID` VARCHAR(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ," +
                    " `Minor_Chat_Offence` TINYINT NOT NULL DEFAULT '0' , `Major_Chat_Offence` TINYINT NOT NULL DEFAULT '0' , `DDoS_DoX_Threats` TINYINT NOT NULL DEFAULT '0' ," +
                    " `Inappropriate_Link` TINYINT NOT NULL DEFAULT '0' , `Scamming` TINYINT NOT NULL DEFAULT '0' , `X_Raying` TINYINT NOT NULL DEFAULT '0' ," +
                    " `AutoClicker` TINYINT NOT NULL DEFAULT '0' , `Fly_Speed_Hacking` TINYINT NOT NULL DEFAULT '0' , `Malicious_PvP_Hacks` TINYINT NOT NULL DEFAULT '0' ," +
                    " `Disallowed_Mods` TINYINT NOT NULL DEFAULT '0' , `Server_Advertisment` TINYINT NOT NULL DEFAULT '0' , `Greifing` TINYINT NOT NULL DEFAULT '0' ," +
                    " `Exploiting` TINYINT NOT NULL DEFAULT '0' , `Tpa_Trapping` TINYINT NOT NULL DEFAULT '0' , `Impersonation` TINYINT NOT NULL DEFAULT '0' , `Manual_Punishments` TINYINT NOT NULL DEFAULT '0' ) ENGINE = InnoDB;";
            String alt = "CREATE TABLE IF NOT EXISTS`" + database + "`.`altlist` ( `UUID` VARCHAR(32) NOT NULL , `ip` VARCHAR(32) NOT NULL ) ENGINE = InnoDB;";
            String iphist = "CREATE TABLE IF NOT EXISTS`" + database + "`.`iphist` ( `UUID` VARCHAR(32) NOT NULL , `date` BIGINT NOT NULL , `ip` VARCHAR(32) NOT NULL ) ENGINE = InnoDB;";
            String usedb = "USE " + database;
            PreparedStatement stmt = connection.prepareStatement(createdb);
            PreparedStatement stmt1 = connection.prepareStatement(bans);
            PreparedStatement stmt2 = connection.prepareStatement(mutes);
            PreparedStatement stmt3 = connection.prepareStatement(history);
            PreparedStatement stmt4 = connection.prepareStatement(staffhist);
            PreparedStatement stmt5 = connection.prepareStatement(alt);
            PreparedStatement stmt6 = connection.prepareStatement(iphist);
            PreparedStatement stmt7 = connection.prepareStatement(usedb);
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
            stmt5.close();
            String oldalts = "SELECT * \nFROM information_schema.tables\nWHERE table_schema = '" + database + "' \nAND table_name = 'iplist'\nLIMIT 1;";
            PreparedStatement oldstmt = connection.prepareStatement(oldalts);
            ResultSet oldtable = oldstmt.executeQuery();
            if (oldtable.next()) {
                getLogger().info(prefix + ChatColor.RED + "Found old \"iplist\" table, this table was renamed in v1.7.x!");
                getLogger().info(prefix + ChatColor.GREEN + "Importing old table into new one..");
                getProxy().getScheduler().runAsync(this, this::importOldTable);
            }
            oldtable.close();
            oldstmt.close();
            stmt6.executeUpdate();
            stmt6.close();
            getLogger().info(prefix + ChatColor.GREEN + "Tables Created!");
            stmt7.executeUpdate();
            stmt7.close();
            getLogger().info(prefix + ChatColor.GREEN + "Database Set to: " + database);
            getLogger().info(prefix + ChatColor.GREEN + "MYSQL setup!");
            getLogger().info("");
            getLogger().info(prefix + ChatColor.GREEN + "SQL Connection is now online!");
            getLogger().info("");
        } catch (SQLException e) {
            getLogger().severe(prefix + ChatColor.RED + "Could not Setup MYSQL!!");
            mysqlfail(e);
            onDisable();
        }
    }

    private void mysqlfail(Exception e) {
        BungeeMain.Logs.severe("Error Message: " + e.getMessage());
        StringBuilder stacktrace = new StringBuilder();
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            stacktrace.append(stackTraceElement.toString()).append("\n");
        }
        BungeeMain.Logs.severe("Stack Trace: " + stacktrace.toString());
        if (e.getCause() != null)
            BungeeMain.Logs.severe("Error Cause Message: " + e.getCause().getMessage());
        ProxyServer.getInstance().getLogger().warning(" ");
        ProxyServer.getInstance().getLogger().warning(prefix + ChatColor.RED + "An error was encountered and debug info was logged to log file!");
        ProxyServer.getInstance().getLogger().warning(prefix + ChatColor.RED + "Error Message: " + e.getMessage());
        if (e.getCause() != null)
            ProxyServer.getInstance().getLogger().warning(prefix + ChatColor.RED + "Error Cause Message: " + e.getCause().getMessage());
        ProxyServer.getInstance().getLogger().warning(" ");
        e.printStackTrace();
        AdminChat.sendMessage("ERROR ENCOUNTERED: " + e.getMessage());
        AdminChat.sendMessage("This error will be logged! Please inform a dev asap, this plugin may no longer function as intended!");
    }

    private void testConnection() {
        getProxy().getScheduler().schedule(this, () -> {
            if (!testConnectionManual()) mysqlfail(new SQLException("Unable to reestablish MySQL connection!"));
        }, 1, 60, TimeUnit.MINUTES);
    }

    public boolean testConnectionManual() {
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
                } catch (SQLException e) {
                    try {
                        if (PunisherConfig.getBoolean("MySQL.debugMode"))
                            getLogger().info(prefix + ChatColor.RED + "Connection Invalid, resetting connection...");
                        connection.close();
                        openConnection();
                        setupmysql();
                        return true;
                    } catch (Exception ex) {
                        if (PunisherConfig.getBoolean("MySQL.debugMode"))
                            getLogger().info(prefix + ChatColor.RED + "MYSQL reconnect failed twice!");
                        mysqlfail(ex);
                        return false;
                    }
                }
            } else {
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
        } catch (Exception e) {
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

    private void importOldTable() {
        try {
            String sqldb = "USE `" + database + "`;";
            PreparedStatement stmtdb = connection.prepareStatement(sqldb);
            stmtdb.executeUpdate();
            stmtdb.close();
            String truncate = "TRUNCATE ` altlist`;";
            PreparedStatement trunstmt = connection.prepareStatement(truncate);
            trunstmt.executeUpdate();
            trunstmt.close();
            String sql = "SELECT * FROM `iplist`";
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet results = stmt.executeQuery();
            while (results.next()) {
                String sqlupdate = "INSERT INTO `altlist`(`UUID`, `ip`) VALUES ('" + results.getString("uuid") + "','" + results.getString("ip") + "');";
                PreparedStatement stmtupdate = connection.prepareStatement(sqlupdate);
                stmtupdate.executeUpdate();
                stmtupdate.close();
            }
            results.close();
            stmt.close();
            getLogger().info(prefix + ChatColor.GREEN + "Table Importing Complete, Removing old table!");
            String sql1 = "DROP TABLE `iplist`";
            PreparedStatement stmt1 = connection.prepareStatement(sql1);
            stmt1.executeUpdate();
            stmt1.close();
            getLogger().info(prefix + ChatColor.GREEN + "Old Table Removed!");
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

    }
}