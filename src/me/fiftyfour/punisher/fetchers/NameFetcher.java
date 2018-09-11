package me.fiftyfour.punisher.fetchers;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;

public class NameFetcher {
    private static HashMap<String, String> names = new HashMap<>();
    public static String getName(String uuid) {
            uuid = uuid.replace("-", "");
            if (names.containsKey(uuid))
                return names.get(uuid);

            String output = callURL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);

            StringBuilder result = new StringBuilder();

            int i = 0;

            while (i < 200) {
                if (output.length() == 0) {
                    if (names.containsKey(uuid))
                        return names.get(uuid);
                    return "null";

                }

                if ((String.valueOf(output.charAt(i)).equalsIgnoreCase("n")) && (String.valueOf(output.charAt(i + 1)).equalsIgnoreCase("a")) && (String.valueOf(output.charAt(i + 2)).equalsIgnoreCase("m")) && (String.valueOf(output.charAt(i + 3)).equalsIgnoreCase("e"))) {

                    int k = i + 7;

                    while (k < 100) {

                        if (!String.valueOf(output.charAt(k)).equalsIgnoreCase("\"")) {

                            result.append(String.valueOf(output.charAt(k)));

                        } else {
                            break;
                        }

                        k++;
                    }

                    break;
                }

                i++;
            }
            names.put(uuid, result.toString());
            return result.toString();
    }

    private static String callURL(String URL) {
        StringBuilder sb = new StringBuilder();
        URLConnection urlConn = null;
        InputStreamReader in = null;
        try {
            URL url = new URL(URL);
            urlConn = url.openConnection();

            if (urlConn != null) urlConn.setReadTimeout(60 * 1000);

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
        } catch (IOException e) {

        }catch (Exception e){
            e.printStackTrace();
        }
        return sb.toString();
    }
}