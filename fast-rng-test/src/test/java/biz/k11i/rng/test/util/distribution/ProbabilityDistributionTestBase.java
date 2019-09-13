package biz.k11i.rng.test.util.distribution;

import org.apache.commons.math3.distribution.RealDistribution;

import java.util.function.DoubleFunction;
import java.util.function.IntToDoubleFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProbabilityDistributionTestBase {
    void testCdf(
            ContinuousDistribution sut,
            RealDistribution referenceImpl,
            int n,
            IntToDoubleFunction f,
            String description,
            DoubleFunction<Object[]> descriptionArgsGenerator) {

        for (int i = 0; i <= n; i++) {
            double x = f.applyAsDouble(i);
            assertEquals(
                    referenceImpl.cumulativeProbability(x),
                    sut.cdf(x),
                    () -> String.format(description, descriptionArgsGenerator.apply(x)));
        }
    }

    void testInverseCdf(
            ContinuousDistribution sut,
            RealDistribution referenceImpl,
            int n,
            String description,
            DoubleFunction<Object[]> descriptionArgsGenerator) {

        for (int i = 0; i <= n; i++) {
            double p = i / (double) n;
            assertEquals(
                    referenceImpl.inverseCumulativeProbability(p),
                    sut.inverseCdf(p),
                    () -> String.format(description, descriptionArgsGenerator.apply(p)));
        }
    }
}
