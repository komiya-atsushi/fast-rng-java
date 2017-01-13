package biz.k11i.rng;

import biz.k11i.rng.stat.test.TwoLevelTester;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.junit.experimental.runners.Enclosed;
import org.junit.experimental.theories.*;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public  class GammaRNGTest {
    @DataPoint("N")
    public static final int N = 2_000_000;

    @DataPoint("K")
    public static final int K = 20;

    @DataPoints("shape")
    public static final double[] SHAPE_PARAMETERS = {0.05, 0.1, 0.5, 0.9, 1.0, 1.1, 50.0, 10000.0};

    @DataPoints("scale")
    public static final double[] SCALE_PARAMETERS = {0.01};

    @DataPoints
    public static final GammaRNG[] GAMMA_RNGS = {GammaRNG.FAST_RNG, GammaRNG.GENERAL_RNG};

    @Theory
    public void testGoodnessOfFitByTwoLevelTesting(
            @FromDataPoints("N") final int N,
            @FromDataPoints("K") final int K,
            @FromDataPoints("shape") final double shape,
            @FromDataPoints("scale") final double scale,
            final GammaRNG gammaRng) {

        System.out.printf("GammaRNG(%s): shape = %.2f, scale = %.2f%n", gammaRng.getClass().getSimpleName(), shape, scale);

        TwoLevelTester tester = new TwoLevelTester(N, K);
        tester.test(
                random -> gammaRng.generate(random, shape, scale),
                new GammaDistribution(shape, scale, 1e-14));
    }
}