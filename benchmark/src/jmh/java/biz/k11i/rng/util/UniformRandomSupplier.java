package biz.k11i.rng.util;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public enum UniformRandomSupplier {
    THREAD_LOCAL_RANDOM {
        @Override
        public Random get() {
            return ThreadLocalRandom.current();
        }
    },

    MERSENNE_TWISTER {
        @Override
        public Random get() {
            return new MtRandom();
        }
    };

    public abstract Random get();
}
