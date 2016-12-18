package biz.k11i.rng;

import biz.k11i.rng.util.ThreadLocalRandomGenerator;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.concurrent.ThreadLocalRandom;

@State(Scope.Benchmark)
public class ExponentialBenchmark {
    private ExponentialDistribution exponentialDistribution;

    @Setup
    public void setUp() {
        exponentialDistribution = new ExponentialDistribution(
                new ThreadLocalRandomGenerator(),
                1.0,
                ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    @Benchmark
    public double commonsMath3_algorithmSA() {
        return exponentialDistribution.sample();
    }

    @Benchmark
    public double fastRng_fast() {
        return ExponentialRNG.FAST_RNG.generate(ThreadLocalRandom.current(), 1.0);
    }

    @Benchmark
    public double fastRng_general() {
        return ExponentialRNG.GENERAL_RNG.generate(ThreadLocalRandom.current(), 1.0);
    }
}
