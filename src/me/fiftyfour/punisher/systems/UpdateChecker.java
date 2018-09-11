package me.fiftyfour.punisher.systems;

import me.fiftyfour.punisher.bukkit.BukkitMain;
import me.fiftyfour.punisher.bungee.BungeeMain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

public class UpdateChecker {

    private static String versionString = callURL("https://api.54mpenguin.com/the-punisher/version/");

    public static String getCurrentVersion(){
        StringBuilder result = new StringBuilder();
        readData(versionString, result, 25);
        if (result.toString().equals("null")){
            return null;
        }else
        return result.toString();
    }

    public static boolean check(){
        if (getCurrentVersion() == null)
            return false;
        try {
            Class.forName("net.md_5.bungee.BungeeCord");
            BungeeMain plugin = BungeeMain.getInstance();
            return !getCurrentVersion().equals(plugin.getDescription().getVersion());
        }catch (ClassNotFoundException CNFE){
            BukkitMain plugin = BukkitMain.getInstance();
            return !getCurrentVersion().equals(plugin.getDescription().getVersion());
        }
    }

    public static String getRealeaseDate(){
        StringBuilder result = new StringBuilder();
        readData(versionString, result, 44);
        if (result.toString().equals("null")){
            return null;
        }else
        return result.toString();
    }

    private static void readData(String toRead, StringBuilder result, int start) {
        int i = start;
        if (toRead == null) {
            result.append("null");
            return;
        }
        while (i < 200) {
            if (toRead.length() == 0) {
                result.append("null");
                break;
            }
            if (!String.valueOf(toRead.charAt(i)).equalsIgnoreCase("\"")) {
                result.append(String.valueOf(toRead.charAt(i)));
            } else {
                break;
            }
            i++;
        }
    }

    private static String callURL(String URL) {
        StringBuilder sb = new StringBuilder();
        URLConnection urlConn;
        InputStreamReader in = null;
        try {
            urlConn = new URL(URL).openConnection();
            urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            urlConn.connect();
            urlConn.setReadTimeout(60 * 1000);
            if (urlConn.getInputStream() != null) {
                in = new InputStreamReader(urlConn.getInputStream(), Charset.defaultCharset());
                BufferedReader bufferedReader = new BufferedReader(in);
                if (bufferedReader != null) {
                    int cp;
                    while ((cp = bufferedReader.read()) != -1) {
                        sb.append((char) cp);
                    }
                    bufferedReader.close();
                }
            }
            if (in != null)
                in.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }catch(Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
