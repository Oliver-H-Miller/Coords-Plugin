package MichaelCoordsPlugin.tut;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class SheetCommunication {

    private static String scriptUrl = "https://script.google.com/macros/s/AKfycbyqUvh7lIJW6y-Idkm4KMW-n8zSIO8SHDionsiUS1oUUHR6wUoVy_oIsg/exec";

    SheetCommunication(String url) {
        scriptUrl = url;
    }

    public static void sendPostRequest(String body) throws IOException {
        URL url = new URL(scriptUrl);
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("POST"); // PUT is another valid option
        http.setDoOutput(true);
        byte[] out = body.getBytes(StandardCharsets.UTF_8);
        int length = out.length;
        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        http.connect();
        try (OutputStream os = http.getOutputStream()) {
            os.write(out);
        }
    }

    public static String getContents() {
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(scriptUrl);
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }
    public static String getContents(String uuid) {
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(scriptUrl + "?uuid=" + uuid);
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}
