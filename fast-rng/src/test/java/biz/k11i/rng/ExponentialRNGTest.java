package biz.k11i.rng;

import biz.k11i.rng.test.SecondLevelTest;
import biz.k11i.rng.test.gof.GoodnessOfFitTest;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class ExponentialRNGTest {
    static Stream<Double> parameter() {
        return Stream.of(0.01, 1.0, 100.0);
    }

    @ParameterizedTest
    @MethodSource("parameter")
    void testFast(double theta) {
        test(ExponentialRNG.FAST_RNG, theta);
    }

    @ParameterizedTest
    @MethodSource("parameter")
    void testGeneral(double theta) {
        test(ExponentialRNG.GENERAL_RNG, theta);
    }

    private void test(ExponentialRNG rng, double theta) {
        GoodnessOfFitTest gofTest = GoodnessOfFitTest.continuous()
                .probabilityDistribution(new ExponentialDistribution(theta))
                .randomNumberGenerator(String.format("Exp(%f)", theta), r -> rng.generate(r, theta))
                .numRandomValues(2_000_000)
                .build();

        SecondLevelTest.builder()
                .numIterations(20)
                .build()
                .testAndVerify(gofTest);
    }
}
