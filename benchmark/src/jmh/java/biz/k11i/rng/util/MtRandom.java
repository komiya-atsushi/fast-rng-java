package biz.k11i.rng.util;

import org.apache.commons.math3.random.MersenneTwister;

import java.util.Random;

public class MtRandom extends Random {
    private final MersenneTwister mersenneTwister;

    public MtRandom() {
        mersenneTwister = new MersenneTwister();
    }

    public MtRandom(int seed) {
        mersenneTwister = new MersenneTwister(seed);
    }

    @Override
    public boolean nextBoolean() {
        return mersenneTwister.nextBoolean();
    }

    @Override
    public void nextBytes(byte[] bytes) {
        mersenneTwister.nextBytes(bytes);
    }

    @Override
    public double nextDouble() {
        return mersenneTwister.nextDouble();
    }

    @Override
    public float nextFloat() {
        return mersenneTwister.nextFloat();
    }

    @Override
    public double nextGaussian() {
        return mersenneTwister.nextGaussian();
    }

    @Override
    public int nextInt() {
        return mersenneTwister.nextInt();
    }

    @Override
    public int nextInt(int n) throws IllegalArgumentException {
        return mersenneTwister.nextInt(n);
    }

    @Override
    public long nextLong() {
        return mersenneTwister.nextLong();
    }
}
