package biz.k11i.rng;

import java.util.Random;

import static biz.k11i.util.MathFunctions.*;
import static java.lang.Math.sqrt;

/**
 * Gamma random number generator.
 */
public interface GammaRNG {
    GammaRNG FAST_RNG = new FastRNG();
    GammaRNG GENERAL_RNG = new GeneralRNG();

    /**
     * Generates a random value sampled from gamma distribution.
     *
     * @param random random number generator
     * @param shape  shape parameter (alpha)
     * @param scale  scale parameter (beta)
     * @return a random value
     */
    double generate(Random random, double shape, double scale);

    class FastRNG implements GammaRNG {
        private final GammaRNG mt = new GammaRNGAlgorithms.MarsagliaTsang(GaussianRNG.FAST_RNG);
        private final GammaRNG exponential = new GammaRNGAlgorithms.Exponential(ExponentialRNG.FAST_RNG);
        private final GammaRNG wh = new GammaRNGAlgorithms.WilsonHilfertyApproximation(GaussianRNG.FAST_RNG);

        @Override
        public double generate(Random random, double shape, double scale) {
            if (shape >= 50) {
                return wh.generate(random, shape, scale);
            }
            if (shape != 1.0) {
                return mt.generate(random, shape, scale);
            }

            // shape == 1.0
            return exponential.generate(random, shape, scale);
        }
    }

    class GeneralRNG implements GammaRNG {
        private final GammaRNG best = new GammaRNGAlgorithms.Best();
        private final GammaRNG mt = new GammaRNGAlgorithms.MarsagliaTsang(GaussianRNG.GENERAL_RNG);
        private final GammaRNG exponential = new GammaRNGAlgorithms.Exponential(ExponentialRNG.GENERAL_RNG);
        private final GammaRNG wh = new GammaRNGAlgorithms.WilsonHilfertyApproximation(GaussianRNG.GENERAL_RNG);

        @Override
        public double generate(Random random, double shape, double scale) {
            if (shape >= 50) {
                return wh.generate(random, shape, scale);
            }
            if (shape < 0.1) {
                return best.generate(random, shape, scale);
            }
            if (shape != 1.0) {
                return mt.generate(random, shape, scale);
            }

            // shape == 1.0
            return exponential.generate(random, shape, scale);
        }
    }
}

class GammaRNGAlgorithms {
    abstract static class BaseGammaRNG implements GammaRNG {
        @Override
        public double generate(Random random, double shape, double scale) {
            return generate(random, shape) * scale;
        }

        abstract double generate(Random random, double shape);

        @Override
        public String toString() {
            return String.format("%s", this.getClass().getSimpleName());
        }
    }

    /**
     * Implementation of Gamma random number generator using Best's algorithm (1983).
     * <p>
     * Best, D. J.
     * <i>“A note on gamma variate generators with shape parameter less than unity.”</i>
     * Computing 30.2 (1983): 185-188.
     * </p>
     */
    static class Best extends BaseGammaRNG {
        @Override
        double generate(Random random, double shape) {
            double c1 = 0.07 + 0.75 * sqrt(1 - shape);
            double c2 = 1 + shape * exp(-c1) / c1;
            double c3 = 1.0 / shape;

            while (true) {
                double u1 = random.nextDouble();
                double u2 = random.nextDouble();
                double v = c2 * u1;

                if (v <= 1) {
                    double x = c1 * pow(v, c3);
                    if (u2 <= (2 - x) / (2 + x) || u2 <= exp(-x)) {
                        return x;
                    }
                } else {
                    double x = -log(c1 * c3 * (c2 - v));
                    double y = x / c1;
                    if (u2 * (shape + y - shape * y) <= 1 || u2 < pow(y, shape - 1)) {
                        return x;
                    }
                }
            }
        }
    }

    static class Exponential implements GammaRNG {
        private final ExponentialRNG exponentialRNG;

        Exponential(ExponentialRNG exponentialRNG) {
            this.exponentialRNG = exponentialRNG;
        }

        @Override
        public double generate(Random random, double shape, double scale) {
            return exponentialRNG.generate(random, 1.0) * scale;
        }

        @Override
        public String toString() {
            return String.format("Exponential[%s]", exponentialRNG.getClass().getSimpleName());
        }
    }

    /**
     * Implementation of Gamma random number generator using Marsaglia and Tsang's algorithm (2000).
     * <p>
     * Marsaglia, George, and Wai Wan Tsang.
     * <i>"A simple method for generating gamma variables."</i>
     * ACM Transactions on Mathematical Software (TOMS) 26.3 (2000): 363-372.
     * </p>
     */
    static class MarsagliaTsang extends BaseGammaRNG {
        private final GaussianRNG gaussianRNG;

        MarsagliaTsang(GaussianRNG gaussianRNG) {
            this.gaussianRNG = gaussianRNG;
        }

        @Override
        double generate(Random random, double shape) {
            if (shape >= 1) {
                return generateMT(random, shape);
            }

            double r = generateMT(random, shape + 1);
            double u = random.nextDouble();

            if (shape != 0.5) {
                return r * pow(u, 1.0 / shape);
            }

            // shape == 0.5
            return r * u * u;
        }

        double generateMT(Random random, double shape) {
            double d = shape - 1.0 / 3;
            double c = 1 / sqrt(9 * d);

            while (true) {
                double x = gaussianRNG.generate(random);
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

        @Override
        public String toString() {
            return String.format("%s[%s]",
                    getClass().getSimpleName(),
                    this.gaussianRNG.getClass().getSimpleName());
        }
    }

    /**
     * Implementation of Gamma random number generator using Wilson-Hilferty approximation
     * for large shape parameter (>= 50).
     * <p>
     * Wilson, Edwin B., and Margaret M. Hilferty.
     * <i>“The distribution of chi-square.”</i>
     * Proceedings of the National Academy of Sciences 17.12 (1931): 684-688.
     * </p>
     */
    static class WilsonHilfertyApproximation extends BaseGammaRNG {
        private final GaussianRNG gaussianRNG;

        WilsonHilfertyApproximation(GaussianRNG gaussianRNG) {
            this.gaussianRNG = gaussianRNG;
        }

        @Override
        double generate(Random random, double shape) {
            double t0 = 1.0 / (9.0 * shape);
            double t1 = 1.0 - t0;
            double t2 = sqrt(t0);

            while (true) {
                double t = t1 + t2 * gaussianRNG.generate(random);
                if (t <= 0) {
                    continue;
                }

                return shape * t * t * t;
            }
        }

        @Override
        public String toString() {
            return String.format("WilsonHilfertyApproximation[%s]", gaussianRNG.getClass().getSimpleName());
        }
    }
}