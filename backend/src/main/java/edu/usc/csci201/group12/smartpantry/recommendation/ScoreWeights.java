package edu.usc.csci201.group12.smartpantry.recommendation;

/**
 * Tunable weights for the recommendation scoring formula:
 *
 * <pre>
 *   RecScore = w1 * pantryScore  +  w2 * prefScore  +  w3 * popularityScore
 * </pre>
 *
 * All weights should be non-negative and ideally sum to 1.0, but the engine
 * will not enforce this so you can experiment freely.
 */
public record ScoreWeights(double w1Pantry, double w2Preference, double w3Popularity) {

    /** Default tuning: pantry coverage is the dominant signal. */
    public static ScoreWeights defaults() {
        return new ScoreWeights(0.60, 0.25, 0.15);
    }

    /** Equal weighting — useful for A/B testing. */
    public static ScoreWeights equal() {
        return new ScoreWeights(1.0 / 3, 1.0 / 3, 1.0 / 3);
    }

    /** Pantry-only mode: ignores preferences and popularity. */
    public static ScoreWeights pantryOnly() {
        return new ScoreWeights(1.0, 0.0, 0.0);
    }
}
