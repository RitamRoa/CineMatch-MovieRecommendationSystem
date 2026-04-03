import java.util.*;

/**
 * RecommendationEngine — the core class that drives the recommendation system.
 *
 * Demonstrates:
 *  - ABSTRACTION: hides all ML logic from the outside
 *  - ENCAPSULATION: private fields, public methods only
 *  - USE OF INTERFACE: SimilarityStrategy is injected (Strategy pattern)
 *  - JAVA COLLECTIONS: HashMap, ArrayList, List
 */
public class RecommendationEngine {

    // ── Data stores ──────────────────────────────────────────────
    private Map<String, User>  users;   // userId  -> User
    private Map<String, Movie> movies;  // movieId -> Movie

    // ML strategy — can be swapped at runtime
    private SimilarityStrategy similarityStrategy;

    // Tuning parameters
    private static final double SIMILARITY_THRESHOLD = 0.1;
    private static final int    DEFAULT_TOP_N         = 5;

    // ── Constructor ───────────────────────────────────────────────
    public RecommendationEngine() {
        this.users  = new HashMap<>();
        this.movies = new HashMap<>();
        this.similarityStrategy = new CosineSimilarity(); // default strategy
    }

    public RecommendationEngine(SimilarityStrategy strategy) {
        this.users  = new HashMap<>();
        this.movies = new HashMap<>();
        this.similarityStrategy = strategy;
    }

    // ── User Management ───────────────────────────────────────────
    public boolean addUser(String userId, String name) {
        if (users.containsKey(userId)) return false;
        users.put(userId, new User(userId, name));
        return true;
    }

    public User getUser(String userId) {
        return users.get(userId);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    // ── Movie Management ──────────────────────────────────────────
    public boolean addMovie(String movieId, String title, String genre) {
        if (movies.containsKey(movieId)) return false;
        movies.put(movieId, new Movie(movieId, title, genre));
        return true;
    }

    public Movie getMovie(String movieId) {
        return movies.get(movieId);
    }

    public List<Movie> getAllMovies() {
        return new ArrayList<>(movies.values());
    }

    // ── Rating ────────────────────────────────────────────────────
    public boolean rateMovie(String userId, String movieId, double score) {
        User  user  = users.get(userId);
        Movie movie = movies.get(movieId);
        if (user == null || movie == null) return false;

        user.addRating(movieId, score);
        movie.addRatingRecord(score);
        return true;
    }

    // ── Core ML: Build Rating Vector ──────────────────────────────
    /**
     * Build a rating vector for a user across ALL movies.
     * Movies the user hasn't rated get a score of 0.
     * Vector order is consistent (sorted by movieId).
     */
    private List<Double> buildRatingVector(User user) {
        List<String> sortedMovieIds = getSortedMovieIds();
        List<Double> vector = new ArrayList<>();
        for (String mid : sortedMovieIds) {
            vector.add(user.getRating(mid)); // 0.0 if not rated
        }
        return vector;
    }

    private List<String> getSortedMovieIds() {
        List<String> ids = new ArrayList<>(movies.keySet());
        Collections.sort(ids);
        return ids;
    }

    // ── Core ML: Cosine Similarity Between Two Users ──────────────
    public double computeUserSimilarity(User userA, User userB) {
        List<Double> vecA = buildRatingVector(userA);
        List<Double> vecB = buildRatingVector(userB);
        return similarityStrategy.computeSimilarity(vecA, vecB);
    }

    // ── Core ML: Get Recommendations ─────────────────────────────
    /**
     * Generate Top-N recommendations for a user.
     * Handles cold-start: if user has no ratings → popular movies.
     * Supports genre filtering.
     *
     * Algorithm:
     *  1. Find all other users & compute cosine similarity
     *  2. Filter by SIMILARITY_THRESHOLD
     *  3. For each unseen movie, compute: score = Σ(similarity × rating)
     *  4. Sort by score descending, return Top-N
     */
    public List<RecommendedMovie> getRecommendations(String userId, int topN, String genreFilter) {
        User targetUser = users.get(userId);
        if (targetUser == null) return Collections.emptyList();

        // ── Cold Start: user has no ratings ───────────────────────
        if (!targetUser.hasAnyRatings()) {
            return getPopularMovies(topN, genreFilter, targetUser);
        }

        // ── Step 1: Compute similarities with all other users ─────
        Map<User, Double> similarities = new HashMap<>();
        for (User other : users.values()) {
            if (other.getUserId().equals(userId)) continue;
            if (!other.hasAnyRatings()) continue;
            double sim = computeUserSimilarity(targetUser, other);
            if (sim >= SIMILARITY_THRESHOLD) {
                similarities.put(other, sim);
            }
        }

        // ── Step 2: Aggregate scores for unseen movies ────────────
        // score[movieId] = sum of (similarity × rating) from similar users
        Map<String, Double> scoreMap     = new HashMap<>();
        Map<String, Double> simSumMap    = new HashMap<>(); // for normalization

        for (Map.Entry<User, Double> entry : similarities.entrySet()) {
            User   similarUser = entry.getKey();
            double sim         = entry.getValue();

            for (Map.Entry<String, Double> ratingEntry : similarUser.getRatings().entrySet()) {
                String movieId = ratingEntry.getKey();
                double rating  = ratingEntry.getValue();

                // Only recommend movies the target user hasn't seen
                if (targetUser.hasRated(movieId)) continue;

                // Genre filter
                Movie m = movies.get(movieId);
                if (m == null) continue;
                if (genreFilter != null && !genreFilter.isEmpty()
                        && !m.getGenre().equalsIgnoreCase(genreFilter)) continue;

                // Weighted score = similarity × rating
                scoreMap.merge(movieId, sim * rating, Double::sum);
                simSumMap.merge(movieId, sim, Double::sum);
            }
        }

        // ── Step 3: Build result list ─────────────────────────────
        List<RecommendedMovie> results = new ArrayList<>();
        for (Map.Entry<String, Double> e : scoreMap.entrySet()) {
            String movieId  = e.getKey();
            double rawScore = e.getValue();

            Movie movie = movies.get(movieId);
            if (movie == null) continue;

            // Normalize by sum of similarities (weighted average)
            double simSum = simSumMap.getOrDefault(movieId, 1.0);
            double normalizedScore = rawScore / simSum;

            // Use max similarity among contributing users for display
            double maxSim = similarities.values().stream().mapToDouble(d -> d).max().orElse(0.0);
            results.add(new RecommendedMovie(movie, normalizedScore, maxSim));
        }

        // ── Step 4: Sort and return Top-N ─────────────────────────
        Collections.sort(results);
        return results.subList(0, Math.min(topN, results.size()));
    }

    /**
     * Cold-start fallback: recommend most popular (most-rated) movies.
     */
    private List<RecommendedMovie> getPopularMovies(int topN, String genreFilter, User targetUser) {
        List<Movie> candidates = new ArrayList<>(movies.values());

        // Filter genre
        if (genreFilter != null && !genreFilter.isEmpty()) {
            candidates.removeIf(m -> !m.getGenre().equalsIgnoreCase(genreFilter));
        }

        // Remove already-rated movies
        candidates.removeIf(m -> targetUser.hasRated(m.getId()));

        // Sort by rating count (popularity), then avg rating
        candidates.sort((a, b) -> {
            if (b.getRatingCount() != a.getRatingCount())
                return Integer.compare(b.getRatingCount(), a.getRatingCount());
            return Double.compare(b.getAverageRating(), a.getAverageRating());
        });

        List<RecommendedMovie> result = new ArrayList<>();
        for (Movie m : candidates.subList(0, Math.min(topN, candidates.size()))) {
            result.add(new RecommendedMovie(m, m.getAverageRating(), 0.0));
        }
        return result;
    }

    // ── Convenience overloads (POLYMORPHISM / overloading) ────────
    public List<RecommendedMovie> getRecommendations(String userId) {
        return getRecommendations(userId, DEFAULT_TOP_N, null);
    }

    public List<RecommendedMovie> getRecommendations(String userId, int topN) {
        return getRecommendations(userId, topN, null);
    }

    // ── Getters ───────────────────────────────────────────────────
    public SimilarityStrategy getSimilarityStrategy() { return similarityStrategy; }
    public void setSimilarityStrategy(SimilarityStrategy s) { this.similarityStrategy = s; }

    public boolean userExists(String userId)  { return users.containsKey(userId); }
    public boolean movieExists(String movieId){ return movies.containsKey(movieId); }
}
