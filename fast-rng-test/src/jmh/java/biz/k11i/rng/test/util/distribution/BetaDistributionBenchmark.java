package biz.k11i.rng.test.util.distribution;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BetaDistributionBenchmark {
    @Param({"0.1", "1.0", "100.0"})
    private double alpha;

    @Param({"0.1", "1.0", "100.0"})
    private double beta;

    private ContinuousDistribution probDist;
    private BetaDistribution commonsMath3;

    @State(Scope.Benchmark)
    public static class X {
        private final double[] values;
        private int index;
        private double next;

        public X() {
            final int N = 100000;
            values = IntStream.rangeClosed(0, N)
                    .mapToDouble(i -> i / (double) N)
                    .toArray();
        }

        @Setup(Level.Iteration)
        public void init() {
            index = 0;
        }

        @Setup(Level.Invocation)
        public void next() {
            next = values[index];

            if (++index >= values.length) {
                index = 0;
            }
        }
    }

    @Setup(Level.Trial)
    public void setUp() {
        probDist = ProbabilityDistributions.beta(alpha, beta);
        commonsMath3 = new BetaDistribution(alpha, beta);
    }

    @Benchmark
    public double commonsMath3Cdf(X x) {
        return commonsMath3.cumulativeProbability(x.next);
    }

    @Benchmark
    public double commonsMath3InverseCdf(X x) {
        return commonsMath3.inverseCumulativeProbability(x.next);
    }

    @Benchmark
    public double cdf(X x) {
        return probDist.cdf(x.next);
    }

    @Benchmark
    public double inverseCdf(X x) {
        return probDist.inverseCdf(x.next);
    }
}
