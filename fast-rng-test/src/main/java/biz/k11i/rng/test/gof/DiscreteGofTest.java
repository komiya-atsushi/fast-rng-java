package biz.k11i.rng.test.gof;

import biz.k11i.rng.test.util.SplittableRandomWrapper;
import biz.k11i.rng.test.util.distribution.DiscreteDistribution;
import biz.k11i.rng.test.util.distribution.ProbabilityDistributions;
import biz.k11i.rng.test.util.inference.MTest;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.math3.stat.inference.GTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

/**
 * Provides Goodness-of-Fit test for discrete random number generator.
 */
class DiscreteGofTest extends GoodnessOfFitTest {
    /**
     * Builds {@link DiscreteGofTest} object.
     */
    @SuppressWarnings("unused")
    public static class Builder extends BuilderBase<Builder> {
        private DiscreteDistribution distribution;
        private int maxFrequencyBins;
        private String name;
        private RandomNumberGenerator generator;

        public Builder probabilityDistribution(IntegerDistribution distribution) {
            this.distribution = ProbabilityDistributions.wrap(distribution);
            return this;
        }

        public Builder probabilityDistribution(DiscreteDistribution distribution) {
            this.distribution = distribution;
            return this;
        }

        public Builder maxFrequencyBins(int maxFrequencyBins) {
            this.maxFrequencyBins = maxFrequencyBins;
            return this;
        }

        public Builder randomNumberGenerator(String name, RandomNumberGenerator generator) {
            this.name = name;
            this.generator = generator;
            return this;
        }

        public GoodnessOfFitTest build() {
            return new DiscreteGofTest(
                    name,
                    significanceLevel,
                    distribution,
                    generator,
                    numRandomValues,
                    maxFrequencyBins);
        }
    }

    /**
     * Generates discrete random variates.
     */
    @FunctionalInterface
    public interface RandomNumberGenerator {
        /**
         * Generates a discrete random variate.
         *
         * @param random uses to sample uniform random variates.
         * @return discrete random variate.
         */
        int generate(Random random);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscreteGofTest.class);

    private final RandomNumberGenerator generator;
    private final int numRandomValues;
    private final FrequencyTable freqTable;
    private int seed;

    private DiscreteGofTest(
            String name,
            double significanceLevel,
            DiscreteDistribution distribution,
            RandomNumberGenerator generator,
            int numRandomValues,
            int maxFrequencyBins) {
        super(name, significanceLevel);
        this.generator = generator;
        this.numRandomValues = numRandomValues;
        this.freqTable = FrequencyTable.build(distribution, maxFrequencyBins);
    }

    @Override
    public Map<String, Double> test() {
        return PerformanceMeasure.run(LOGGER, m -> {
            m.measure("Generate {} random numbers and count frequencies", numRandomValues, ignore -> {
                Random random = new SplittableRandomWrapper(seed++);
                for (int i = 0; i < numRandomValues; i++) {
                    int rv = generator.generate(random);
                    freqTable.increment(rv);
                }
            });

            return buildResult(m);
        });
    }

    @Override
    public Map<String, Double> testInParallel(ForkJoinPool pool) {
        return PerformanceMeasure.run(LOGGER, m -> {
            m.measure("Generate {} random numbers and count frequencies", numRandomValues, ignore -> {
                SplittableRandomWrapper random = new SplittableRandomWrapper(seed++);

                int numTasks = pool.getParallelism();
                List<Callable<Void>> tasks = new ArrayList<>(numTasks);

                for (int i = 0; i < numTasks; i++) {
                    int start = i * numRandomValues / numTasks;
                    int end = (i + 1) * numRandomValues / numTasks;

                    tasks.add(new RandomValueGenerationTask(generator, end - start, freqTable, random.split()));
                }
                pool.invokeAll(tasks);
            });

            return buildResult(m);
        });
    }

    private Map<String, Double> buildResult(PerformanceMeasure m) {
        Map<String, Double> result = new LinkedHashMap<>();

        m.measure("Test on contingency table", ignore -> {
            long[] freq = freqTable.sumUpAndReset();
            for (TestMethod test : TestMethod.values()) {
                m.measure("Test by [{}]: p-value = {}", r -> {
                    double p = test.test(freqTable.probs, freq);
                    result.put(test.toString(), p);

                    r.updateArgs(test, p);
                });
            }

            long[] totalFreq = freqTable.totalFrequencies;
            for (TestMethod test : TestMethod.values()) {
                m.measure("Test total frequency by [{}]: p-value = {}", r -> {
                    double p = test.test(freqTable.probs, totalFreq);
                    r.updateArgs(test, p);
                });
            }
        });

        return result;
    }
}

/**
 * Statistical tests to test contingency tables.
 */
enum TestMethod {
    CHI_SQUARE_TEST {
        private final ChiSquareTest chiSquareTest = new ChiSquareTest();

        @Override
        double test(double[] probabilities, long[] frequencies) {
            return chiSquareTest.chiSquareTest(probabilities, frequencies);
        }
    },
    G_TEST {
        private final GTest gTest = new GTest();

        @Override
        double test(double[] probabilities, long[] frequencies) {
            return gTest.gTest(probabilities, frequencies);
        }
    },
    M_TEST {
        @Override
        double test(double[] probabilities, long[] frequencies) {
            return MTest.mTest(probabilities, frequencies);
        }
    };

    abstract double test(double[] probabilities, long[] frequencies);
}

class FrequencyTable {
    private static final double EPSILON = Math.nextDown(1.0);
    private final boolean isParent;
    private final int numBins;
    final double[] probs;
    private final int[] bounds;
    final long[] totalFrequencies;
    private final long[] frequencies;
    private final ConcurrentHashMap<Long, FrequencyTable> children = new ConcurrentHashMap<>();

    private FrequencyTable(double[] probs, int[] bounds) {
        this(true, probs, bounds);
    }

    private FrequencyTable(boolean isParent, double[] probs, int[] bounds) {
        this.isParent = isParent;
        this.numBins = probs.length;
        this.probs = probs;
        this.bounds = bounds;
        this.totalFrequencies = new long[numBins];
        this.frequencies = new long[numBins];
    }

    static FrequencyTable build(DiscreteDistribution distribution, int maxBins) {
        // Calculate boundaries
        int[] bounds = new int[maxBins - 1];
        int boundaryCount = 0;

        for (int i = 1; i < maxBins; i++) {
            int bound = distribution.inverseCdf(i / (double) maxBins);
            if (boundaryCount > 0 && bound == bounds[boundaryCount - 1]) {
                continue;
            }
            double point = distribution.cdf(bound);
            if (point > EPSILON) {
                break;
            }
            bounds[boundaryCount++] = bound;
        }
        bounds = Arrays.copyOf(bounds, boundaryCount);

        // Calculate expected probabilities
        double[] probs = new double[boundaryCount + 1];
        double prevPoint = 0;

        for (int i = 0; i < boundaryCount; i++) {
            double point = distribution.cdf(bounds[i]);
            probs[i] = point - prevPoint;
            prevPoint = point;
        }
        probs[boundaryCount] = 1.0 - prevPoint;

        return new FrequencyTable(probs, bounds);
    }

    void increment(int x) {
        int index = Arrays.binarySearch(bounds, x);
        if (index < 0) {
            index = ~index;
        }

        frequencies[index]++;
    }

    FrequencyTable child() {
        return children.computeIfAbsent(
                Thread.currentThread().getId(),
                ignore -> new FrequencyTable(false, this.probs, this.bounds));
    }

    long[] sumUpAndReset() {
        if (!isParent) {
            throw new IllegalStateException("This object is not parent");
        }

        for (FrequencyTable child : children.values()) {
            for (int i = 0; i < numBins; i++) {
                this.frequencies[i] += child.frequencies[i];
                child.frequencies[i] = 0;
            }
        }

        long[] result = Arrays.copyOf(frequencies, numBins);
        for (int i = 0; i < numBins; i++) {
            totalFrequencies[i] += frequencies[i];
            frequencies[i] = 0;
        }

        return result;
    }
}

class RandomValueGenerationTask implements Callable<Void> {
    private final DiscreteGofTest.RandomNumberGenerator generator;
    private final int numRandomValues;
    private final FrequencyTable parentFrequencyTable;
    private final SplittableRandomWrapper random;

    RandomValueGenerationTask(
            DiscreteGofTest.RandomNumberGenerator generator,
            int numRandomValues,
            FrequencyTable parentFrequencyTable,
            SplittableRandomWrapper random) {
        this.generator = generator;
        this.numRandomValues = numRandomValues;
        this.parentFrequencyTable = parentFrequencyTable;
        this.random = random;
    }

    @Override
    public Void call() {
        FrequencyTable table = parentFrequencyTable.child();
        for (int i = 0; i < numRandomValues; i++) {
            int rv = generator.generate(random);
            table.increment(rv);
        }
        return null;
    }
}
