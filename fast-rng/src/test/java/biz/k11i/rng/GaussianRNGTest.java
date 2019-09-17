package biz.k11i.rng;

import biz.k11i.rng.test.gof.GoodnessOfFitTest;
import biz.k11i.rng.test.SecondLevelTest;
import biz.k11i.rng.test.util.distribution.ProbabilityDistributions;
import org.junit.jupiter.api.Test;

class GaussianRNGTest {
    @Test
    void testFast() {
        test(GaussianRNG.FAST_RNG);
    }

    @Test
    void testGeneral() {
        test(GaussianRNG.GENERAL_RNG);
    }

    private void test(GaussianRNG rng) {
        GoodnessOfFitTest gofTest = GoodnessOfFitTest.continuous()
                .probabilityDistribution(ProbabilityDistributions.gaussian(0.0, 1.0))
                .randomNumberGenerator("Gaussian", rng::generate)
                .numRandomValues(2_000_000)
                .build();

        SecondLevelTest.builder()
                .numIterations(20)
                .build()
                .testAndVerify(gofTest);
    }
}
