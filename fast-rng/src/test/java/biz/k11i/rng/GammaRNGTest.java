package biz.k11i.rng;

import biz.k11i.rng.test.SecondLevelTest;
import biz.k11i.rng.test.gof.GoodnessOfFitTest;
import biz.k11i.rng.test.util.distribution.ProbabilityDistributions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class GammaRNGTest {
    private static final double SCALE = 0.01;

    static Stream<Double> parameter() {
        return Stream.of(0.05, 0.1, 0.5, 0.9, 1.0, 1.1, 50.0, 10000.0);
    }

    @ParameterizedTest
    @MethodSource("parameter")
    void testFast(double shape) {
        test(GammaRNG.FAST_RNG, shape);
    }

    @ParameterizedTest
    @MethodSource("parameter")
    void testGeneral(double shape) {
        test(GammaRNG.GENERAL_RNG, shape);
    }

    private void test(GammaRNG rng, double shape) {
        GoodnessOfFitTest gofTest = GoodnessOfFitTest.continuous()
                .probabilityDistribution(ProbabilityDistributions.gamma(shape, SCALE))
                .randomNumberGenerator(String.format("Gamma(%f, %f)", shape, SCALE), r -> rng.generate(r, shape, SCALE))
                .numRandomValues(2_000_000)
                .build();

        SecondLevelTest.builder()
                .numIterations(20)
                .build()
                .testAndVerify(gofTest);
    }
}
