package biz.k11i.rng.test.util.distribution;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Stream;

class GaussianTest extends ProbabilityDistributionTestBase {
    private static final int N = 10000;

    private static Stream<Arguments> distributionParameters() {
        double[] means = {0.0, -1.5, 2.5, -1e10, 1e-10};
        double[] stdDevs = {1e-10, 0.3, 1.0, 3.4, 1e10};

        return Arrays.stream(means)
                .boxed()
                .flatMap(mean -> Arrays.stream(stdDevs).mapToObj(sd -> Arguments.of(mean, sd)));
    }

    @ParameterizedTest
    @MethodSource("distributionParameters")
    void testCdf(double mean, double sd) {
        IntToDoubleFunction f = value -> {
            double hi = (value / 100.0) - 50;
            double lo = (value % 100) - 50;

            return hi * FastMath.pow(10, lo / 2);
        };

        testCdf(
                ProbabilityDistributions.gaussian(mean, sd),
                new NormalDistribution(mean, sd),
                N,
                f,
                "Gaussian(%f, %f).cdf(%f)",
                x -> new Object[]{mean, sd, x});
    }

    @ParameterizedTest
    @MethodSource("distributionParameters")
    void testInverseCdf(double mean, double sd) {
        testInverseCdf(
                ProbabilityDistributions.gaussian(mean, sd),
                new NormalDistribution(mean, sd),
                N,
                "Gaussian(%f, %f).inverseCDF(%f)",
                p -> new Object[]{mean, sd, p});
    }
}
