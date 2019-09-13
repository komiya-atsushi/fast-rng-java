package biz.k11i.rng.test.util.distribution;

/**
 * Continuous probability distribution.
 */
public interface ContinuousDistribution {
    /**
     * Calculates cumulative distribution function.
     */
    double cdf(double x);

    /**
     * Calculates inverse distribution function.
     */
    double inverseCdf(double p);
}
