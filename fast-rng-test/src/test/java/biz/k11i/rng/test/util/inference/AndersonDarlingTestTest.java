package biz.k11i.rng.test.util.inference;

import org.junit.jupiter.api.Test;

import java.util.SplittableRandom;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class AndersonDarlingTestTest {
    @Test
    void testNonuniformityDetectin() {
        double[] x = new double[1000];

        assertThat(AndersonDarlingTest.andersonDarlingTest(x))
                .isGreaterThanOrEqualTo(0.0)
                .isLessThan(1e-5);
    }

    @Test
    void testUniformityDetection() {
        double[] x = new double[1000];
        for (int i = 0; i < x.length; i++) {
            x[i] = (i) / (double) (x.length);
        }

        assertThat(AndersonDarlingTest.andersonDarlingTest(x))
                .isGreaterThan(1.0 - 1e-5)
                .isLessThanOrEqualTo(1.0);
    }

    @Test
    void testRandom() {
        SplittableRandom r = new SplittableRandom(1);
        double[] pValues = IntStream.range(0, 100)
                .mapToDouble(ignore -> AndersonDarlingTest.andersonDarlingTest(r.doubles(100).sorted().toArray()))
                .peek(System.out::println)
                .sorted()
                .toArray();

        assertThat(AndersonDarlingTest.andersonDarlingTest(pValues))
                .isGreaterThan(1e-5)
                .isLessThan(1.0 - 1e-5);
    }
}