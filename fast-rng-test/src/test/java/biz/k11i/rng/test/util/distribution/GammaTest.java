package biz.k11i.rng.test.util.distribution;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Stream;

class GammaTest extends ProbabilityDistributionTestBase {
    private static final int N = 10000;

    private static Stream<Arguments> distributionParameters() {
        double[] values = {0.001, 0.1, 0.5, 0.999, 1.0, 1.1, 5.5, 16.0, 256.0, 1024.0};

        return Arrays.stream(values)
                .boxed()
                .flatMap(shape -> Arrays.stream(values).mapToObj(scale -> Arguments.of(shape, scale)));
    }

    @ParameterizedTest
    @MethodSource("distributionParameters")
    void testCdf(double shape, double scale) {
        IntToDoubleFunction f = i -> (i == 0 ? 0.0 : FastMath.pow(10, (double) (i - 5000) / 100));
        testCdf(
                ProbabilityDistributions.gamma(shape, scale),
                new GammaDistribution(shape, scale),
                N,
                f,
                "Gamma(%f, %f).cdf(%f)",
                x -> new Object[]{shape, scale, x});
    }

    @ParameterizedTest
    @MethodSource("distributionParameters")
    void testInverseCdf(double shape, double scale) {
        testInverseCdf(
                ProbabilityDistributions.gamma(shape, scale),
                new GammaDistribution(shape, scale),
                N,
                "Gamma(%f, %f).inverseCdf(%f)",
                p -> new Object[]{shape, scale, p});
    }
}
