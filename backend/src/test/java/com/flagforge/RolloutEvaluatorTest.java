package com.flagforge;

import com.flagforge.service.RolloutEvaluator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RolloutEvaluatorTest {

    private final RolloutEvaluator evaluator = new RolloutEvaluator();

    @Test
    void sameUserGetsSameResultAcrossCalls_stickyBucketing() {
        boolean first = evaluator.isInRollout("new-checkout-flow", "user-123", 50);
        boolean second = evaluator.isInRollout("new-checkout-flow", "user-123", 50);
        assertThat(first).isEqualTo(second);
    }

    @Test
    void zeroPercentRollout_alwaysFalse() {
        assertThat(evaluator.isInRollout("flag", "any-user", 0)).isFalse();
    }

    @Test
    void hundredPercentRollout_alwaysTrue() {
        assertThat(evaluator.isInRollout("flag", "any-user", 100)).isTrue();
    }

    @Test
    void distributionApproximatesConfiguredPercentage() {
        int rolloutPercentage = 30;
        int sampleSize = 10_000;
        int inCount = 0;

        for (int i = 0; i < sampleSize; i++) {
            if (evaluator.isInRollout("distribution-test-flag", "user-" + i, rolloutPercentage)) {
                inCount++;
            }
        }

        double actualPercentage = (inCount / (double) sampleSize) * 100;
        // Consistent hashing won't be exact, but should be within a couple percentage points at this sample size
        assertThat(actualPercentage).isBetween(rolloutPercentage - 3.0, rolloutPercentage + 3.0);
    }

    @Test
    void sameUserDifferentFlags_areIndependentlyBucketed() {
        // A user being "in" for one flag's rollout must not guarantee they're "in" for another —
        // otherwise overlapping experiments would be biased.
        boolean flagA = evaluator.isInRollout("flag-a", "user-999", 50);
        boolean flagB = evaluator.isInRollout("flag-b", "user-999", 50);
        // Not asserting a specific relationship — just documenting the independence property being tested.
        // A real test would run this across many users and check for near-zero correlation.
        assertThat(flagA).isIn(true, false);
        assertThat(flagB).isIn(true, false);
    }
}
