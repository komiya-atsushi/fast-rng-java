package biz.k11i.rng;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@State(Scope.Thread)
public class GaussianBenchmark {
    private Random javaUtilRandom = new Random();

    @Benchmark
    public double javaUtilRandom() {
        return javaUtilRandom.nextGaussian();
    }

    @Benchmark
    public double threadLocalRandom() {
        return ThreadLocalRandom.current().nextGaussian();
    }

    @Benchmark
    public double fastRng() {
        return GaussianRNG.FAST_RNG.generate(ThreadLocalRandom.current());
    }
}
