package biz.k11i.rng;

import java.util.Random;

import static biz.k11i.util.MathFunctions.exp;
import static biz.k11i.util.MathFunctions.log;

/**
 * Gaussian random number generator.
 */
public interface GaussianRNG {
    GaussianRNG FAST_RNG = ZigguratFast.Z_256;
    GaussianRNG GENERAL_RNG = ZigguratGeneral.Z_256;

    /**
     * Generates a random value sampled from gaussian distribution (normal distribution).
     *
     * @param random random number generator
     * @return a random value
     */
    double generate(Random random);

    abstract class ZigguratBase {
        static double f(double x) {
            // f(x) = e^{-x^2 / 2}
            return exp(-0.5 * x * x);
        }

        static double tail(Random random, double r) {
            double _x, _y;
            do {
                _x = -log(random.nextDouble()) / r;
                _y = -log(random.nextDouble());

            } while (_y + _y < _x * _x);
            return r + _x;
        }
    }

    /**
     * Implementation of Gaussian random number generator using Ziggurat algorithm.
     * <p>
     * Marsaglia, George, and Wai Wan Tsang.
     * <i>"The ziggurat method for generating random variables."</i>
     * Journal of statistical software 5.8 (2000): 1-7.
     * </p>
     * <p>
     * Tesuaki Yotsuji. <i>計算機シミュレーションのための確率分布乱数生成法.</i>
     * Pleiades PUBLISHING Co.,Ltd. (2010)
     * </p>
     * <p>
     * This implementation assumes that the values returned from {@link Random#nextLong()}
     * have the independence of each bit.
     * </p>
     */
    class ZigguratFast extends ZigguratBase implements GaussianRNG {
        private static final ZigguratFast Z_256 = new ZigguratFast(8, 3.6541528853610088, 0.00492867323399);

        private final int N;
        private final double R;
        private final double V;
        private final int INDEX_RIGHT_SHIFT_BITS;
        private final long SIGN_BIT_MASK;
        private final long U_BIT_MASK;
        private final int TAIL_INDEX;

        private final long[] k;
        private final double[] w;
        private final double[] f;

        /**
         * Constructs {@link ZigguratFast} with parameters.
         *
         * @param nBits number of rectangles (2^nBits)
         * @param r     rightmost x_i
         * @param v     area of the rectangle
         */
        ZigguratFast(int nBits, double r, double v) {
            N = 1 << nBits;
            R = r;
            V = v;
            INDEX_RIGHT_SHIFT_BITS = 64 - nBits;
            SIGN_BIT_MASK = 1L << (64 - nBits - 1);
            U_BIT_MASK = (1L << (64 - nBits - 1)) - 1;
            TAIL_INDEX = N - 1;

            k = new long[N];
            w = new double[N];
            f = new double[N];

            double fr = f(r);
            long b = 1L << (64 - 8 - 1);

            w[N - 1] = v * exp(0.5 * r * r) / b;
            w[N - 2] = r / b;
            k[N - 1] = (long) Math.floor(r / w[N - 1]);
            f[N - 1] = fr;

            double x = r;

            for (int i = N - 2; i >= 1; i--) {
                x = Math.sqrt(-2.0 * log(f(x) + v / x));
                w[i - 1] = x / b;
                k[i] = (long) Math.floor(x / w[i]);
                f[i] = f(x);
            }

            k[0] = 0;
            f[0] = 1;
        }

        public double generate(Random random) {
            while (true) {
                long u = random.nextLong();
                int i = (int) (u >>> INDEX_RIGHT_SHIFT_BITS);
                int sign = (u & SIGN_BIT_MASK) == 0 ? 1 : -1;
                u &= U_BIT_MASK;

                if (u < k[i]) {
                    return sign * u * w[i];
                }

                if (i == TAIL_INDEX) {
                    return sign * tail(random, R);
                }

                double x = u * w[i];
                if (random.nextDouble() * (f[i] - f[i + 1]) <= f(x) - f[i + 1]) {
                    return sign * x;
                }
            }
        }

        @Override
        public String toString() {
            return String.format("ZigguratFast(N = %d, R = %f, V = %f)", N, R, V);
        }
    }

    /**
     * Implementation of Gaussian random number generator using Ziggurat algorithm.
     * <p>
     * Marsaglia, George, and Wai Wan Tsang.
     * <i>"The ziggurat method for generating random variables."</i>
     * Journal of statistical software 5.8 (2000): 1-7.
     * </p>
     * <p>
     * Tesuaki Yotsuji. <i>計算機シミュレーションのための確率分布乱数生成法.</i>
     * Pleiades PUBLISHING Co.,Ltd. (2010)
     * </p>
     * <p>
     * This implementation is a bit slower than {@link ZigguratFast}
     * but it does not require the independence of each bit to the values returned from {@link Random#nextLong()}.
     * </p>
     */
    class ZigguratGeneral extends ZigguratBase implements GaussianRNG {
        private static final ZigguratGeneral Z_256 = new ZigguratGeneral(8, 3.6541528853610088, 0.00492867323399);

        private final int N;
        private final double R;
        private final double V;
        private final int INDEX_BIT_MASK;
        private final int TAIL_INDEX;

        private final double[] x;
        private final double[] xx;
        private final double[] t;

        /**
         * Constructs {@link ZigguratGeneral} with parameters.
         *
         * @param nBits number of rectangles (2^nBits)
         * @param r     rightmost x_i
         * @param v     area of the rectangle
         */
        ZigguratGeneral(int nBits, double r, double v) {
            N = 1 << nBits;
            R = r;
            V = v;
            INDEX_BIT_MASK = N - 1;
            TAIL_INDEX = N - 1;

            x = new double[N + 1];
            xx = new double[N + 1];
            t = new double[N];

            x[N] = v * exp(0.5 * r * r);
            x[N - 1] = r;

            for (int i = N - 2; i >= 1; i--) {
                x[i] = Math.sqrt(-2.0 * log(f(x[i + 1]) + v / x[i + 1]));
            }
            x[0] = 0;

            for (int i = 0; i < t.length; i++) {
                t[i] = x[i] / x[i + 1];
            }

            for (int i = 0; i < x.length; i++) {
                xx[i] = x[i] * x[i];
            }
        }

        @Override
        public double generate(Random random) {
            while (true) {
                int i = random.nextInt() & INDEX_BIT_MASK;

                double u1 = 2 * random.nextDouble() - 1;
                if (Math.abs(u1) < t[i]) {
                    return u1 * x[i + 1];
                }

                if (i == TAIL_INDEX) {
                    return Math.signum(u1) * tail(random, R);
                }

                double y = u1 * x[i + 1];
                double yy = y * y;
                double gU = exp(-0.5 * (xx[i] - yy));
                double gL = exp(-0.5 * (xx[i + 1] - yy));

                if (random.nextDouble() * (gU - gL) <= 1 - gL) {
                    return y;
                }
            }
        }

        @Override
        public String toString() {
            return String.format("ZigguratGeneral(N = %d, R = %f, V = %f)", N, R, V);
        }
    }
}
