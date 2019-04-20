package biz.k11i.rng;

import java.util.Random;
import java.util.SplittableRandom;

@SuppressWarnings("Duplicates")
public class UniformRNGUtils {
    /**
     * Returns a random integer sampled from discrete uniform distribution {@code unif{0, bound - 1}}.
     *
     * <p>
     * This implementation uses "nearly divisionless" algorithm by Lemire.
     * </p>
     * <p>
     * Lemire, Daniel.
     * <i>"Fast random integer generation in an interval."</i>
     * ACM Transactions on Modeling and Computer Simulation (TOMACS) 29.1 (2019): 3.
     * </p>
     *
     * @param random random number generator ({@link Random} object)
     * @param bound  the upper bound (exclusive)
     * @return sampled random integer between 0 (inclusive) and {@code bound} (exclusive)
     */
    public static int nextInt(Random random, int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive");
        }

        long x = ((long) random.nextInt()) & 0xffff_ffffL;
        long m = x * bound;
        long l = m & 0xffff_ffffL;

        if (l < bound) {
            for (long t = 0x1_0000_0000L % bound; l < t; ) {
                x = ((long) random.nextInt()) & 0xffff_ffffL;
                m = x * bound;
                l = m & 0xffff_ffffL;
            }
        }

        return (int) (m >>> 32);
    }

    /**
     * Returns a random integer sampled from discrete uniform distribution {@code unif{0, bound - 1}}.
     *
     * <p>
     * This implementation uses "nearly divisionless" algorithm by Lemire.
     * </p>
     * <p>
     * Lemire, Daniel.
     * <i>"Fast random integer generation in an interval."</i>
     * ACM Transactions on Modeling and Computer Simulation (TOMACS) 29.1 (2019): 3.
     * </p>
     *
     * @param random random number generator ({@link SplittableRandom} object)
     * @param bound  the upper bound (exclusive)
     * @return sampled random integer between 0 (inclusive) and {@code bound} (exclusive)
     */
    public static int nextInt(SplittableRandom random, int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive");
        }

        long x = ((long) random.nextInt()) & 0xffff_ffffL;
        long m = x * bound;
        long l = m & 0xffff_ffffL;

        if (l < bound) {
            for (long t = 0x1_0000_0000L % bound; l < t; ) {
                x = ((long) random.nextInt()) & 0xffff_ffffL;
                m = x * bound;
                l = m & 0xffff_ffffL;
            }
        }

        return (int) (m >>> 32);
    }
}
