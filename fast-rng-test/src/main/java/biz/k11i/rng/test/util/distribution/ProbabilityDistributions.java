package biz.k11i.rng.test.util.distribution;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.util.ContinuedFraction;
import org.apache.commons.math3.util.FastMath;

/**
 * Provides probability distributions that can compute cumulative probability function and inverse distribution function.
 *
 * <p>
 * Some implementations of the probability distribution came from the
 * <a href="https://commons.apache.org/proper/commons-math/">Apache Commons Math</a> that is licensed under the Apache License 2.0.
 * </p>
 * <pre>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </pre>
 * <p>
 * <ul>
 *     <li>{@link Gaussian}</li>
 *     <li>{@link Gamma}</li>
 *     <li>{@link Beta}</li>
 * </ul>
 * </p>
 */
public interface ProbabilityDistributions {
    static ContinuousDistribution gaussian(double mean, double sd) {
        return new Gaussian(mean, sd);
    }

    static ContinuousDistribution gamma(double shape, double scale) {
        return new Gamma(shape, scale);
    }

    static ContinuousDistribution beta(double alpha, double beta) {
        return new Beta(alpha, beta);
    }

    static ContinuousDistribution wrap(RealDistribution distribution) {
        return new CommonsMath3DistributionWrapper.Continuous(distribution);
    }

    static DiscreteDistribution wrap(IntegerDistribution distribution) {
        return new CommonsMath3DistributionWrapper.Discrete(distribution);
    }
}

/**
 * Implementation of Gaussian distrbution.
 * <p>
 * This class includes the code came from {@link org.apache.commons.math3.distribution.NormalDistribution},
 * {@link org.apache.commons.math3.special.Gamma} and {@link Erf}.
 * </p>
 */
class Gaussian implements ContinuousDistribution {
    private static final double EPSILON = 1.0e-15;
    private static final int MAX_ITERATIONS = 10000;
    private static final double SQRT2 = FastMath.sqrt(2.0);
    private static final double A = 0.5;
    private static final double LOG_GAMMA_A = org.apache.commons.math3.special.Gamma.logGamma(A);
    private static final ContinuedFraction fraction = new ContinuedFraction() {
        /** {@inheritDoc} */
        @Override
        protected double getA(int n, double x) {
            return ((2.0 * n) + 1.0) - A + x;
        }

        /** {@inheritDoc} */
        @Override
        protected double getB(int n, double x) {
            return n * (A - n);
        }
    };

    private final double mean;
    private final double sd;


    Gaussian(double mean, double sd) {
        this.mean = mean;
        this.sd = sd;
    }

    @Override
    public double cdf(double x) {
        final double dev = x - mean;
        if (FastMath.abs(dev) > 40 * sd) {
            return dev < 0 ? 0.0d : 1.0d;
        }
        return 0.5 * erfc(-dev / (sd * SQRT2));
    }

    @Override
    public double inverseCdf(double p) {
        if (p < 0.0 || p > 1.0) {
            throw new OutOfRangeException(p, 0, 1);
        }
        return mean + sd * SQRT2 * Erf.erfInv(2 * p - 1);
    }

    private static double erfc(double x) {
        if (FastMath.abs(x) > 40) {
            return x > 0 ? 0 : 2;
        }
        final double ret = regularizedGammaQ(x * x);
        return x < 0 ? 2 - ret : ret;
    }

    private static double regularizedGammaP(double x) {
        double ret;

        // calculate series
        double n = 0.0; // current element index
        double an = 1.0 / A; // n-th element in the series
        double sum = an; // partial sum
        while (FastMath.abs(an / sum) > EPSILON &&
                n < MAX_ITERATIONS &&
                sum < Double.POSITIVE_INFINITY) {
            // compute next element in the series
            n += 1.0;
            an *= x / (A + n);

            // update partial sum
            sum += an;
        }
        if (n >= MAX_ITERATIONS) {
            throw new MaxCountExceededException(MAX_ITERATIONS);
        } else if (Double.isInfinite(sum)) {
            ret = 1.0;
        } else {
            ret = FastMath.exp(-x + (A * FastMath.log(x)) - LOG_GAMMA_A) * sum;
        }

        return ret;
    }

    private static double regularizedGammaQ(double x) {
        double ret;

        if (x == 0.0) {
            ret = 1.0;
        } else if (x < A + 1.0) {
            ret = 1.0 - regularizedGammaP(x);
        } else {
            ret = 1.0 / fraction.evaluate(x, EPSILON, MAX_ITERATIONS);
            ret = FastMath.exp(-x + (A * FastMath.log(x)) - LOG_GAMMA_A) * ret;
        }

        return ret;
    }
}

/**
 * Implementation of Gamma distrbution.
 * <p>
 * This class includes the code came from {@link org.apache.commons.math3.distribution.GammaDistribution} and
 * {@link org.apache.commons.math3.special.Gamma}.
 * </p>
 */
class Gamma extends ContinuousDistributionBase {
    private static final double DEFAULT_EPSILON = 10e-15;

    private final double a;
    private final double scale;
    private final double logGammaA;
    private final ContinuedFraction fraction;

    Gamma(double shape, double scale) {
        super(new GammaDistribution(shape, scale), GammaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
        this.a = shape;
        this.scale = scale;
        this.logGammaA = org.apache.commons.math3.special.Gamma.logGamma(a);
        this.fraction = new ContinuedFraction() {
            /** {@inheritDoc} */
            @Override
            protected double getA(int n, double x) {
                return ((2.0 * n) + 1.0) - a + x;
            }

            /** {@inheritDoc} */
            @Override
            protected double getB(int n, double x) {
                return n * (a - n);
            }
        };
    }

    @Override
    public double cdf(double x) {
        if (x <= 0) {
            return 0;
        } else {
            return regularizedGammaP(x / scale);
        }
    }

    private double regularizedGammaP(double x) {
        double ret;

        if (x >= a + 1) {
            // use regularizedGammaQ because it should converge faster in this
            // case.
            ret = 1.0 - regularizedGammaQ(x);
        } else {
            // calculate series
            double n = 0.0; // current element index
            double an = 1.0 / a; // n-th element in the series
            double sum = an; // partial sum
            while (FastMath.abs(an / sum) > DEFAULT_EPSILON &&
                    n < Integer.MAX_VALUE &&
                    sum < Double.POSITIVE_INFINITY) {
                // compute next element in the series
                n += 1.0;
                an *= x / (a + n);

                // update partial sum
                sum += an;
            }
            if (n >= Integer.MAX_VALUE) {
                throw new MaxCountExceededException(Integer.MAX_VALUE);
            } else if (Double.isInfinite(sum)) {
                ret = 1.0;
            } else {
                ret = FastMath.exp(-x + (a * FastMath.log(x)) - logGammaA) * sum;
            }
        }

        return ret;
    }

    private double regularizedGammaQ(double x) {
        double ret;

        ret = 1.0 / fraction.evaluate(x, DEFAULT_EPSILON, Integer.MAX_VALUE);
        ret = FastMath.exp(-x + (a * FastMath.log(x)) - logGammaA) * ret;

        return ret;
    }
}

/**
 * Implementation of Beta distrbution.
 * <p>
 * This class includes the code came from {@link org.apache.commons.math3.distribution.BetaDistribution} and
 * {@link org.apache.commons.math3.special.Beta}.
 * </p>
 */
class Beta extends ContinuousDistributionBase {
    private static final double DEFAULT_EPSILON = 1E-14;

    private final double a;
    private final double b;
    private final double t1;
    private final double t2;
    private final double logA;
    private final double logBeta;
    private final ContinuedFraction fraction;
    private final Beta sym;

    Beta(double alpha, double beta) {
        this(alpha, beta, null);
    }

    private Beta(double alpha, double beta, Beta sym) {
        super(new BetaDistribution(alpha, beta), BetaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

        this.a = alpha;
        this.b = beta;
        this.t1 = (alpha + 1) / (2 + beta + alpha);
        this.t2 = (beta + 1) / (2 + beta + alpha);
        this.logA = FastMath.log(alpha);
        this.logBeta = org.apache.commons.math3.special.Beta.logBeta(alpha, beta);
        this.fraction = new ContinuedFraction() {
            /** {@inheritDoc} */
            @Override
            protected double getB(int n, double x) {
                double ret;
                double m;
                if (n % 2 == 0) { // even
                    m = n / 2.0;
                    ret = (m * (b - m) * x) /
                            ((a + (2 * m) - 1) * (a + (2 * m)));
                } else {
                    m = (n - 1.0) / 2.0;
                    ret = -((a + m) * (a + b + m) * x) /
                            ((a + (2 * m)) * (a + (2 * m) + 1.0));
                }
                return ret;
            }

            /** {@inheritDoc} */
            @Override
            protected double getA(int n, double x) {
                return 1.0;
            }
        };

        if (sym == null) {
            this.sym = new Beta(beta, alpha, this);
        } else {
            this.sym = sym;
        }
    }

    @Override
    public double cdf(double x) {
        if (x <= 0) {
            return 0;
        } else if (x >= 1) {
            return 1;
        } else {
            return regularizedBeta(x);
        }
    }

    private double regularizedBeta(double x) {
        double ret;

        if (x > t1 &&
                1 - x <= t2) {
            ret = 1 - sym.regularizedBeta(1 - x);
        } else {
            ret = FastMath.exp((a * FastMath.log(x)) + (b * FastMath.log1p(-x)) -
                    logA - logBeta) *
                    1.0 / fraction.evaluate(x, DEFAULT_EPSILON, Integer.MAX_VALUE);
        }

        return ret;
    }
}

interface CommonsMath3DistributionWrapper {
    class Continuous implements ContinuousDistribution {
        private final RealDistribution distribution;

        Continuous(RealDistribution distribution) {
            this.distribution = distribution;
        }

        @Override
        public double cdf(double x) {
            return distribution.cumulativeProbability(x);
        }

        @Override
        public double inverseCdf(double p) {
            return distribution.inverseCumulativeProbability(p);
        }
    }

    class Discrete implements DiscreteDistribution {
        private final IntegerDistribution distribution;

        Discrete(IntegerDistribution distribution) {
            this.distribution = distribution;
        }

        @Override
        public double cdf(int x) {
            return distribution.cumulativeProbability(x);
        }

        @Override
        public int inverseCdf(double p) {
            return distribution.inverseCumulativeProbability(p);
        }
    }
}
