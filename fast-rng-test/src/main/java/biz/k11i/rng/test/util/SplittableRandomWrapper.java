package biz.k11i.rng.test.util;

import java.util.Random;
import java.util.SplittableRandom;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class SplittableRandomWrapper extends Random implements ComputationAndSorting.Splittable<SplittableRandomWrapper> {
    private final SplittableRandom splittableRandom;

    @Override
    public SplittableRandomWrapper split() {
        return new SplittableRandomWrapper(splittableRandom.split());
    }

    private SplittableRandomWrapper(SplittableRandom splittableRandom) {
        this.splittableRandom = splittableRandom;
    }

    public SplittableRandomWrapper(long seed) {
        this.splittableRandom = new SplittableRandom(seed);
    }

    @Override
    public int nextInt() {
        return splittableRandom.nextInt();
    }

    @Override
    public int nextInt(int bound) {
        return splittableRandom.nextInt(bound);
    }

    @Override
    public long nextLong() {
        return splittableRandom.nextLong();
    }

    @Override
    public double nextDouble() {
        return splittableRandom.nextDouble();
    }

    @Override
    public boolean nextBoolean() {
        return splittableRandom.nextBoolean();
    }

    @Override
    public IntStream ints(long streamSize) {
        return splittableRandom.ints(streamSize);
    }

    @Override
    public IntStream ints() {
        return splittableRandom.ints();
    }

    @Override
    public IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound) {
        return splittableRandom.ints(streamSize, randomNumberOrigin, randomNumberBound);
    }

    @Override
    public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
        return splittableRandom.ints(randomNumberOrigin, randomNumberBound);
    }

    @Override
    public LongStream longs(long streamSize) {
        return splittableRandom.longs(streamSize);
    }

    @Override
    public LongStream longs() {
        return splittableRandom.longs();
    }

    @Override
    public LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound) {
        return splittableRandom.longs(streamSize, randomNumberOrigin, randomNumberBound);
    }

    @Override
    public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
        return splittableRandom.longs(randomNumberOrigin, randomNumberBound);
    }

    @Override
    public DoubleStream doubles(long streamSize) {
        return splittableRandom.doubles(streamSize);
    }

    @Override
    public DoubleStream doubles() {
        return splittableRandom.doubles();
    }

    @Override
    public DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound) {
        return splittableRandom.doubles(streamSize, randomNumberOrigin, randomNumberBound);
    }

    @Override
    public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
        return splittableRandom.doubles(randomNumberOrigin, randomNumberBound);
    }
}
