package biz.k11i.rng;

import biz.k11i.rng.stat.test.TwoLevelTester;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.Test;

import java.util.Random;

public class GaussianRNGTest {
    private static final int N = 2_000_000;
    private static final int K = 20;
    private static final NormalDistribution NORMAL_DISTRIBUTION = new NormalDistribution();

    private void testGoodnessOfFitByTwoLevelTesting(final GaussianRNG gaussianRNG) {
        TwoLevelTester tester = new TwoLevelTester(N, K);
        TwoLevelTester.RealRng rng = new TwoLevelTester.RealRng() {
            @Override
            public double generate(Random random) {
                return gaussianRNG.generate(random);
            }
        };
        tester.test(rng, NORMAL_DISTRIBUTION);
    }

    @Test
    public void testGoodnessOfFit_fast() {
        testGoodnessOfFitByTwoLevelTesting(GaussianRNG.FAST_RNG);
    }

    @Test
    public void testGoodnessOfFit_general() {
        testGoodnessOfFitByTwoLevelTesting(GaussianRNG.GENERAL_RNG);
    }
}