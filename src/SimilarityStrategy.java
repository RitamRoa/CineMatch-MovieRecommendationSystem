import java.util.List;

/**
 * SimilarityStrategy INTERFACE — defines the contract for any similarity algorithm.
 * Demonstrates ABSTRACTION and INTERFACE usage in OOP.
 */
public interface SimilarityStrategy {
    /**
     * Compute similarity between two vectors.
     * @param vectorA ratings vector of user A
     * @param vectorB ratings vector of user B
     * @return similarity score between 0.0 and 1.0
     */
    double computeSimilarity(List<Double> vectorA, List<Double> vectorB);

    /**
     * Returns the name of the algorithm.
     */
    String algorithmName();
}


/**
 * CosineSimilarity — implements SimilarityStrategy.
 *
 * Formula: similarity = (A · B) / (|A| × |B|)
 *   where:
 *     A · B = dot product = sum of (a_i * b_i)
 *     |A|   = magnitude   = sqrt(sum of a_i^2)
 *     |B|   = magnitude   = sqrt(sum of b_i^2)
 *
 * Demonstrates INTERFACE IMPLEMENTATION and POLYMORPHISM.
 */
class CosineSimilarity implements SimilarityStrategy {

    @Override
    public double computeSimilarity(List<Double> vectorA, List<Double> vectorB) {
        if (vectorA == null || vectorB == null || vectorA.size() != vectorB.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;

        for (int i = 0; i < vectorA.size(); i++) {
            double a = vectorA.get(i);
            double b = vectorB.get(i);
            dotProduct  += a * b;
            magnitudeA  += a * a;
            magnitudeB  += b * b;
        }

        magnitudeA = Math.sqrt(magnitudeA);
        magnitudeB = Math.sqrt(magnitudeB);

        // Avoid division by zero
        if (magnitudeA == 0.0 || magnitudeB == 0.0) return 0.0;

        return dotProduct / (magnitudeA * magnitudeB);
    }

    /**
     * METHOD OVERLOADING — POLYMORPHISM
     * Convenience overload accepting raw arrays instead of lists.
     */
    public double computeSimilarity(double[] vectorA, double[] vectorB) {
        if (vectorA.length != vectorB.length) return 0.0;

        double dotProduct = 0.0, magA = 0.0, magB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            magA += vectorA[i] * vectorA[i];
            magB += vectorB[i] * vectorB[i];
        }
        magA = Math.sqrt(magA);
        magB = Math.sqrt(magB);
        if (magA == 0 || magB == 0) return 0.0;
        return dotProduct / (magA * magB);
    }

    @Override
    public String algorithmName() {
        return "Cosine Similarity";
    }
}
