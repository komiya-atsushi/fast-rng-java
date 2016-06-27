package biz.k11i.rng;

import java.util.Random;

/**
 * Gaussian random number generator.
 */
public interface GaussianRNG {
    GaussianRNG FAST_RNG = ZigguratRNG.Z_256;

    /**
     * Generates a random value sampled from gaussian distribution (normal distribution).
     *
     * @param random random number generator
     * @return a random value
     */
    double generate(Random random);

    /**
     * Implementation of Gaussian random number generator using Ziggurat algorithm.
     * <p>
     * Marsaglia, George, and Wai Wan Tsang.
     * <i>"The ziggurat method for generating random variables."</i>
     * Journal of statistical software 5.8 (2000): 1-7.
     * </p>
     */
    class ZigguratRNG implements GaussianRNG {
        private static final ZigguratRNG Z_256 = new ZigguratRNG(256, 3.6541528853610088, 0.00492867323399);

        private final int INDEX_BITMASK;
        private final int N;
        private final double R;
        private final double V;

        private long[] k;
        private double[] w;
        private double[] f;

        /**
         * Constructs {@link ZigguratRNG} with parameters.
         *
         * @param n number of rectangles (assume that n is power of 2)
         * @param r rightmost x_i
         * @param v area of the rectangle
         */
        ZigguratRNG(int n, double r, double v) {
            INDEX_BITMASK = n - 1;
            N = n;
            R = r;
            V = v;

            k = new long[n];
            w = new double[n];
            f = new double[n];

            double fr = f(r);

            k[0] = (long) (Long.MAX_VALUE * r * fr / v);
            k[1] = 0;

            w[0] = v / fr / Long.MAX_VALUE;
            w[n - 1] = r / Long.MAX_VALUE;

            f[0] = 1.0;
            f[n - 1] = fr;

            double dn = r;
            double tn = r;

            for (int i = n - 2; i >= 1; i--) {
                dn = Math.sqrt(-2.0 * Math.log(v / dn + f(dn)));

                k[i + 1] = (long) (Long.MAX_VALUE * dn / tn);
                tn = dn;

                w[i] = dn / Long.MAX_VALUE;
                f[i] = f(dn);
            }
        }

        private double f(double x) {
            // f(x) = e^{-x^2 / 2}
            return Math.exp(-0.5 * x * x);
        }

        public double generate(Random random) {
            while (true) {
                long j = random.nextLong();
                if (j == Long.MIN_VALUE) {
                    continue;
                }

                int i = (int) (j & INDEX_BITMASK);

                if (Math.abs(j) < k[i]) {
                    return j * w[i];
                }

                if (i == 0) {
                    double _x, _y;
                    do {
                        _x = -Math.log(random.nextDouble()) / R;
                        _y = -Math.log(random.nextDouble());

                    } while (_y + _y < _x * _x);
                    return j > 0 ? R + _x : -(R + _x);
                }

                double x = j * w[i];
                if ((f[i - 1] - f[i]) * random.nextDouble() < f(x) - f[i]) {
                    return x;
                }
            }
        }

        @Override
        public String toString() {
            return String.format("ZigguratRNG(N = %d, R = %f, V = %f)", N, R, V);
        }
    }
}
