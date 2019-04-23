GRADLE = ./gradlew


.PHONY: test release

test:
	$(GRADLE) test

release:
	$(GRADLE) clean fast-rng:release --no-daemon -x test
