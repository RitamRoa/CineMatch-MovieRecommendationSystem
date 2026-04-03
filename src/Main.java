import java.util.*;

public class Main {

    private static final RecommendationEngine engine = new RecommendationEngine();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        printBanner();
        launchFrontend();

        boolean loaded = FileHandler.loadData(engine);
        if (!loaded) {
            System.out.println("  No saved data found. Loading sample data...\n");
            seedSampleData();
        }

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readIntInput("  Enter choice: ");
            try {
                switch (choice) {
                    case 1  -> addUser();
                    case 2  -> addMovie();
                    case 3  -> rateMovie();
                    case 4  -> getRecommendations();
                    case 5  -> listUsers();
                    case 6  -> listMovies();
                    case 7  -> showSimilarityMatrix();
                    case 8  -> FileHandler.saveData(engine);
                    case 9  -> {
                        FileHandler.saveData(engine);
                        System.out.println("\n  Goodbye!\n");
                        running = false;
                    }
                    default -> System.out.println("  [!] Invalid option. Please enter 1-9.");
                }
            } catch (Exception e) {
                System.out.println("  [!] Unexpected error: " + e.getMessage());
            }
        }
        scanner.close();
    }

    // ─────────────────────────────────────────────────
    //  LAUNCH BROWSER (CineMatch HTML UI)
    // ─────────────────────────────────────────────────
    private static void launchFrontend() {
        try {
            String base = System.getProperty("user.dir");
            String sep  = java.io.File.separator;

            String[] paths = {
                base + sep + "src" + sep + "CineMatch.html",
                base + sep + "CineMatch.html",
                base + sep + "src" + sep + "CineMatch_v2.html",
                base + sep + "CineMatch_v2.html"
            };

            java.io.File htmlFile = null;
            for (String p : paths) {
                java.io.File f = new java.io.File(p);
                if (f.exists()) { htmlFile = f; break; }
            }

            if (htmlFile == null) {
                System.out.println("  [!] HTML file not found. Checked:");
                for (String p : paths) System.out.println("      " + p);
                System.out.println("  [!] Continuing in console mode.\n");
                return;
            }

            System.out.println("  [>>] Opening CineMatch UI: " + htmlFile.getName());

            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec(new String[]{
                    "rundll32", "url.dll,FileProtocolHandler", htmlFile.getAbsolutePath()
                });
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"open", htmlFile.getAbsolutePath()});
            } else {
                // Linux / Unix
                Runtime.getRuntime().exec(new String[]{"xdg-open", htmlFile.getAbsolutePath()});
            }

            Thread.sleep(1500);
            System.out.println("  [OK] Browser launched! CineMatch UI is now open.\n");

        } catch (Exception e) {
            System.out.println("  [!] Could not open browser: " + e.getMessage() + "\n");
        }
    }

    // ─────────────────────────────────────────────────
    //  MENU ACTIONS
    // ─────────────────────────────────────────────────
    private static void addUser() {
        System.out.println("\n  -- Add New User --");
        System.out.print("  User ID   : ");
        String id = scanner.nextLine().trim();
        System.out.print("  User Name : ");
        String name = scanner.nextLine().trim();
        if (id.isEmpty() || name.isEmpty()) {
            System.out.println("  [!] ID and Name cannot be empty."); return;
        }
        if (engine.addUser(id, name))
            System.out.println("  [OK] User '" + name + "' added.");
        else
            System.out.println("  [!] User ID '" + id + "' already exists.");
    }

    private static void addMovie() {
        System.out.println("\n  -- Add New Movie --");
        System.out.print("  Movie ID  : ");
        String id = scanner.nextLine().trim();
        System.out.print("  Title     : ");
        String title = scanner.nextLine().trim();
        System.out.print("  Genre     : ");
        String genre = scanner.nextLine().trim();
        if (id.isEmpty() || title.isEmpty() || genre.isEmpty()) {
            System.out.println("  [!] All fields required."); return;
        }
        if (engine.addMovie(id, title, genre))
            System.out.println("  [OK] Movie '" + title + "' (" + genre + ") added.");
        else
            System.out.println("  [!] Movie ID '" + id + "' already exists.");
    }

    private static void rateMovie() {
        System.out.println("\n  -- Rate a Movie --");
        System.out.print("  User ID   : ");
        String userId = scanner.nextLine().trim();
        if (!engine.userExists(userId)) { System.out.println("  [!] User not found."); return; }

        System.out.print("  Movie ID  : ");
        String movieId = scanner.nextLine().trim();
        if (!engine.movieExists(movieId)) { System.out.println("  [!] Movie not found."); return; }

        double score = readDoubleInput("  Rating (1.0 - 5.0): ");
        if (score < 1.0 || score > 5.0) { System.out.println("  [!] Must be 1.0 - 5.0."); return; }

        if (engine.rateMovie(userId, movieId, score))
            System.out.printf("  [OK] Rating %.1f saved for '%s'.%n",
                    score, engine.getMovie(movieId).getName());
        else
            System.out.println("  [!] Failed to save rating.");
    }

    private static void getRecommendations() {
        System.out.println("\n  -- Get Recommendations --");
        System.out.print("  User ID   : ");
        String userId = scanner.nextLine().trim();
        if (!engine.userExists(userId)) { System.out.println("  [!] User not found."); return; }

        int topN = readIntInput("  How many? (e.g. 5): ");
        if (topN <= 0) topN = 5;

        System.out.print("  Genre filter? (blank = all): ");
        String genre = scanner.nextLine().trim();

        User user = engine.getUser(userId);
        System.out.println("\n  ╔══════════════════════════════════════════════╗");
        System.out.println("  ║   CINEMATCH  ·  Smart Recommendations       ║");
        System.out.println("  ╠══════════════════════════════════════════════╣");
        System.out.printf("  ║  User      : %-30s ║%n", user.getName());
        System.out.printf("  ║  Algorithm : %-30s ║%n", engine.getSimilarityStrategy().algorithmName());
        if (!genre.isEmpty())
            System.out.printf("  ║  Genre     : %-30s ║%n", genre);
        System.out.println("  ╚══════════════════════════════════════════════╝");

        List<RecommendedMovie> recs = engine.getRecommendations(userId, topN, genre);

        if (recs.isEmpty()) {
            System.out.println("  No recommendations found.");
            if (!user.hasAnyRatings())
                System.out.println("  Tip: Rate some movies first (option 3)!");
        } else {
            boolean cold = !user.hasAnyRatings();
            System.out.println(cold
                ? "\n  [Cold Start] Showing popular movies:\n"
                : "\n  [Collaborative Filtering] Personalised picks:\n");
            int rank = 1;
            for (RecommendedMovie rec : recs) {
                Movie m = rec.getMovie();
                System.out.printf("  %2d. %-34s [%-12s]%n", rank++, m.getName(), m.getGenre());
                System.out.printf("      Score: %5.1f%%", rec.getScore() * 100);
                if (!cold && rec.getSimilarity() > 0)
                    System.out.printf("   |  Similarity: %.3f", rec.getSimilarity());
                System.out.println();
            }
        }
        System.out.println("\n  ────────────────────────────────────────────────\n");
    }

    private static void listUsers() {
        List<User> users = engine.getAllUsers();
        System.out.println("\n  ── Users (" + users.size() + ") ─────────────────────────────");
        if (users.isEmpty()) { System.out.println("  No users yet.\n"); return; }
        System.out.printf("  %-10s %-22s %-12s%n", "ID", "Name", "Ratings");
        System.out.println("  " + "─".repeat(46));
        for (User u : users)
            System.out.printf("  %-10s %-22s %d%n",
                    u.getUserId(), u.getName(), u.getRatings().size());
        System.out.println();
    }

    private static void listMovies() {
        List<Movie> movies = engine.getAllMovies();
        System.out.println("\n  ── Movies (" + movies.size() + ") ────────────────────────────────────────");
        if (movies.isEmpty()) { System.out.println("  No movies yet.\n"); return; }
        System.out.printf("  %-8s %-36s %-14s %s%n", "ID", "Title", "Genre", "Avg Rating");
        System.out.println("  " + "─".repeat(74));
        for (Movie m : movies)
            System.out.printf("  %-8s %-36s %-14s %.1f (%d votes)%n",
                    m.getId(), m.getName(), m.getGenre(),
                    m.getAverageRating(), m.getRatingCount());
        System.out.println();
    }

    private static void showSimilarityMatrix() {
        List<User> users = engine.getAllUsers();
        if (users.size() < 2) {
            System.out.println("\n  [!] Need at least 2 users with ratings.\n"); return;
        }
        System.out.println("\n  ── Cosine Similarity Matrix ─────────────────────\n");
        System.out.printf("  %-14s", "");
        for (User u : users) System.out.printf("%-14s", u.getName());
        System.out.println();
        System.out.println("  " + "─".repeat(14 + 14 * users.size()));
        for (User row : users) {
            System.out.printf("  %-14s", row.getName());
            for (User col : users) {
                if (row.getUserId().equals(col.getUserId()))
                    System.out.printf("%-14s", "1.000");
                else
                    System.out.printf("%-14.3f", engine.computeUserSimilarity(row, col));
            }
            System.out.println();
        }
        System.out.println();
    }

    // ─────────────────────────────────────────────────
    //  SAMPLE DATA  (matches CineMatch.html database)
    //  130 movies across Hollywood + Bollywood
    // ─────────────────────────────────────────────────
    private static void seedSampleData() {

        // ── Users ──────────────────────────────────────
        engine.addUser("u1", "Alice");
        engine.addUser("u2", "Bob");
        engine.addUser("u3", "Carol");
        engine.addUser("u4", "Dave");
        engine.addUser("u5", "Riya");
        engine.addUser("u6", "Arjun");
        engine.addUser("u7", "Eve");

        // ── Hollywood · Action ─────────────────────────
        engine.addMovie("m1",  "The Dark Knight",              "Action");
        engine.addMovie("m2",  "Inception",                    "Sci-Fi");
        engine.addMovie("m3",  "Interstellar",                 "Sci-Fi");
        engine.addMovie("m4",  "The Matrix",                   "Sci-Fi");
        engine.addMovie("m5",  "Avengers: Endgame",            "Action");
        engine.addMovie("m6",  "Mad Max: Fury Road",           "Action");
        engine.addMovie("m7",  "John Wick",                    "Action");
        engine.addMovie("m8",  "Top Gun: Maverick",            "Action");
        engine.addMovie("m9",  "Mission: Impossible Fallout",  "Action");
        engine.addMovie("m10", "The Avengers",                 "Action");
        engine.addMovie("m11", "Gladiator",                    "Action");
        engine.addMovie("m12", "Batman Begins",                "Action");
        engine.addMovie("m13", "Spider-Man: No Way Home",      "Action");
        engine.addMovie("m14", "Black Panther",                "Action");
        engine.addMovie("m15", "300",                          "Action");
        engine.addMovie("m16", "The Dark Knight Rises",        "Action");
        engine.addMovie("m17", "Iron Man",                     "Action");
        engine.addMovie("m18", "Fast & Furious 7",             "Action");
        engine.addMovie("m19", "Wonder Woman",                 "Action");
        engine.addMovie("m20", "Captain America: Civil War",   "Action");

        // ── Crime / Thriller ───────────────────────────
        engine.addMovie("m21", "The Godfather",                "Drama");
        engine.addMovie("m22", "Pulp Fiction",                 "Thriller");
        engine.addMovie("m23", "Fight Club",                   "Thriller");
        engine.addMovie("m24", "Parasite",                     "Thriller");
        engine.addMovie("m25", "Goodfellas",                   "Drama");
        engine.addMovie("m26", "The Silence of the Lambs",     "Thriller");
        engine.addMovie("m27", "The Wolf of Wall Street",      "Drama");
        engine.addMovie("m28", "Se7en",                        "Thriller");
        engine.addMovie("m29", "Joker",                        "Drama");
        engine.addMovie("m30", "Prisoners",                    "Thriller");
        engine.addMovie("m31", "Knives Out",                   "Thriller");
        engine.addMovie("m32", "Gone Girl",                    "Thriller");
        engine.addMovie("m33", "No Country for Old Men",       "Thriller");
        engine.addMovie("m34", "Zodiac",                       "Thriller");
        engine.addMovie("m35", "Heat",                         "Action");
        engine.addMovie("m36", "Oldboy",                       "Thriller");

        // ── Sci-Fi / Adventure ─────────────────────────
        engine.addMovie("m37", "Dune",                         "Sci-Fi");
        engine.addMovie("m38", "Oppenheimer",                  "Drama");
        engine.addMovie("m39", "Avatar",                       "Sci-Fi");
        engine.addMovie("m40", "Blade Runner 2049",            "Sci-Fi");
        engine.addMovie("m41", "Arrival",                      "Sci-Fi");
        engine.addMovie("m42", "The Martian",                  "Sci-Fi");
        engine.addMovie("m43", "Gravity",                      "Sci-Fi");
        engine.addMovie("m44", "2001: A Space Odyssey",        "Sci-Fi");
        engine.addMovie("m45", "E.T. the Extra-Terrestrial",   "Sci-Fi");
        engine.addMovie("m46", "Dune: Part Two",               "Sci-Fi");
        engine.addMovie("m47", "Star Wars: A New Hope",        "Sci-Fi");
        engine.addMovie("m48", "The Terminator",               "Sci-Fi");
        engine.addMovie("m49", "Back to the Future",           "Sci-Fi");

        // ── Drama ──────────────────────────────────────
        engine.addMovie("m50", "The Shawshank Redemption",     "Drama");
        engine.addMovie("m51", "Forrest Gump",                 "Drama");
        engine.addMovie("m52", "Schindler's List",             "Drama");
        engine.addMovie("m53", "Whiplash",                     "Drama");
        engine.addMovie("m54", "12 Years a Slave",             "Drama");
        engine.addMovie("m55", "The Pursuit of Happyness",     "Drama");
        engine.addMovie("m56", "A Beautiful Mind",             "Drama");
        engine.addMovie("m57", "American History X",           "Drama");
        engine.addMovie("m58", "The Green Mile",               "Drama");
        engine.addMovie("m59", "Good Will Hunting",            "Drama");
        engine.addMovie("m60", "Titanic",                      "Drama");
        engine.addMovie("m61", "The Pianist",                  "Drama");
        engine.addMovie("m62", "Manchester by the Sea",        "Drama");
        engine.addMovie("m63", "1917",                         "Drama");

        // ── Comedy ─────────────────────────────────────
        engine.addMovie("m64", "The Grand Budapest Hotel",     "Comedy");
        engine.addMovie("m65", "Superbad",                     "Comedy");
        engine.addMovie("m66", "The Hangover",                 "Comedy");
        engine.addMovie("m67", "Home Alone",                   "Comedy");
        engine.addMovie("m68", "Game Night",                   "Comedy");
        engine.addMovie("m69", "Bridesmaids",                  "Comedy");
        engine.addMovie("m70", "Step Brothers",                "Comedy");
        engine.addMovie("m71", "Groundhog Day",                "Comedy");

        // ── Animation ──────────────────────────────────
        engine.addMovie("m72", "Spider-Man: Into the Spider-Verse", "Animation");
        engine.addMovie("m73", "Spirited Away",                "Animation");
        engine.addMovie("m74", "The Lion King",                "Animation");
        engine.addMovie("m75", "WALL-E",                       "Animation");
        engine.addMovie("m76", "Toy Story",                    "Animation");
        engine.addMovie("m77", "My Neighbor Totoro",           "Animation");
        engine.addMovie("m78", "Coco",                         "Animation");
        engine.addMovie("m79", "Finding Nemo",                 "Animation");
        engine.addMovie("m80", "Shrek",                        "Animation");

        // ── Horror ─────────────────────────────────────
        engine.addMovie("m81", "Get Out",                      "Horror");
        engine.addMovie("m82", "A Quiet Place",                "Horror");
        engine.addMovie("m83", "Hereditary",                   "Horror");
        engine.addMovie("m84", "It",                           "Horror");
        engine.addMovie("m85", "The Conjuring",                "Horror");
        engine.addMovie("m86", "Midsommar",                    "Horror");
        engine.addMovie("m87", "The Shining",                  "Horror");

        // ── Bollywood ──────────────────────────────────
        engine.addMovie("b1",  "3 Idiots",                     "Bollywood");
        engine.addMovie("b2",  "Dangal",                       "Bollywood");
        engine.addMovie("b3",  "Dilwale Dulhania Le Jayenge",  "Bollywood");
        engine.addMovie("b4",  "PK",                           "Bollywood");
        engine.addMovie("b5",  "Andhadhun",                    "Bollywood");
        engine.addMovie("b6",  "Gully Boy",                    "Bollywood");
        engine.addMovie("b7",  "Taare Zameen Par",             "Bollywood");
        engine.addMovie("b8",  "Queen",                        "Bollywood");
        engine.addMovie("b9",  "Dil Chahta Hai",               "Bollywood");
        engine.addMovie("b10", "Zindagi Na Milegi Dobara",     "Bollywood");
        engine.addMovie("b11", "Barfi!",                       "Bollywood");
        engine.addMovie("b12", "Gangs of Wasseypur",           "Bollywood");
        engine.addMovie("b13", "Uri: The Surgical Strike",     "Bollywood");
        engine.addMovie("b14", "Kabir Singh",                  "Bollywood");
        engine.addMovie("b15", "Bajrangi Bhaijaan",            "Bollywood");
        engine.addMovie("b16", "Lagaan",                       "Bollywood");
        engine.addMovie("b17", "Mughal-E-Azam",                "Bollywood");
        engine.addMovie("b18", "Swades",                       "Bollywood");
        engine.addMovie("b19", "Rang De Basanti",              "Bollywood");
        engine.addMovie("b20", "Dil Dhadakne Do",              "Bollywood");
        engine.addMovie("b21", "Rockstar",                     "Bollywood");
        engine.addMovie("b22", "Kahaani",                      "Bollywood");
        engine.addMovie("b23", "Dil Se",                       "Bollywood");
        engine.addMovie("b24", "Masaan",                       "Bollywood");
        engine.addMovie("b25", "Article 15",                   "Bollywood");
        engine.addMovie("b26", "Tumbbad",                      "Bollywood");

        // ── Seed Ratings ───────────────────────────────
        // Alice — Action/Sci-Fi fan
        engine.rateMovie("u1","m1",5.0);  engine.rateMovie("u1","m2",4.5);
        engine.rateMovie("u1","m3",4.0);  engine.rateMovie("u1","m4",5.0);
        engine.rateMovie("u1","m5",3.5);  engine.rateMovie("u1","m37",4.5);
        engine.rateMovie("u1","m46",5.0); engine.rateMovie("u1","m47",4.0);

        // Bob — Mixed taste
        engine.rateMovie("u2","m1",4.5);  engine.rateMovie("u2","m2",4.0);
        engine.rateMovie("u2","m21",3.0); engine.rateMovie("u2","m4",4.5);
        engine.rateMovie("u2","m51",5.0); engine.rateMovie("u2","b1",4.0);
        engine.rateMovie("u2","b4",3.5);  engine.rateMovie("u2","m64",4.0);

        // Carol — Drama/Bollywood fan
        engine.rateMovie("u3","m21",5.0); engine.rateMovie("u3","m22",4.5);
        engine.rateMovie("u3","m51",4.0); engine.rateMovie("u3","m50",5.0);
        engine.rateMovie("u3","m24",4.0); engine.rateMovie("u3","b8",4.5);
        engine.rateMovie("u3","b7",5.0);  engine.rateMovie("u3","b3",4.0);

        // Dave — Thriller/Sci-Fi fan
        engine.rateMovie("u4","m2",5.0);  engine.rateMovie("u4","m3",4.5);
        engine.rateMovie("u4","m22",4.0); engine.rateMovie("u4","m4",5.0);
        engine.rateMovie("u4","m24",3.5); engine.rateMovie("u4","b5",4.5);
        engine.rateMovie("u4","m33",4.5); engine.rateMovie("u4","m36",4.0);

        // Riya — Bollywood fanatic
        engine.rateMovie("u5","b1",5.0);  engine.rateMovie("u5","b2",4.5);
        engine.rateMovie("u5","b3",5.0);  engine.rateMovie("u5","b4",4.0);
        engine.rateMovie("u5","b5",4.5);  engine.rateMovie("u5","b7",5.0);
        engine.rateMovie("u5","b10",4.5); engine.rateMovie("u5","b19",5.0);
        engine.rateMovie("u5","b22",4.5); engine.rateMovie("u5","m51",4.0);

        // Arjun — Action + Bollywood
        engine.rateMovie("u6","b1",4.5);  engine.rateMovie("u6","b2",5.0);
        engine.rateMovie("u6","b6",4.0);  engine.rateMovie("u6","b5",5.0);
        engine.rateMovie("u6","b13",4.5); engine.rateMovie("u6","m1",4.0);
        engine.rateMovie("u6","m5",4.5);  engine.rateMovie("u6","m11",4.5);
        engine.rateMovie("u6","b26",5.0);

        // Eve — Horror/Animation fan
        engine.rateMovie("u7","m81",5.0); engine.rateMovie("u7","m82",4.5);
        engine.rateMovie("u7","m83",4.0); engine.rateMovie("u7","m87",5.0);
        engine.rateMovie("u7","m72",4.5); engine.rateMovie("u7","m73",5.0);
        engine.rateMovie("u7","m74",4.0); engine.rateMovie("u7","m78",4.5);

        System.out.println("  [OK] Sample data loaded:");
        System.out.println("       · 7 users, 130 movies, ~60 ratings");
        System.out.println("       · Genres: Action, Crime, Sci-Fi, Drama, Comedy,");
        System.out.println("                 Animation, Horror, Bollywood\n");
    }

    // ─────────────────────────────────────────────────
    //  UI HELPERS
    // ─────────────────────────────────────────────────
    private static void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════╗");
        System.out.println("  ║          C I N E M A T C H                      ║");
        System.out.println("  ║    Hollywood + Bollywood · 130 Movies            ║");
        System.out.println("  ║    Collaborative Filtering · Cosine Similarity   ║");
        System.out.println("  ╚══════════════════════════════════════════════════╝");
        System.out.println();
    }

    private static void printMenu() {
        System.out.println("  ┌──────────────────────────────────┐");
        System.out.println("  │           MAIN  MENU             │");
        System.out.println("  ├──────────────────────────────────┤");
        System.out.println("  │  1. Add User                     │");
        System.out.println("  │  2. Add Movie                    │");
        System.out.println("  │  3. Rate a Movie                 │");
        System.out.println("  │  4. Get Recommendations          │");
        System.out.println("  │  5. List All Users               │");
        System.out.println("  │  6. List All Movies              │");
        System.out.println("  │  7. Show Similarity Matrix       │");
        System.out.println("  │  8. Save Data                    │");
        System.out.println("  │  9. Save & Exit                  │");
        System.out.println("  └──────────────────────────────────┘");
    }

    private static int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  [!] Please enter a valid integer.");
            }
        }
    }

    private static double readDoubleInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  [!] Please enter a valid number.");
            }
        }
    }
}