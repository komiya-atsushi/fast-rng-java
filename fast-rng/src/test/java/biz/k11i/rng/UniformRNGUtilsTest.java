package biz.k11i.rng;

import biz.k11i.rng.test.SecondLevelTest;
import biz.k11i.rng.test.gof.GoodnessOfFitTest;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class UniformRNGUtilsTest {
    static Stream<Integer> bounds() {
        return Stream.of(3, 7, 997, 100_000, (1 << 30) + (1 << 29), Integer.MAX_VALUE - 1);
    }

    @ParameterizedTest
    @MethodSource("bounds")
    void test(int bound) {
        GoodnessOfFitTest gofTest = GoodnessOfFitTest.discrete()
                .probabilityDistribution(new UniformIntegerDistribution(0, bound - 1))
                .randomNumberGenerator(String.format("Nearly divisionless nextInt(%d)", bound), r -> UniformRNGUtils.nextInt(r, bound))
                .numRandomValues(2_000_000)
                .maxFrequencyBins(1000)
                .build();

        SecondLevelTest.builder()
                .numIterations(20)
                .build()
                .testAndVerify(gofTest);
    }
}
