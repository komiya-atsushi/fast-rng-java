package biz.k11i.rng;

import biz.k11i.rng.test.SecondLevelTest;
import biz.k11i.rng.test.gof.GoodnessOfFitTest;
import biz.k11i.rng.test.util.distribution.ProbabilityDistributions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class BetaRNGTest {
    static Stream<Arguments> parameterCase1() {
        return Stream.of(
                Arguments.of(0.1, 0.5),
                Arguments.of(0.1, 0.9),
                Arguments.of(0.45, 0.46),
                Arguments.of(0.46, 0.45),
                Arguments.of(0.5, 0.5),
                Arguments.of(0.5, 0.9),
                Arguments.of(0.9, 0.5),
                Arguments.of(0.99998, 0.99999),
                Arguments.of(0.99999, 0.99998));
    }

    @ParameterizedTest
    @MethodSource("parameterCase1")
    void testCase1_fast(double alpha, double beta) {
        test(BetaRNG.FAST_RNG, alpha, beta);
    }

    @ParameterizedTest
    @MethodSource("parameterCase1")
    void testCase1_general(double alpha, double beta) {
        test(BetaRNG.GENERAL_RNG, alpha, beta);
    }

    static Stream<Arguments> parameterCase2() {
        return Stream.of(
                Arguments.of(0.099, 10.0),
                Arguments.of(0.6, 1.2),
                Arguments.of(1.2, 0.6),
                Arguments.of(0.7, 5.0),
                Arguments.of(0.8, 20.0),
                Arguments.of(20.0, 0.8),
                Arguments.of(0.9, 80.0));
    }

    @ParameterizedTest
    @MethodSource("parameterCase2")
    void testCase2_fast(double alpha, double beta) {
        test(BetaRNG.FAST_RNG, alpha, beta);
    }

    @ParameterizedTest
    @MethodSource("parameterCase2")
    void testCase2_general(double alpha, double beta) {
        test(BetaRNG.GENERAL_RNG, alpha, beta);
    }

    static Stream<Arguments> parameterCase3() {
        return Stream.of(
                Arguments.of(1.5, 1.5),
                Arguments.of(1.5, 4.0),
                Arguments.of(4.0, 1.5),
                Arguments.of(4.0, 100.0),
                Arguments.of(100.0, 4.0));
    }

    @ParameterizedTest
    @MethodSource("parameterCase3")
    void testCase3_fast(double alpha, double beta) {
        test(BetaRNG.FAST_RNG, alpha, beta);
    }

    @ParameterizedTest
    @MethodSource("parameterCase3")
    void testCase3_general(double alpha, double beta) {
        test(BetaRNG.GENERAL_RNG, alpha, beta);
    }

    static Stream<Arguments> parameterSpecialCase() {
        return Stream.of(
                Arguments.of(1.0, 1.01),
                Arguments.of(1.0, 0.99),
                Arguments.of(1.0, 10.0),
                Arguments.of(1.01, 1.0),
                Arguments.of(0.99, 1.0),
                Arguments.of(10.0, 1.0),
                Arguments.of(1.0, 1.0));
    }

    @ParameterizedTest
    @MethodSource("parameterSpecialCase")
    void testSpecialCase_fast(double alpha, double beta) {
        test(BetaRNG.FAST_RNG, alpha, beta);
    }

    @ParameterizedTest
    @MethodSource("parameterSpecialCase")
    void testSpecialCase_general(double alpha, double beta) {
        test(BetaRNG.GENERAL_RNG, alpha, beta);
    }

    private void test(BetaRNG rng, double alpha, double beta) {
        GoodnessOfFitTest gofTest = GoodnessOfFitTest.continuous()
                .probabilityDistribution(ProbabilityDistributions.beta(alpha, beta))
                .randomNumberGenerator(String.format("Beta(%f, %f)", alpha, beta), r -> rng.generate(r, alpha, beta))
                .numRandomValues(1_000_000)
                .build();

        SecondLevelTest.builder()
                .numIterations(20)
                .build()
                .testAndVerify(gofTest);
    }
}
