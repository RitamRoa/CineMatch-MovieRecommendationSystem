import java.net.*;
import java.io.*;

public class TMDBService {

    // 🔑 Replace with your actual API key
    private static final String API_KEY = "c1e978c26e73603f409556ed3cdc923b";

    public static void getPopularMovies() {
        try {
            // 🔗 API URL
            String urlStr = "https://api.themoviedb.org/3/movie/popular?api_key=" + API_KEY;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            // ⏱ Timeout settings (important)
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            // 📥 Read response
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            // 📦 Convert to string
            String json = response.toString();

            // 🎬 Extract movie titles cleanly
            System.out.println("\n🎬 Popular Movies:\n");

            int count = 0;
            int index = 0;

            while ((index = json.indexOf("\"title\":\"", index)) != -1 && count < 5) {
                index += 9; // move past "title":"
                int end = json.indexOf("\"", index);

                if (end != -1) {
                    String title = json.substring(index, end);
                    System.out.println("👉 " + title);
                    count++;
                }
            }

            System.out.println("\n✅ Movies fetched successfully!\n");

        } catch (Exception e) {
            System.out.println("\n❌ Error fetching movies. Check internet/VPN.\n");
            e.printStackTrace();
        }
    }
}