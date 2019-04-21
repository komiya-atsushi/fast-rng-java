package biz.k11i.rng;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.SplittableRandom;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class NextIntWithBoundBenchmark {
    @State(Scope.Benchmark)
    public static class FixedBound {
        @Param({"1025", // 2^10 + 1
                "1048577", // 2^20 + 1
                "1073741828", // 2^30 + 4
        })
        private int bound;

        @Benchmark
        public int jdk() {
            return ThreadLocalRandom.current().nextInt(bound);
        }

        @Benchmark
        public int nearlyDivisionless() {
            return UniformRNGUtils.nextInt(ThreadLocalRandom.current(), bound);
        }
    }

    @State(Scope.Benchmark)
    public static class ArbitraryBounds {
        private static final int NUM_BOUNDS = 1 << 10;
        private int[] bounds;
        private int index;

        @Setup
        public void setUp() {
            SplittableRandom r = new SplittableRandom(12345);
            bounds = IntStream
                    .generate(() -> r.nextInt(Integer.MAX_VALUE - 1) + 1)
                    .limit(NUM_BOUNDS)
                    .toArray();
        }

        @Benchmark
        public int jdk() {
            index = (index + 1) & (NUM_BOUNDS - 1);
            return ThreadLocalRandom.current().nextInt(bounds[index]);
        }

        @Benchmark
        public int nearlyDivisionless() {
            index = (index + 1) & (NUM_BOUNDS - 1);
            return UniformRNGUtils.nextInt(ThreadLocalRandom.current(), bounds[index]);
        }
    }
}
