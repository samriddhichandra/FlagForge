package com.flagforge.service;

import org.springframework.stereotype.Service;

/**
 * Computes statistical significance between a control and treatment variant
 * using a two-proportion z-test — the standard approach for comparing
 * conversion rates between two independent samples.
 *
 * This is deliberately implemented from first principles (rather than pulling in
 * a heavyweight stats library) so the reasoning is transparent and testable.
 */
@Service
public class ExperimentStatsService {

    public record VariantResult(long exposures, long conversions) {
        public double rate() {
            return exposures == 0 ? 0.0 : (double) conversions / exposures;
        }
    }

    public record SignificanceResult(
            double controlRate,
            double treatmentRate,
            double liftPercent,
            double zScore,
            double pValue,
            boolean significant
    ) {}

    private static final double SIGNIFICANCE_THRESHOLD = 0.05;

    public SignificanceResult evaluate(VariantResult control, VariantResult treatment) {
        double p1 = control.rate();
        double p2 = treatment.rate();
        long n1 = control.exposures();
        long n2 = treatment.exposures();

        if (n1 == 0 || n2 == 0) {
            return new SignificanceResult(p1, p2, 0.0, 0.0, 1.0, false);
        }

        // Pooled proportion under the null hypothesis (no difference between variants)
        double pooledP = (double) (control.conversions() + treatment.conversions()) / (n1 + n2);
        double standardError = Math.sqrt(pooledP * (1 - pooledP) * (1.0 / n1 + 1.0 / n2));

        double zScore = standardError == 0 ? 0.0 : (p2 - p1) / standardError;
        double pValue = twoTailedPValue(zScore);
        double liftPercent = p1 == 0 ? 0.0 : ((p2 - p1) / p1) * 100.0;

        return new SignificanceResult(p1, p2, liftPercent, zScore, pValue, pValue < SIGNIFICANCE_THRESHOLD);
    }

    /** Two-tailed p-value from a z-score using the standard normal CDF approximation. */
    private double twoTailedPValue(double z) {
        double absZ = Math.abs(z);
        double cdf = 1.0 - 0.5 * erfc(absZ / Math.sqrt(2));
        return 2 * (1.0 - cdf);
    }

    /** Complementary error function approximation (Abramowitz & Stegun 7.1.26). */
    private double erfc(double x) {
        double t = 1.0 / (1.0 + 0.3275911 * x);
        double y = 1.0 - (((((1.061405429 * t - 1.453152027) * t) + 1.421413741) * t - 0.284496736) * t + 0.254829592) * t * Math.exp(-x * x);
        return 1.0 - y;
    }
}
