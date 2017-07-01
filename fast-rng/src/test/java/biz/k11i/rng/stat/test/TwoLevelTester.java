package biz.k11i.rng.stat.test;

import biz.k11i.rng.util.ParallelSorts;
import biz.k11i.rng.util.RandomSupplier;
import org.apache.commons.math3.distribution.RealDistribution;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TwoLevelTester {
    private static final int FJ_TASK_SIZE = 10000;

    private final int n;
    private final int k;
    private final RandomSupplier randomSupplier = new RandomSupplier() {
        @Override
        public Random random() {
            return ThreadLocalRandom.current();
        }
    };

    public TwoLevelTester(int n, int k) {
        this.n = n;
        this.k = k;
    }

    public interface RealRng {
        double generate(Random random);
    }

    private Map<SequenceTransformer, Double> runTwoLevelTest(
            final RealRng rng,
            final RealDistribution distribution,
            final TestStatistic testStatistic,
            SequenceTransformer... transformers) throws ExecutionException, InterruptedException {

        System.out.printf("Two-level testing: n = %d, k = %d, test statistic = %s%n",
                n, k, testStatistic.toString());

        double[] firstLevelValues = new double[n];
        double[] firstLevelWork = new double[n];

        Map<SequenceTransformer, double[]> secondLevelValuesMap = new EnumMap<>(SequenceTransformer.class);
        for (SequenceTransformer transformer : transformers) {
            secondLevelValuesMap.put(transformer, new double[k]);
        }

        ForkJoinPool pool = new ForkJoinPool();

        for (int ki = 0; ki < k; ki++) {
            pool.invoke(generateRandomValuesRecursive(rng, distribution, 0, n, firstLevelValues, firstLevelWork));

            Map<SequenceTransformer, ForkJoinTask<Double>> tasks = new EnumMap<>(SequenceTransformer.class);
            for (SequenceTransformer transformer : transformers) {
                ForkJoinTask<double[]> transformationTask = pool.submit(transformer.asForkJoinTask(firstLevelValues));
                ForkJoinTask<Double> task = pool.submit(calculatePValue(transformationTask, testStatistic));
                tasks.put(transformer, task);
            }

            for (SequenceTransformer transformer : transformers) {
                double pValue = tasks.get(transformer).get();
                secondLevelValuesMap.get(transformer)[ki] = pValue;
                System.out.printf("  First level [%d]: %.5f (%s)%n", ki + 1, pValue, transformer);
            }
        }

        Map<SequenceTransformer, Double> result = new EnumMap<>(SequenceTransformer.class);
        for (Map.Entry<SequenceTransformer, double[]> entry : secondLevelValuesMap.entrySet()) {
            SequenceTransformer transformer = entry.getKey();
            double[] secondLevelValues = entry.getValue();

            Arrays.sort(secondLevelValues);

            double pValue = testStatistic.test(secondLevelValues);
            System.out.printf("  Second level: %.5f (%s)%n", pValue, transformer);

            result.put(transformer, pValue);
        }

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.MINUTES);

        return result;
    }

    private ForkJoinTask<Void> generateRandomValuesRecursive(
            final RealRng rng,
            final RealDistribution distribution,
            final int startInclusive,
            final int endExclusive,
            final double[] result,
            final double[] work) {

        return new RecursiveAction() {
            @Override
            protected void compute() {
                if (endExclusive - startInclusive > FJ_TASK_SIZE) {
                    int mid = startInclusive + (endExclusive - startInclusive) / 2;

                    invokeAll(
                            generateRandomValuesRecursive(rng, distribution, startInclusive, mid, work, result /* swap work & result*/),
                            generateRandomValuesRecursive(rng, distribution, mid, endExclusive, work, result));

                    ParallelSorts.merge(work, result, startInclusive, mid, endExclusive);

                } else {
                    for (int i = startInclusive; i < endExclusive; i++) {
                        double r = rng.generate(randomSupplier.random());
                        if (Double.isNaN(r)) {
                            throw new RuntimeException("NaN");
                        }
                        result[i] = distribution.cumulativeProbability(r);
                    }

                    Arrays.sort(result, startInclusive, endExclusive);
                }
            }
        };
    }

    private ForkJoinTask<Double> calculatePValue(
            final ForkJoinTask<double[]> transformationTask,
            final TestStatistic testStatistic) {

        return new RecursiveTask<Double>() {

            @Override
            protected Double compute() {
                try {
                    return testStatistic.test(transformationTask.get());

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public void test(RealRng rng, RealDistribution distribution) {
        SequenceTransformer[] transformers = SequenceTransformer.values();

        try {
            for (TestStatistic testStatistic : Collections.singleton(TestStatistic.ANDERSON_DARLING)) {
                Map<SequenceTransformer, Double> result = runTwoLevelTest(rng, distribution, testStatistic, transformers);

                for (SequenceTransformer transformer : transformers) {
                    assertThat(result, hasEntry(is(transformer), greaterThanOrEqualTo(0.001)));
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
