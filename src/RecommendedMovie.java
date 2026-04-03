/**
 * RecommendedMovie — a result wrapper holding a movie and its recommendation score.
 * Used to sort and display recommendations to the user.
 */
public class RecommendedMovie implements Comparable<RecommendedMovie> {
    private Movie movie;
    private double score;       // Weighted: similarity × rating
    private double similarity;  // For display purposes

    public RecommendedMovie(Movie movie, double score, double similarity) {
        this.movie = movie;
        this.score = score;
        this.similarity = similarity;
    }

    // Natural ordering: highest score first
    @Override
    public int compareTo(RecommendedMovie other) {
        return Double.compare(other.score, this.score);
    }

    public Movie getMovie() { return movie; }
    public double getScore() { return score; }
    public double getSimilarity() { return similarity; }
}
