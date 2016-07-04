package biz.k11i.rng;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.Random;

@State(Scope.Benchmark)
public class GammaBenchmark {
    @Param({"0.01", "0.1", "1.0", "10.0", "100.0"})
    public double shape;
    public double scale = 1.0;

    private Random random;
    private GammaDistribution gammaDistribution;

    @Setup
    public void setUp() {
        random = new Random();
        gammaDistribution = new GammaDistribution(
                new JDKRandomGenerator(),
                shape,
                scale,
                GammaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    @Benchmark
    public double commonsMath3() {
        return gammaDistribution.sample();
    }

    @Benchmark
    public double fastRng() {
        return GammaRNG.FAST_RNG.generate(random, shape, scale);
    }
}
