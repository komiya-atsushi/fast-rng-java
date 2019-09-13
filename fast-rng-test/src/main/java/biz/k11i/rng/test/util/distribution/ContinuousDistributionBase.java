package biz.k11i.rng.test.util.distribution;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.UnivariateSolverUtils;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.util.FastMath;

/**
 * Base class of {@link ContinuousDistribution}.
 *
 * <p>
 * This implementation includes a part of <a href="https://commons.apache.org/proper/commons-math/">Apache Commons Math</a>
 * that is licensed under the Apache License 2.0.
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
 */
abstract class ContinuousDistributionBase implements ContinuousDistribution {
    private final double supportLowerBound;
    private final double supportUpperBound;
    private final double mu;
    private final double sig;
    private final boolean chebyshevApplies;
    private final boolean isSupportConnected;
    private final double solverAbsoluteAccuracy;

    ContinuousDistributionBase(RealDistribution distribution, double solverAbsoluteAccuracy) {
        this.supportLowerBound = distribution.getSupportLowerBound();
        this.supportUpperBound = distribution.getSupportUpperBound();
        this.mu = distribution.getNumericalMean();
        this.sig = FastMath.sqrt(distribution.getNumericalVariance());
        this.chebyshevApplies = !(Double.isInfinite(mu) || Double.isNaN(mu) ||
                Double.isInfinite(sig) || Double.isNaN(sig));
        this.isSupportConnected = distribution.isSupportConnected();
        this.solverAbsoluteAccuracy = solverAbsoluteAccuracy;
    }

    @Override
    public double inverseCdf(double p) {
        if (p < 0.0 || p > 1.0) {
            throw new OutOfRangeException(p, 0, 1);
        }

        if (p == 0.0) {
            return supportLowerBound;
        }

        if (p == 1.0) {
            return supportUpperBound;
        }

        double lowerBound = supportLowerBound;
        if (supportLowerBound == Double.NEGATIVE_INFINITY) {
            if (chebyshevApplies) {
                lowerBound = mu - sig * FastMath.sqrt((1. - p) / p);
            } else {
                lowerBound = -1.0;
                while (cdf(lowerBound) >= p) {
                    lowerBound *= 2.0;
                }
            }
        }

        double upperBound = supportUpperBound;
        if (upperBound == Double.POSITIVE_INFINITY) {
            if (chebyshevApplies) {
                upperBound = mu + sig * FastMath.sqrt(p / (1. - p));
            } else {
                upperBound = 1.0;
                while (cdf(upperBound) < p) {
                    upperBound *= 2.0;
                }
            }
        }

        final UnivariateFunction toSolve = new UnivariateFunction() {
            /** {@inheritDoc} */
            public double value(final double x) {
                return cdf(x) - p;
            }
        };

        double x = UnivariateSolverUtils.solve(toSolve,
                lowerBound,
                upperBound,
                solverAbsoluteAccuracy);

        if (!isSupportConnected) {
            /* Test for plateau. */
            if (x - solverAbsoluteAccuracy >= supportLowerBound) {
                double px = cdf(x);
                if (cdf(x - solverAbsoluteAccuracy) == px) {
                    upperBound = x;
                    while (upperBound - lowerBound > solverAbsoluteAccuracy) {
                        final double midPoint = 0.5 * (lowerBound + upperBound);
                        if (cdf(midPoint) < px) {
                            lowerBound = midPoint;
                        } else {
                            upperBound = midPoint;
                        }
                    }
                    return upperBound;
                }
            }
        }
        return x;
    }
}
