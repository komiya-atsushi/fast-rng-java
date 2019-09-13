package biz.k11i.rng.test.util.inference;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.SplittableRandom;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class MTestTest {
    @Test
    void testOutlierDetection() {
        final int N = 100;

        double[] probs = new double[N];
        Arrays.fill(probs, 1.0 / N);

        long[] freqs = new long[N];
        Arrays.fill(freqs, 100);
        freqs[0] = 40;

        assertThat(MTest.mTest(probs, freqs))
                .isGreaterThanOrEqualTo(0.0)
                .isLessThan(1e-5);
    }

    @Test
    void testRandom() {
        final int N = 100;

        double[] probs = new double[N];
        Arrays.fill(probs, 1.0 / N);

        SplittableRandom r = new SplittableRandom(1);
        double[] pValues = IntStream.range(0, 100)
                .mapToDouble(ignore -> {
                    long[] freqs = new long[N];
                    r.ints(1000, 0, N).forEach(x -> {
                        freqs[x]++;
                    });
                    return MTest.mTest(probs, freqs);
                })
                .peek(System.out::println)
                .sorted()
                .toArray();

        // Test uniformity of p-values by Anderson-Darling test
        assertThat(AndersonDarlingTest.andersonDarlingTest(pValues))
                .isGreaterThan(1e-5)
                .isLessThan(1.0 - 1e-5);
    }
}