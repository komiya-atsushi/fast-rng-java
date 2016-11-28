# fast-rng: Fast random number generator for various distributions

[![Download](https://api.bintray.com/packages/komiya-atsushi/maven/fast-rng/images/download.svg) ](https://bintray.com/komiya-atsushi/maven/fast-rng/_latestVersion)

# Getting started

## Resolving artifacts using Maven

```xml
<repositories>
  <repository>
    <id>bintray-komiya-atsushi-maven</id>
    <url>http://dl.bintray.com/komiya-atsushi/maven</url>
  </repository>
</repository>

<dependencies>
  <dependency>
    <groupId>biz.k11i</groupId>
    <artifactId>fast-rng</artifactId>
    <version>0.1.1</version>
  </dependency>
</dependencies>
```

## Resolving artifacts using Gradle

```groovy
repositories {
    maven {
        url  "http://dl.bintray.com/komiya-atsushi/maven" 
    }
}

dependencies {
    compile group: 'biz.k11i', name: 'fast-rng', version: '0.1.1'
}
```

## Generating random values

```java
package biz.k11i.rng.demo;

import java.util.Random;

public class FastRngDemo {
    public static void main(String[] args) {
        // Fast-rng requires a java.util.Random instance to generate
        // uniformly distributed random values.
        Random random = new Random();

        System.out.println(
                // Generate a gaussian random value.
                GaussianRNG.FAST_RNG.generate(random)
        );
    }
}
```


# Supported distributions

- [Gaussian distribution (Normal distribution)](https://en.wikipedia.org/wiki/Normal_distribution)
- [Gamma distribution](https://en.wikipedia.org/wiki/Gamma_distribution)


# License

This software is licensed under the terms of the MIT license. See LICENSE.


# Acknowledgments

## commons-math3

This product includes software developed at
The Apache Software Foundation (http://www.apache.org/).
https://github.com/apache/commons-math

## Jafama

This product includes software developed by Jeff Hain.
https://github.com/jeffhain/jafama


# References

- Tesuaki Yotsuji.
  *計算機シミュレーションのための確率分布乱数生成法.*
  Pleiades Publishing Co.,Ltd. (2010)
- Marsaglia, George, and Wai Wan Tsang.
  *The ziggurat method for generating random variables.*
  Journal of statistical software 5.8 (2000): 1-7.
- Ahrens, Joachim H., and Ulrich Dieter.
  *Computer methods for sampling from gamma, beta, poisson and bionomial distributions.*
  Computing 12.3 (1974): 223-246.
- Marsaglia, George, and Wai Wan Tsang.
  *A simple method for generating gamma variables.*
  ACM Transactions on Mathematical Software (TOMS) 26.3 (2000): 363-372.
