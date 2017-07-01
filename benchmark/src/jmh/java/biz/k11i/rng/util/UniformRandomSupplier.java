package biz.k11i.rng.util;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public enum UniformRandomSupplier {
    THREAD_LOCAL_RANDOM() {
        @Override
        Random newRandom() {
            return ThreadLocalRandom.current();
        }

        @Override
        RandomGenerator newRandomGenerator() {
            return new ThreadLocalRandomGenerator();
        }
    },
    MERSENNE_TWISTER() {
        @Override
        Random newRandom() {
            return new MtRandom();
        }

        @Override
        RandomGenerator newRandomGenerator() {
            return new MersenneTwister();
        }
    }
    ;

    abstract Random newRandom();

    abstract RandomGenerator newRandomGenerator();
}
