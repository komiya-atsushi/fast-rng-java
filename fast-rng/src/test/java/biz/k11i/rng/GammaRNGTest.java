package biz.k11i.rng;

import biz.k11i.rng.stat.test.TwoLevelTester;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.Random;

@RunWith(Theories.class)
public class GammaRNGTest {
    private static final int N = 2_000_000;
    private static final int K = 20;

    @DataPoints("shape")
    public static final double[] SHAPE_PARAMETERS = {0.1, 0.5, 0.9, 1.0, 100.0};

    @DataPoints("scale")
    public static final double[] SCALE_PARAMETERS = {0.1, 0.9, 1.0, 100.0};

    @Theory
    public void testGoodnessOfFitByTwoLevelTesting(@FromDataPoints("shape") final double shape, @FromDataPoints("scale") final double scale) {
        System.out.printf("GammaRNG: shape = %.2f, scale = %.2f%n", shape, scale);

        TwoLevelTester tester = new TwoLevelTester(N, K);
        TwoLevelTester.RealRng rng = new TwoLevelTester.RealRng() {
            @Override
            public double generate(Random random) {
                return GammaRNG.FAST_RNG.generate(random, shape, scale);
            }
        };
        GammaDistribution distribution = new GammaDistribution(shape, scale);
        tester.test(rng, distribution);
    }
}