package biz.k11i.rng;

import java.util.Random;

import static biz.k11i.util.MathFunctions.exp;
import static biz.k11i.util.MathFunctions.log;
import static biz.k11i.util.MathFunctions.pow;

/**
 * Beta random number generator.
 */
public interface BetaRNG {
    BetaRNG FAST_RNG = new BetaRNGImpl(GammaRNG.FAST_RNG);
    BetaRNG GENERAL_RNG = new BetaRNGImpl(GammaRNG.GENERAL_RNG);

    /**
     * Generates a random value sampled from beta distribution.
     *
     * @param random random number generator
     * @param alpha  shape parameter (alpha)
     * @param beta   shape parameter (beta)
     * @return a random value
     */
    double generate(Random random, double alpha, double beta);

    class BetaRNGImpl implements BetaRNG {
        private static final double[] CASE2_MAX_THRESHOLDS;

        static {
            CASE2_MAX_THRESHOLDS = new double[11];
            CASE2_MAX_THRESHOLDS[0] = 90.0;
            CASE2_MAX_THRESHOLDS[1 /* 0.01 */] = 70.0;
            CASE2_MAX_THRESHOLDS[2 /* 0.02 */] = 50.0;
            CASE2_MAX_THRESHOLDS[3 /* 0.03 */] = 12.0;
            CASE2_MAX_THRESHOLDS[4 /* 0.04 */] = 6.0;
            CASE2_MAX_THRESHOLDS[5 /* 0.05 */] = 3.0;
            CASE2_MAX_THRESHOLDS[6 /* 0.06 */] = 2.5;
            CASE2_MAX_THRESHOLDS[7 /* 0.07 */] = 2.0;
            CASE2_MAX_THRESHOLDS[8 /* 0.08 */] = 1.6;
            CASE2_MAX_THRESHOLDS[9 /* 0.09 */] = 1.3;
            CASE2_MAX_THRESHOLDS[10 /* 0.10 */] = 1.0;
        }

        private final BetaRNG twoGammaVariates;

        BetaRNGImpl(GammaRNG gammaRNG) {
            this.twoGammaVariates = new BetaRNGAlgorithms.TwoGammaVariates(gammaRNG);
        }

        @Override
        public double generate(Random random, double alpha, double beta) {
            return (alpha <= beta ? selectAlgorithm(alpha, beta) : selectAlgorithm(beta, alpha))
                    .generate(random, alpha, beta);
        }

        BetaRNG selectAlgorithm(double min, double max) {
            if (min > 1.0) { // case 3: max >= min > 1
                return twoGammaVariates;
            }

            if (max < 1.0) { // case 1: min <= max < 1
                return max + min <= 1.5 ? BetaRNGAlgorithms.Johnk.INSTANCE : BetaRNGAlgorithms.B00.INSTANCE;
            }

            // case 2 + special case
            // min <= 1, max >= 1

            if (min < 0.1 && max <= CASE2_MAX_THRESHOLDS[(int) (min * 100)]) {
                return BetaRNGAlgorithms.Johnk.INSTANCE;
            }

            if (max > 1.0) {
                return twoGammaVariates;
            }

            // min <= 1, max = 1

            if (min == 1.0) {
                // max = 1, min = 1
                return BetaRNGAlgorithms.Unif.INSTANCE;
            }

            return BetaRNGAlgorithms.CdfInversion.INSTANCE;
        }
    }


}

class BetaRNGAlgorithms {
    /**
     * Implementation of Beta random number generator using Jöhnk's algorithm.
     * <p>
     * Jöhnk, M. D.
     * <i>"Erzeugung von betaverteilten und gammaverteilten Zufallszahlen."</i>
     * Metrika 8.1 (1964): 5-15.
     * </p>
     */
    static class Johnk implements BetaRNG {
        static final BetaRNG INSTANCE = new Johnk();

        @Override
        public double generate(Random random, double alpha, double beta) {
            while (true) {
                double u = log(random.nextDouble()) / alpha;
                double v = log(random.nextDouble()) / beta;

                double uu = exp(u);
                double vv = exp(v);

                double w = uu + vv;
                if (w <= 1) {
                    if (w > 0) {
                        return uu / w;
                    }

                    double logM = u > v ? u : v;
                    u -= logM;
                    v -= logM;

                    return exp(u - log(exp(u) + exp(v)));
                }
            }
        }
    }

    /**
     * Implementation of Beta random number generator using Sakasegawa's B00 algorithm.
     * <p>
     * Sakasegawa, H.
     * <i>"Stratified rejection and squeeze method for generating beta random numbers."</i>
     * Annals of the Institute of Statistical Mathematics 35.1 (1983): 291-302.
     * </p>
     */
    static class B00 implements BetaRNG {
        static final BetaRNG INSTANCE = new B00();

        @Override
        public double generate(Random random, double alpha, double beta) {
            double t = (1 - alpha) / (2 - alpha - beta);
            double s = (beta - alpha) * (1 - alpha - beta);
            double r = alpha * (1 - alpha);
            t -= ((s * t + 2 * r) * t - r) / 2 * (s * t + r);
            double p = t / alpha;
            double q = (1 - t) / beta;
            s = pow((1 - t), beta - 1);
            double c = pow(t, alpha - 1);
            r = (c - 1) / (t - 1);

            while (true) {
                // step 1
                double u = random.nextDouble() * (p + q);
                double v = random.nextDouble();

                if (u <= p) {
                    // step 2
                    double x = t * pow(u / p, 1 / alpha);
                    v *= s;

                    if (v < (1 - beta) * x + 1) {
                        return x;
                    }
                    if (v < (s - 1) * x / t + 1 && v <= pow(1 - x, beta - 1)) {
                        return x;
                    }

                } else {
                    // step 3
                    double x = 1 - (1 - t) * pow((u - p) / q, 1 / beta);
                    v *= c;

                    if (v < (alpha - 1) * (x - 1) + 1) {
                        return x;
                    }
                    if (v <= r * (x - 1) + 1 && v <= pow(x, alpha - 1)) {
                        return x;
                    }
                }
            }
        }
    }

    /**
     * Generates Beta variates using two Gamma variates.
     */
    static class TwoGammaVariates implements BetaRNG {
        private final GammaRNG gammaRNG;

        TwoGammaVariates(GammaRNG gammaRNG) {
            this.gammaRNG = gammaRNG;
        }

        @Override
        public double generate(Random random, double alpha, double beta) {
            double a = gammaRNG.generate(random, alpha, 1);
            if (a == 0.0) {
                return 0.0;
            }

            return a / (a + gammaRNG.generate(random, beta, 1));
        }
    }

    /**
     * Generates Beta variates using inversion method.
     */
    static class CdfInversion implements BetaRNG {
        static final BetaRNG INSTANCE = new CdfInversion();

        @Override
        public double generate(Random random, double alpha, double beta) {
            return alpha == 1.0
                    ? 1 - pow(random.nextDouble(), 1.0 / beta)
                    : pow(random.nextDouble(), 1.0 / alpha);
        }
    }

    static class Unif implements BetaRNG {
        static final BetaRNG INSTANCE = new Unif();

        @Override
        public double generate(Random random, double ignore1, double ignore2) {
            return random.nextDouble();
        }
    }
}