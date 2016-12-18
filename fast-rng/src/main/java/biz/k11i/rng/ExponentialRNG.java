package biz.k11i.rng;

import java.util.Random;

import static biz.k11i.util.MathFunctions.exp;
import static biz.k11i.util.MathFunctions.log;
import static biz.k11i.util.MathFunctions.log1p;

/**
 * Exponential random number generator.
 */
public interface ExponentialRNG {
    ExponentialRNG FAST_RNG = ZigguratFast.Z_256;
    ExponentialRNG GENERAL_RNG = ZigguratGeneral.Z_256;

    /**
     * Generates a random value sampled from exponential distribution.
     *
     * @param random random number generator
     * @param theta  mean of the distribution
     * @return a random value
     */
    double generate(Random random, double theta);

    abstract class ZigguratBase implements ExponentialRNG {
        final int N;
        final double R;
        final double V;
        final int INDEX_BIT_MASK;
        final int TAIL_INDEX;

        ZigguratBase(int nBits, double r, double v) {
            N = 1 << nBits;
            R = r;
            V = v;

            INDEX_BIT_MASK = (1 << nBits) - 1;
            TAIL_INDEX = (1 << nBits) - 1;
        }

        static double finv(double x, double v) {
            return -log(exp(-x) + v / x);
        }

        @Override
        public double generate(Random random, double theta) {
            return theta * generate(random, 0);
        }

        abstract double generate(Random random, int recursionCount);
    }

    /**
     * Implementation of Exponential random number generator using Ziggurat algorithm.
     * <p>
     * Tesuaki Yotsuji. <i>計算機シミュレーションのための確率分布乱数生成法.</i>
     * Pleiades PUBLISHING Co.,Ltd. (2010)
     * </p>
     * <p>
     * This implementation assumes that the values returned from {@link Random#nextLong()}
     * have the independence of each bit.
     * </p>
     */
    class ZigguratFast extends ZigguratBase implements ExponentialRNG {
        private static final ZigguratFast Z_256 = new ZigguratFast(8, 7.697117470131, 0.00394965982258);

        private final int INDEX_BITS;

        private final long[] k;
        private final double[] w;
        private final double[] f;

        ZigguratFast(int nBits, double r, double v) {
            super(nBits, r, v);

            INDEX_BITS = nBits;

            w = new double[N];
            k = new long[N];
            f = new double[N];

            long b = 1L << (64 - nBits);
            w[N - 1] = v * exp(r) / b;
            w[N - 2] = r / b;
            k[N - 1] = (long) Math.floor(r / w[N - 1]);
            f[N - 1] = exp(-r);

            double x = r;
            for (int i = N - 2; i >= 1; i--) {
                x = finv(x, v);
                w[i - 1] = x / b;
                k[i] = (long) Math.floor(x / w[i]);
                f[i] = exp(-x);
            }

            k[0] = 0;
            f[0] = 1;
        }

        @Override
        double generate(Random random, int recursiveCount) {
            while (true) {
                long u = random.nextLong();
                int i = (int) (u & INDEX_BIT_MASK);
                u >>>= INDEX_BITS;

                if (u < k[i]) {
                    return u * w[i];
                }

                if (i == TAIL_INDEX) {
                    if (recursiveCount < 2) {
                        return R + generate(random, recursiveCount + 1);
                    }
                    return R - log1p(-random.nextDouble());
                }

                double x = u * w[i];
                double fx = exp(-x);
                if (random.nextDouble() * (f[i] - f[i + 1]) <= fx - f[i + 1]) {
                    return x;
                }
            }
        }
    }

    /**
     * Implementation of Exponential random number generator using Ziggurat algorithm.
     * <p>
     * Tesuaki Yotsuji. <i>計算機シミュレーションのための確率分布乱数生成法.</i>
     * Pleiades PUBLISHING Co.,Ltd. (2010)
     * </p>
     * <p>
     * This implementation is a bit slower than {@link ZigguratFast}
     * but it does not require the independence of each bit to the values returned from {@link Random#nextLong()}.
     * </p>
     */
    class ZigguratGeneral extends ZigguratBase implements ExponentialRNG {
        private static final ZigguratGeneral Z_256 = new ZigguratGeneral(8, 7.697117470131, 0.00394965982258);

        private final double[] x;
        private final double[] t;

        ZigguratGeneral(int nBits, double r, double v) {
            super(nBits, r, v);

            x = new double[N + 1];
            t = new double[N];

            x[N] = v * exp(r);
            x[N - 1] = r;
            for (int i = N - 2; i >= 1; i--) {
                x[i] = finv(x[i + 1], v);
            }
            x[0] = 0;

            for (int i = 0; i < N; i++) {
                t[i] = x[i] / x[i + 1];
            }
        }

        @Override
        double generate(Random random, int recursiveCount) {
            while (true) {
                int i = (int) (random.nextLong() & INDEX_BIT_MASK);
                double u1 = random.nextDouble();

                if (u1 < t[i]) {
                    return u1 * x[i + 1];
                }

                if (i == TAIL_INDEX) {
                    if (recursiveCount < 2) {
                        return R + generate(random, recursiveCount + 1);
                    }
                    return R - log1p(-random.nextDouble());
                }

                double y = u1 * x[i + 1];
                double gu = exp(-(x[i] - y));
                double gl = exp(-(x[i + 1] - y));
                if (random.nextDouble() * (gu - gl) <= 1 - gl) {
                    return y;
                }
            }
        }
    }
}
