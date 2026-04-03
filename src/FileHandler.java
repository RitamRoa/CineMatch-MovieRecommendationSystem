import java.io.*;
import java.util.*;

/**
 * FileHandler — handles saving and loading system data.
 * Demonstrates FILE HANDLING with FileWriter / FileReader (BONUS FEATURE).
 */
public class FileHandler {

    private static final String DATA_FILE = "movie_data.txt";

    /**
     * Save all users, movies, and ratings to a text file.
     */
    public static void saveData(RecommendationEngine engine) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {

            // Save Users
            for (User user : engine.getAllUsers()) {
                writer.write("USER|" + user.getUserId() + "|" + user.getName());
                writer.newLine();
            }

            // Save Movies
            for (Movie movie : engine.getAllMovies()) {
                writer.write("MOVIE|" + movie.getId() + "|" + movie.getName() + "|" + movie.getGenre());
                writer.newLine();
            }

            // Save Ratings
            for (User user : engine.getAllUsers()) {
                for (Map.Entry<String, Double> entry : user.getRatings().entrySet()) {
                    writer.write("RATING|" + user.getUserId() + "|"
                            + entry.getKey() + "|" + entry.getValue());
                    writer.newLine();
                }
            }

            System.out.println("  [✓] Data saved to '" + DATA_FILE + "' successfully.");

        } catch (IOException e) {
            System.out.println("  [!] Error saving data: " + e.getMessage());
        }
    }

    /**
     * Load data from file into the engine.
     * Returns true if file was found and loaded.
     */
    public static boolean loadData(RecommendationEngine engine) {
        File file = new File(DATA_FILE);
        if (!file.exists()) return false;

        int loaded = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                switch (parts[0]) {
                    case "USER":
                        engine.addUser(parts[1], parts[2]);
                        break;
                    case "MOVIE":
                        engine.addMovie(parts[1], parts[2], parts[3]);
                        break;
                    case "RATING":
                        engine.rateMovie(parts[1], parts[2], Double.parseDouble(parts[3]));
                        break;
                }
                loaded++;
            }
            System.out.println("  [✓] Loaded " + loaded + " records from '" + DATA_FILE + "'.");
            return true;

        } catch (IOException | NumberFormatException e) {
            System.out.println("  [!] Error loading data: " + e.getMessage());
            return false;
        }
    }
}
