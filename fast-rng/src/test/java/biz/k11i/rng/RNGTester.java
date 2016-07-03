package biz.k11i.rng;

import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.stat.inference.ChiSquareTest;

import java.util.Arrays;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

abstract class RNGTester {
    private static class CommonsMathRandom extends Random {
        private final RandomGenerator randomGenerator = new Well19937c();

        @Override
        public double nextDouble() {
            return randomGenerator.nextDouble();
        }

        @Override
        public long nextLong() {
            return randomGenerator.nextLong();
        }
    }

    private final int numBins;

    RNGTester(int numBins) {
        this.numBins = numBins;
    }

    abstract RealDistribution createDistribution();

    abstract double generateRandomValue(Random random);

    private double[] calculateBoundaries() {
        RealDistribution distribution = createDistribution();

        double[] result = new double[numBins];
        for (int i = 0; i < numBins; i++) {
            double p = 1.0 * (i + 1) / numBins;
            result[i] = distribution.inverseCumulativeProbability(p);
        }

        return result;
    }

    private double[] calculateExpectations() {
        double[] result = new double[numBins];
        Arrays.fill(result, 1.0 / numBins);
        return result;
    }

    void testGoodnessOfFit() {
        final double SIGNIFICANCE_LEVEL = 0.01;
        final int NUM_TRIALS = 10;
        final int NUM_ITERATIONS = 2_000_000;
        final int ACCEPTABLE_FAILURE_COUNT = 2;

        double[] boundaries = calculateBoundaries();
        double[] expected = calculateExpectations();
        Random random = new CommonsMathRandom();

        long[] observed = new long[numBins];

        int failureCount = 0;
        for (int i = 0; i < NUM_TRIALS; i++) {
            long beginMillis = System.currentTimeMillis();
            for (int j = 0; j < NUM_ITERATIONS; j++) {
                double r = generateRandomValue(random);

                int k = Arrays.binarySearch(boundaries, r);
                observed[k < 0 ? ~k : k]++;
            }
            long endMillis = System.currentTimeMillis();

            ChiSquareTest chiSquareTest = new ChiSquareTest();
            double chiSquare = chiSquareTest.chiSquare(expected, observed);
            double pValue = chiSquareTest.chiSquareTest(expected, observed);

            String message = String.format("[%s] chi^2 = %.3f, p-value = %.5f, elapsedMillis = %d",
                    this.toString(), chiSquare, pValue, endMillis - beginMillis);
            System.out.println(message);

            if (pValue < SIGNIFICANCE_LEVEL) {
                failureCount++;
            }
        }

        assertThat(failureCount, lessThan(ACCEPTABLE_FAILURE_COUNT));
    }
}
