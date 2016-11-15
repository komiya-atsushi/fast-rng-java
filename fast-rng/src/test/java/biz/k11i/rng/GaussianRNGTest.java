package biz.k11i.rng;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.junit.Test;

import java.util.Random;

public class GaussianRNGTest {
    private static class GaussianRNGTester extends RNGTester {
        private final GaussianRNG rng;
        GaussianRNGTester(GaussianRNG rng, int numBins) {
            super(numBins);
            this.rng = rng;
        }

        @Override
        RealDistribution createDistribution() {
            return new NormalDistribution();
        }

        @Override
        double generateRandomValue(Random random) {
            return rng.generate(random);
        }

        @Override
        public String toString() {
            return "GaussianRNG";
        }
    }

    @Test
    public void testGoodnessOfFit_fast() {
        final int numBins = 200;
        new GaussianRNGTester(GaussianRNG.FAST_RNG, numBins).testGoodnessOfFit();
    }

    @Test
    public void testGoodnessOfFit_general() {
        final int numBins = 200;
        new GaussianRNGTester(GaussianRNG.GENERAL_RNG, numBins).testGoodnessOfFit();
    }
}