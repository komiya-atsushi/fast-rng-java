package biz.k11i.rng;

import biz.k11i.rng.util.MtRandom;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.Random;

public class GammaBenchmark {
    @State(Scope.Benchmark)
    public static class FixedParameters {
        @Param({"0.05", "0.1", "0.2", "0.5", "0.9", "1.0", "1.1", "40.0", "10000.0"})
        public double shape;
        public double scale = 1.0;

        private Random random = new MtRandom();
        private GammaDistribution gammaDistribution;

        @Setup
        public void setUp() {
            gammaDistribution = new GammaDistribution(
                    new MersenneTwister(),
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

        @Benchmark
        public double generalRng() {
            return GammaRNG.GENERAL_RNG.generate(random, shape, scale);
        }
    }

    public static class ParameterPool {
        private double[] parameters;
        private int index;

        ParameterPool(int seed, int count) {
            parameters = new double[count];
            MtRandom r = new MtRandom(seed);

            for (int i = 0; i < count; i++) {
                parameters[i] = Math.nextUp(ExponentialRNG.FAST_RNG.generate(r, 10.0));
            }
        }

        double next() {
            if (index >= parameters.length) {
                index = 0;
            }

            return parameters[index++];
        }
    }

    @State(Scope.Benchmark)
    public static class ArbitraryParameters {
        private double scale = 1.0;
        private Random random = new MtRandom();
        private MersenneTwister mersenneTwister = new MersenneTwister();
        private ParameterPool parameters = new ParameterPool(12345, 10000);

        @Benchmark
        public double commonsMath3() {
            return new GammaDistribution(mersenneTwister, parameters.next(), scale).sample();
        }

        @Benchmark
        public double fastRng() {
            return GammaRNG.FAST_RNG.generate(random, parameters.next(), scale);
        }

        @Benchmark
        public double generalRng() {
            return GammaRNG.GENERAL_RNG.generate(random, parameters.next(), scale);
        }
    }
}
