# fast-rng: Fast random number generator for various distributions

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
    <version>0.1.0</version>
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
    compile group: 'biz.k11i', name: 'fast-rng', version: '0.1.0'
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


# License

This software is licensed under the terms of the MIT license. See LICENSE.
