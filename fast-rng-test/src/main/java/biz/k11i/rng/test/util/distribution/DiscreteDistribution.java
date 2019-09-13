package biz.k11i.rng.test.util.distribution;

/**
 * Discrete probability distribution.
 */
public interface DiscreteDistribution {
    /**
     * Calculates cumulative distribution function.
     */
    double cdf(int x);

    /**
     * Calculates inverse distribution function.
     */
    int inverseCdf(double p);
}
