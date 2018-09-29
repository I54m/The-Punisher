package me.fiftyfour.punisher.fetchers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Callable;

public class UUIDFetcher implements Callable<String> {

    private String name;
    private HashMap<String, String> uuidCache = new HashMap<>();

    public void fetch(String name) {
        this.name = name;
    }

    @Override
    public String call() throws Exception {
        if (uuidCache.containsKey(name)){
            return uuidCache.get(name);
        }
        StringBuilder sb = new StringBuilder();
        URLConnection urlConn;
        InputStreamReader in = null;
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + this.name);
        urlConn = url.openConnection();
        if (urlConn != null) urlConn.setReadTimeout(5000);
        if (urlConn != null && urlConn.getInputStream() != null) {
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
        StringBuilder result = new StringBuilder();
        int k = 7;
        while (k < 100) {
            if (!String.valueOf(sb.toString().charAt(k)).equalsIgnoreCase("\"")) {
                result.append(String.valueOf(sb.toString().charAt(k)));
            } else {
                break;
            }
            k++;
        }
        uuidCache.put(name, result.toString());
        return result.toString();
    }
    public static UUID formatUUID(String uuid) {
        StringBuffer sb = new StringBuffer(uuid);
        sb.insert(8, "-");
        sb = new StringBuffer(sb.toString());
        sb.insert(13, "-");
        sb = new StringBuffer(sb.toString());
        sb.insert(18, "-");
        sb = new StringBuffer(sb.toString());
        sb.insert(23, "-");
        return UUID.fromString(sb.toString());
    }
}