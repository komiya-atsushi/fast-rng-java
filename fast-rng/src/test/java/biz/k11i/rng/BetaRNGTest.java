package biz.k11i.rng;

import biz.k11i.rng.stat.test.TwoLevelTester;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.junit.experimental.runners.Enclosed;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static biz.k11i.rng.BetaRNGTest.BetaDistParam.param;

@RunWith(Enclosed.class)
public class BetaRNGTest {
    static class BetaDistParam {
        private final double alpha;
        private final double beta;

        BetaDistParam(double alpha, double beta) {
            this.alpha = alpha;
            this.beta = beta;
        }

        static BetaDistParam param(double alpha, double beta) {
            return new BetaDistParam(alpha, beta);
        }
    }

    @DataPoint("N")
    public static final int N = 1_000_000;

    @DataPoint("K")
    public static final int K = 20;

    @Theory
    public void testGoodnessOfFitByTwoLevelTesting(
            @FromDataPoints("N") final int N,
            @FromDataPoints("K") final int K,
            final BetaDistParam parameters,
            final BetaRNG betaRNG) {

        final double alpha = parameters.alpha;
        final double beta = parameters.beta;

        System.out.printf("%s: alpha = %.2f, beta = %.2f%n", betaRNG.getClass().getSimpleName(), alpha, beta);

        TwoLevelTester tester = new TwoLevelTester(N, K);
        tester.test(
                random -> betaRNG.generate(random, alpha, beta),
                new BetaDistribution(alpha, beta, 1e-15));
        System.out.println(betaRNG);
    }

    @RunWith(Theories.class)
    public static class Case1Test extends BetaRNGTest {
        @DataPoints
        public static final BetaDistParam[] PARAMS = {
                param(0.1, 0.5),
                param(0.1, 0.9),
                param(0.45, 0.46),
                param(0.46, 0.45),
                param(0.5, 0.5),
                param(0.5, 0.9),
                param(0.9, 0.5),
                param(0.99998, 0.99999),
                param(0.99999, 0.99998),
        };

        @DataPoints
        public static final BetaRNG[] RNGS = {BetaRNG.FAST_RNG, BetaRNG.GENERAL_RNG};
    }

    @RunWith(Theories.class)
    public static class Case2Test extends BetaRNGTest {
        @DataPoints
        public static final BetaDistParam[] PARAMS = {
                param(0.099, 10.0),
                param(0.6, 1.2),
                param(1.2, 0.6),
                param(0.7, 5.0),
                param(0.8, 20.0),
                param(20.0, 0.8),
                param(0.9, 80.0)
        };

        @DataPoints
        public static final BetaRNG[] RNGS = {BetaRNG.FAST_RNG, BetaRNG.GENERAL_RNG};
    }

    @RunWith(Theories.class)
    public static class Case3Test extends BetaRNGTest {
        @DataPoints
        public static final BetaDistParam[] PARAMS = {
                param(1.5, 1.5),
                param(1.5, 4.0),
                param(4.0, 1.5),
                param(4.0, 100.0),
                param(100.0, 4.0),
        };

        @DataPoints
        public static final BetaRNG[] RNGS = {BetaRNG.FAST_RNG, BetaRNG.GENERAL_RNG};
    }

    @RunWith(Theories.class)
    public static class SpecialCaseTest extends BetaRNGTest {
        @DataPoints
        public static final BetaDistParam[] PARAMS = {
                param(1.0, 1.01),
                param(1.0, 0.99),
                param(1.0, 10.0),
                param(1.01, 1.0),
                param(0.99, 1.0),
                param(10.0, 1.0),
                param(1.0, 1.0)
        };

        @DataPoints
        public static final BetaRNG[] RNGS = {BetaRNG.FAST_RNG, BetaRNG.GENERAL_RNG};
    }
}
