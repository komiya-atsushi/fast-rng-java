package biz.k11i.rng.test.util.distribution;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

class BetaTest extends ProbabilityDistributionTestBase {
    private static final int N = 10000;

    private static Stream<Arguments> distributionParameters() {
        double[] values = {0.001, 0.1, 0.5, 0.999, 1.0, 1.1, 5.5, 16.0, 256.0, 1024.0};

        return Arrays.stream(values)
                .boxed()
                .flatMap(alpha -> Arrays.stream(values).mapToObj(beta -> Arguments.of(alpha, beta)));
    }

    @ParameterizedTest
    @MethodSource("distributionParameters")
    void testCdf(double alpha, double beta) {
        testCdf(
                ProbabilityDistributions.beta(alpha, beta),
                new BetaDistribution(alpha, beta),
                N,
                i -> i / (double) N,
                "Beta(%f, %f).cdf(%f)",
                x -> new Object[]{alpha, beta, x});
    }

    @ParameterizedTest
    @MethodSource("distributionParameters")
    void testInverseCdf(double alpha, double beta) {
        testInverseCdf(
                ProbabilityDistributions.beta(alpha, beta),
                new BetaDistribution(alpha, beta),
                N,
                "Beta(%f, %f).inverseCDF(%f)",
                p -> new Object[]{alpha, beta, p});
    }
}
