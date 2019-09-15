package biz.k11i.rng.test;

import biz.k11i.rng.test.gof.GoodnessOfFitTest;
import biz.k11i.rng.test.util.distribution.ProbabilityDistributions;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecondLevelTestTest {
    @Test
    void test() {
        GoodnessOfFitTest gofTest = GoodnessOfFitTest.continuous()
                .probabilityDistribution(ProbabilityDistributions.gaussian(0.0, 1.002 /* not 1.0 */))
                .numRandomValues(1_000_000)
                .randomNumberGenerator("buggy", Random::nextGaussian)
                .build();

        SecondLevelTest secondLevelTest = SecondLevelTest.builder()
                .numIterations(20)
                .build();

        assertThatThrownBy(() -> secondLevelTest.testAndVerify(gofTest))
                .isInstanceOf(AssertionError.class);
    }
}
