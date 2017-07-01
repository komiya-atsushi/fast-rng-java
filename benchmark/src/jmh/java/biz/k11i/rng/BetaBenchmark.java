package biz.k11i.rng;

import biz.k11i.rng.util.MtRandom;
import biz.k11i.rng.util.ParameterPool;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.Random;

public class BetaBenchmark {
    @State(Scope.Benchmark)
    public static class FixedParameters {
        @Param({"0.1:0.1",
                "0.1:0.5",
                "0.1:0.99999",
                "0.5:0.5",
                "0.5:0.99999",
                "0.99999:0.99999",

                "0.05:2.0",
                "0.2:20.0",
                "0.8:200.0",

                "1.0:2.0",
                "2.0:3.0",
                "20.0:30.0",
                "200.0:300.0"
        })
        public String parameters;
        private double alpha;
        private double beta;

        private Random random = new MtRandom();
        private BetaDistribution commonsMath;

        @Setup
        public void setUp() {
            String[] items = parameters.split(":");
            alpha = Double.valueOf(items[0]);
            beta = Double.valueOf(items[1]);
            commonsMath = new BetaDistribution(alpha, beta);
        }

        @Benchmark
        public double commonsMath() {
            return commonsMath.sample();
        }

        @Benchmark
        public double fastRng() {
            return BetaRNG.FAST_RNG.generate(random, alpha, beta);
        }

        @Benchmark
        public double generalRng() {
            return BetaRNG.GENERAL_RNG.generate(random, alpha, beta);
        }
    }

    @State(Scope.Benchmark)
    public static class ArbitraryParameters {
        private Random random = new MtRandom();
        private RandomGenerator randomGenerator = new MersenneTwister();

        private ParameterPool alphaParameters = new ParameterPool(12345, 10000, 10.0);
        private ParameterPool betaParameters = new ParameterPool(23456, 9997, 100.0);

        @Benchmark
        public double commonsMath() {
            return new BetaDistribution(randomGenerator, alphaParameters.next(), betaParameters.next(), BetaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY).sample();
        }

        @Benchmark
        public double fastRng() {
            return BetaRNG.FAST_RNG.generate(random, alphaParameters.next(), betaParameters.next());
        }

        @Benchmark
        public double generalRng() {
            return BetaRNG.GENERAL_RNG.generate(random, alphaParameters.next(), betaParameters.next());
        }
    }
}
