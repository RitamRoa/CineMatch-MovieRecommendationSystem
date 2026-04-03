/**
 * Rating class — represents a user's rating for a movie.
 * Demonstrates ENCAPSULATION with private fields.
 */
public class Rating {
    private String userId;
    private String movieId;
    private double score; // 1.0 to 5.0

    public Rating(String userId, String movieId, double score) {
        this.userId = userId;
        this.movieId = movieId;
        this.score = score;
    }

    // Getters and Setters — ENCAPSULATION
    public String getUserId() { return userId; }
    public String getMovieId() { return movieId; }

    public double getScore() { return score; }
    public void setScore(double score) {
        if (score < 1.0 || score > 5.0)
            throw new IllegalArgumentException("Rating must be between 1.0 and 5.0");
        this.score = score;
    }

    @Override
    public String toString() {
        return "Rating{user=" + userId + ", movie=" + movieId + ", score=" + score + "}";
    }
}
