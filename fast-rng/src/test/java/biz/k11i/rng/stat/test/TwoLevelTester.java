package biz.k11i.rng.stat.test;

import biz.k11i.rng.util.CommonsMathRandom;
import org.apache.commons.math3.distribution.RealDistribution;

import java.util.Arrays;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

public class TwoLevelTester {
    private final int n;
    private final int k;
    private final Random random = new CommonsMathRandom();

    public TwoLevelTester(int n, int k) {
        this.n = n;
        this.k = k;
    }

    public interface RealRng {
        double generate(Random random);
    }

    public double runTwoLevelTest(
            RealRng rng,
            RealDistribution distribution,
            TestStatistic testStatistic,
            SequenceTransformer transformer) {

        System.out.printf("Two-level testing: n = %d, k = %d, test statistic = %s, transformation = %s %n",
                n, k, testStatistic.toString(), transformer.toString());

        double[] firstLevelValues = new double[n];
        double[] secondLevelValues = new double[k];

        for (int ki = 0; ki < k; ki++) {
            for (int ni = 0; ni < n; ni++) {
                firstLevelValues[ni] = distribution.cumulativeProbability(rng.generate(random));
            }
            Arrays.sort(firstLevelValues);

            firstLevelValues = transformer.transform(firstLevelValues);

            double pValue = testStatistic.test(firstLevelValues);
            secondLevelValues[ki] = pValue;

            System.out.printf("  First level [%d]: %.5f%n", ki + 1, pValue);
        }

        Arrays.sort(secondLevelValues);

        double result = testStatistic.test(secondLevelValues);
        System.out.printf("  Second level: %.5f%n", result);

        return result;
    }

    public void test(RealRng rng, RealDistribution distribution) {
        for (TestStatistic testStatistic : TestStatistic.values()) {
            for (SequenceTransformer transformer : SequenceTransformer.values()) {
                double result = runTwoLevelTest(rng, distribution, testStatistic, transformer);
                assertThat(result, is(greaterThanOrEqualTo(0.001)));
            }
        }
    }
}
