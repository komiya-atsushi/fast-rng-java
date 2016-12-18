package biz.k11i.rng;

import biz.k11i.rng.stat.test.TwoLevelTester;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.Random;

@RunWith(Theories.class)
public class ExponentialRNGTest {
    private static final int N = 2_000_000;
    private static final int K = 20;

    private void testGoodnessOfFitByTwoLevelTesting(final ExponentialRNG exponentialRNG, final double theta) {
        TwoLevelTester tester = new TwoLevelTester(N, K);
        TwoLevelTester.RealRng rng = new TwoLevelTester.RealRng() {
            @Override
            public double generate(Random random) {
                return exponentialRNG.generate(random, theta);
            }
        };
        ExponentialDistribution distribution = new ExponentialDistribution(theta);
        tester.test(rng, distribution);
    }

    @DataPoints
    public static final double[] theta = {0.01, 1.0, 100.0};

    @Theory
    public void testGoodnessOfFit_fast(double theta) {
        testGoodnessOfFitByTwoLevelTesting(ExponentialRNG.FAST_RNG, theta);
    }

    @Theory
    public void testGoodnessOfFit_general(double theta) {
        testGoodnessOfFitByTwoLevelTesting(ExponentialRNG.GENERAL_RNG, theta);
    }
}