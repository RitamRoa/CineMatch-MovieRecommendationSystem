import java.util.HashMap;
import java.util.Map;

/**
 * User class — represents a user with their movie ratings.
 * Uses HashMap to store movie ratings (JAVA COLLECTIONS).
 * Demonstrates ENCAPSULATION.
 */
public class User {
    private String userId;
    private String name;

    // HashMap: movieId -> rating score (JAVA COLLECTIONS)
    private Map<String, Double> ratings;

    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
        this.ratings = new HashMap<>();
    }

    /**
     * Add or update a rating for a movie.
     */
    public void addRating(String movieId, double score) {
        if (score < 1.0 || score > 5.0)
            throw new IllegalArgumentException("Rating must be between 1.0 and 5.0");
        ratings.put(movieId, score);
    }

    /**
     * Get rating for a specific movie. Returns 0 if not rated.
     */
    public double getRating(String movieId) {
        return ratings.getOrDefault(movieId, 0.0);
    }

    public boolean hasRated(String movieId) {
        return ratings.containsKey(movieId);
    }

    public boolean hasAnyRatings() {
        return !ratings.isEmpty();
    }

    // Getters and Setters — ENCAPSULATION
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public Map<String, Double> getRatings() { return ratings; }

    @Override
    public String toString() {
        return "User{id=" + userId + ", name=" + name + ", ratings=" + ratings.size() + "}";
    }
}
