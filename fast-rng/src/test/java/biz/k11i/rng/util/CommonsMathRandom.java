package biz.k11i.rng.util;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

import java.util.Random;

public class CommonsMathRandom extends Random {
    private final RandomGenerator randomGenerator = new Well19937c();

    @Override
    public double nextDouble() {
        return randomGenerator.nextDouble();
    }

    @Override
    public long nextLong() {
        return randomGenerator.nextLong();
    }
}
