package com.flagforge;

import com.flagforge.service.ExperimentStatsService;
import com.flagforge.service.ExperimentStatsService.SignificanceResult;
import com.flagforge.service.ExperimentStatsService.VariantResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ExperimentStatsServiceTest {

    private final ExperimentStatsService stats = new ExperimentStatsService();

    @Test
    void identicalRates_areNotSignificant() {
        VariantResult control = new VariantResult(5000, 500);   // 10%
        VariantResult treatment = new VariantResult(5000, 500); // 10%

        SignificanceResult result = stats.evaluate(control, treatment);

        assertThat(result.significant()).isFalse();
        assertThat(result.pValue()).isCloseTo(1.0, within(0.01));
    }

    @Test
    void largeClearDifference_isSignificant() {
        // 12.4% vs 14.1% conversion at n=5000 each — matches the example in docs/04-api-specification.md
        VariantResult control = new VariantResult(5000, 620);
        VariantResult treatment = new VariantResult(5000, 705);

        SignificanceResult result = stats.evaluate(control, treatment);

        assertThat(result.controlRate()).isCloseTo(0.124, within(0.001));
        assertThat(result.treatmentRate()).isCloseTo(0.141, within(0.001));
        assertThat(result.liftPercent()).isCloseTo(13.7, within(0.5));
        assertThat(result.pValue()).isLessThan(0.05);
        assertThat(result.significant()).isTrue();
    }

    @Test
    void tinySampleSize_isNotSignificantEvenWithBigApparentLift() {
        // 1/2 vs 2/2 "looks like" a 100% lift but the sample is far too small to conclude anything
        VariantResult control = new VariantResult(2, 1);
        VariantResult treatment = new VariantResult(2, 2);

        SignificanceResult result = stats.evaluate(control, treatment);

        assertThat(result.significant()).isFalse();
    }

    @Test
    void zeroExposures_doesNotThrow_andIsNotSignificant() {
        VariantResult control = new VariantResult(0, 0);
        VariantResult treatment = new VariantResult(100, 20);

        SignificanceResult result = stats.evaluate(control, treatment);

        assertThat(result.significant()).isFalse();
        assertThat(result.pValue()).isEqualTo(1.0);
    }
}
