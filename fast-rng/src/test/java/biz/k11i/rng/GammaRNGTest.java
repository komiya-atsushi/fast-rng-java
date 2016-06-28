package biz.k11i.rng;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.Random;

@RunWith(Theories.class)
public class GammaRNGTest {
    static class GammaRNGTester extends RNGTester {
        private final double shape;
        private final double scale;
        private final RealDistribution distribution;

        GammaRNGTester(int numBins, double shape, double scale) {
            super(numBins);
            this.shape = shape;
            this.scale = scale;
            this.distribution = new GammaDistribution(shape, scale, 1e-300);
        }

        @Override
        RealDistribution createDistribution() {
            return distribution;
        }

        @Override
        double generateRandomValue(Random random) {
            return GammaRNG.FAST_RNG.generate(random, shape, scale);
        }

        @Override
        public String toString() {
            return String.format("GammaRNG, shape = %.3f, scale = %.2f", shape, scale);
        }
    }

    @DataPoints("shape")
    public static final double[] SHAPE_PARAMETERS = {0.1, 0.5, 0.9, 1.0, 100.0};

    @DataPoints("scale")
    public static final double[] SCALE_PARAMETERS = {0.1, 0.9, 1.0, 100.0};

    @Theory
    public void testGoodnessOfFit(@FromDataPoints("shape") double shape, @FromDataPoints("scale") double scale) {
        new GammaRNGTester(200, shape, scale).testGoodnessOfFit();
    }
}