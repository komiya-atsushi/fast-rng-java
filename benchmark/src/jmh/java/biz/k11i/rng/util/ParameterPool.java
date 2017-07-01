package biz.k11i.rng.util;

import biz.k11i.rng.ExponentialRNG;
import biz.k11i.rng.util.MtRandom;

public class ParameterPool {
    private double[] parameters;
    private int index;

    public ParameterPool(int seed, int count, double theta) {
        parameters = new double[count];
        MtRandom r = new MtRandom(seed);

        for (int i = 0; i < count; i++) {
            parameters[i] = Math.nextUp(ExponentialRNG.FAST_RNG.generate(r, theta));
        }
    }

    public double next() {
        if (index >= parameters.length) {
            index = 0;
        }

        return parameters[index++];
    }
}
