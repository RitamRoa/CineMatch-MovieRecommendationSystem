import java.util.ArrayList;
import java.util.List;

/**
 * Movie class — extends Item (INHERITANCE).
 * Represents a movie with genre and rating metadata.
 */
public class Movie extends Item {
    private String genre;
    private List<Double> allRatings; // For computing popularity

    public Movie(String id, String name, String genre) {
        super(id, name); // Calls Item constructor
        this.genre = genre;
        this.allRatings = new ArrayList<>();
    }

    // POLYMORPHISM — overrides abstract method from Item
    @Override
    public void displayInfo() {
        System.out.println("  Movie  : " + getName());
        System.out.println("  ID     : " + getId());
        System.out.println("  Genre  : " + genre);
        double avg = getAverageRating();
        System.out.printf("  Avg Rating: %.2f (%d votes)%n", avg, allRatings.size());
    }

    public void addRatingRecord(double rating) {
        allRatings.add(rating);
    }

    public double getAverageRating() {
        if (allRatings.isEmpty()) return 0.0;
        double sum = 0;
        for (double r : allRatings) sum += r;
        return sum / allRatings.size();
    }

    public int getRatingCount() {
        return allRatings.size();
    }

    // Getters and Setters — ENCAPSULATION
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
}
