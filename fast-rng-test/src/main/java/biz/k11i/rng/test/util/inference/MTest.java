package biz.k11i.rng.test.util.inference;

import biz.k11i.rng.test.util.distribution.ContinuousDistribution;
import biz.k11i.rng.test.util.distribution.ProbabilityDistributions;
import org.apache.commons.math3.exception.DimensionMismatchException;

/**
 * Implements M-test to detect outliers in contingency table.
 *
 * <p>
 * Fuchs, Camil, and Ron Kenett.
 * <i>"A test for detecting outlying cells in the multinomial distribution and two-way contingency tables."</i>
 * Journal of the American Statistical Association 75.370 (1980): 395-398.
 * </p>
 */
public class MTest {
    private static final ContinuousDistribution GAUSSIAN = ProbabilityDistributions.gaussian(0, 1);

    /**
     * Tests for detecting outliers in array.
     *
     * @param probs array of expected probabilities or expected frequency counts
     * @param freqs array of observed frequency counts
     * @return p-value
     */
    public static double mTest(final double[] probs, final long[] freqs) {
        if (probs.length != freqs.length) {
            throw new DimensionMismatchException(probs.length, freqs.length);
        }

        int k = probs.length;
        double pSum = 0;
        long fSum = 0;

        for (int i = 0; i < k; i++) {
            pSum += probs[i];
            fSum += freqs[i];
        }

        double maxZ = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < k; i++) {
            double mean = fSum * (probs[i] / pSum);
            double var = mean * (1 - probs[i] / pSum);

            double z = Math.abs((freqs[i] - mean) / Math.sqrt(var));
            if (z > maxZ) {
                maxZ = z;
            }
        }

        return 1 - Math.pow((2 * GAUSSIAN.cdf(maxZ) - 1), k);
    }
}
