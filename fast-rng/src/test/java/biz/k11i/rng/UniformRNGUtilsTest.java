package biz.k11i.rng;

import biz.k11i.rng.stat.test.TestStatistic;
import biz.k11i.rng.stat.test.TwoLevelTester;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assume.assumeThat;

@RunWith(Theories.class)
public class UniformRNGUtilsTest {
    private static final int N = 2_000_000;
    private static final int K = 20;

    @DataPoints
    public static final int[] upperBounds = {3, 7, 997, 100_000, (1 << 30) + (1 << 29), Integer.MAX_VALUE - 1};

    @Theory
    public void testGoodnessOfFit_javaUtilRandom_nextIntWithBound(int bound) {
        assumeThat(bound, greaterThanOrEqualTo(1000));

        TwoLevelTester tester = new TwoLevelTester(N, K);
        TwoLevelTester.IntRng rng = random -> UniformRNGUtils.nextInt(random, bound);
        tester.test(rng, new UniformIntegerDistribution(0, bound));
    }

    @Theory
    public void testGoodnessOfFit_SplittableRandom_nextIntWithBound(int bound) {
        assumeThat(bound, greaterThanOrEqualTo(1000));

        TwoLevelTester tester = new TwoLevelTester(N, K, SplittableRandomWrapper::split);
        TwoLevelTester.IntRng rng = random -> UniformRNGUtils.nextInt(((SplittableRandomWrapper) random).splittableRandom, bound);
        tester.test(rng, new UniformIntegerDistribution(0, bound));
    }

    @Theory
    public void chiSquareTest(int bound) {
        final int numBins = Math.min(bound, 1000);
        long[] observed = new long[numBins];
        double[] expected = new double[numBins];

        for (int i = 0; i < numBins; i++) {
            long b = bound;
            long l = (b * i) / numBins;
            long h = (b * (i + 1)) / numBins;
            expected[i] = (h - l);
        }

        ChiSquareTest test = new ChiSquareTest();
        double[] pValues = new double[K];

        for (int k = 0; k < K; k++) {
            Arrays.fill(observed, 0);

            for (int i = 0; i < N; i++) {
                long r = UniformRNGUtils.nextInt(ThreadLocalRandom.current(), bound);
                int binIndex = (int) (r * numBins / bound);
                observed[binIndex]++;
            }

            pValues[k] = test.chiSquareTest(expected, observed);
        }

        Arrays.sort(pValues);
        double pValueSecondLevel = TestStatistic.ANDERSON_DARLING.test(pValues);

        System.out.printf("bound = %d, 2nd level p-value = %f%n", bound, pValueSecondLevel);

        assertThat(pValueSecondLevel, greaterThanOrEqualTo(0.001));
    }
}

class SplittableRandomWrapper extends Random {
    private static final SplittableRandom PARENT = new SplittableRandom();

    final SplittableRandom splittableRandom = PARENT.split();

    static SplittableRandomWrapper split() {
        return new SplittableRandomWrapper();
    }

    @Override
    public int nextInt() {
        return splittableRandom.nextInt();
    }

    @Override
    public int nextInt(int bound) {
        return splittableRandom.nextInt(bound);
    }

    @Override
    public long nextLong() {
        return splittableRandom.nextLong();
    }

    @Override
    public double nextDouble() {
        return splittableRandom.nextDouble();
    }

    @Override
    public boolean nextBoolean() {
        return splittableRandom.nextBoolean();
    }

    @Override
    public IntStream ints(long streamSize) {
        return splittableRandom.ints(streamSize);
    }

    @Override
    public IntStream ints() {
        return splittableRandom.ints();
    }

    @Override
    public IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound) {
        return splittableRandom.ints(streamSize, randomNumberOrigin, randomNumberBound);
    }

    @Override
    public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
        return splittableRandom.ints(randomNumberOrigin, randomNumberBound);
    }

    @Override
    public LongStream longs(long streamSize) {
        return splittableRandom.longs(streamSize);
    }

    @Override
    public LongStream longs() {
        return splittableRandom.longs();
    }

    @Override
    public LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound) {
        return splittableRandom.longs(streamSize, randomNumberOrigin, randomNumberBound);
    }

    @Override
    public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
        return splittableRandom.longs(randomNumberOrigin, randomNumberBound);
    }

    @Override
    public DoubleStream doubles(long streamSize) {
        return splittableRandom.doubles(streamSize);
    }

    @Override
    public DoubleStream doubles() {
        return splittableRandom.doubles();
    }

    @Override
    public DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound) {
        return splittableRandom.doubles(streamSize, randomNumberOrigin, randomNumberBound);
    }

    @Override
    public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
        return splittableRandom.doubles(randomNumberOrigin, randomNumberBound);
    }
}