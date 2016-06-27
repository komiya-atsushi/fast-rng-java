package biz.k11i.rng;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.junit.Test;

import java.util.Random;

public class GaussianRNGTest {
    private static class GaussianRNGTester extends RNGTester {
        GaussianRNGTester(int numBins) {
            super(numBins);
        }

        @Override
        RealDistribution createDistribution() {
            return new NormalDistribution();
        }

        @Override
        double generateRandomValue(Random random) {
            return GaussianRNG.FAST_RNG.generate(random);
        }

        @Override
        public String toString() {
            return "GaussianRNG";
        }
    }

    @Test
    public void testGoodnessOfFit() {
        final int numBins = 200;
        new GaussianRNGTester(numBins).testGoodnessOfFit();
    }
}