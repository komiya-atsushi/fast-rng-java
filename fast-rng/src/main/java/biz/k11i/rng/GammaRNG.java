package biz.k11i.rng;

import java.util.Random;

import static biz.k11i.rng.MathFunctions.exp;
import static biz.k11i.rng.MathFunctions.log;
import static biz.k11i.rng.MathFunctions.pow;
import static java.lang.Math.E;
import static java.lang.Math.sqrt;

/**
 * Gamma random number generator.
 */
public interface GammaRNG {
    GammaRNG FAST_RNG = new HybridRNG();

    /**
     * Generates a random value sampled from gamma distribution.
     *
     * @param random random number generator
     * @param shape  shape parameter (alpha)
     * @param scale  scale parameter (beta)
     * @return a random value
     */
    double generate(Random random, double shape, double scale);

    class HybridRNG implements GammaRNG {
        @Override
        public double generate(Random random, double shape, double scale) {
            if (shape >= 1.0) {
                return generateMT(random, shape) * scale;

            } else if (shape < 0.5) { // (0.0, 0.5)
                return generateAD(random, shape) * scale;

            } else if (shape == 0.5) {
                double u = random.nextDouble();
                return generateMT(random, shape + 1) * scale * u * u;

            } else { // (0.5, 1.0)
                return generateMT(random, shape + 1) * scale * pow(random.nextDouble(), 1.0 / shape);
            }
        }

        private static final double IE = 1.0 / E;

        /**
         * Implementation of Gamma random number generator using Ahrens and Dieter (1974).
         * <p>
         * Ahrens, Joachim H., and Ulrich Dieter.
         * <i>"Computer methods for sampling from gamma, beta, poisson and bionomial distributions."</i>
         * Computing 12.3 (1974): 223-246.
         * </p>
         *
         * @param random random number generator
         * @param alpha  shape parameter
         * @return a random value
         */
        private double generateAD(Random random, double alpha) {
            double ia = 1.0 / alpha;
            double ic = 1 + alpha * IE;

            while (true) {
                double u1 = random.nextDouble();
                double u2 = random.nextDouble();

                double t = u1 * ic;

                if (t <= 1) {
                    double x = pow(t, ia);
                    if (u2 <= exp(-x)) {
                        return x;
                    }

                } else {
                    double x = -log((1 - u1) * (ia + IE));
                    if (u2 <= pow(x, alpha - 1)) {
                        return x;
                    }
                }
            }
        }

        /**
         * Implementation of Gamma random number generator using Marsaglia and Tsang algorithm (2000).
         * <p>
         * Marsaglia, George, and Wai Wan Tsang.
         * <i>"A simple method for generating gamma variables."</i>
         * ACM Transactions on Mathematical Software (TOMS) 26.3 (2000): 363-372.
         * </p>
         *
         * @param random random number generator
         * @param alpha  shape parameter
         * @return a random value
         */
        private double generateMT(Random random, double alpha) {
            double d = alpha - 1.0 / 3;
            double c = 1 / sqrt(9 * d);

            while (true) {
                double x = GaussianRNG.FAST_RNG.generate(random);
                double v = 1 + c * x;
                if (v <= 0) {
                    continue;
                }

                v = v * v * v;
                x = x * x;

                double u = random.nextDouble();
                if (u < 1 - 0.0331 * x * x) {
                    return d * v;
                }

                if (log(u) < 0.5 * x + d * (1 - v + log(v))) {
                    return d * v;
                }
            }
        }
    }
}
