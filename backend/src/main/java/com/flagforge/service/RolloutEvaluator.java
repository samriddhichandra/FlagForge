package com.flagforge.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Determines whether a given user falls inside a percentage rollout bucket.
 *
 * Design goal: the SAME user must ALWAYS get the SAME result for a given flag,
 * for as long as the rollout percentage doesn't change. This is "sticky" bucketing,
 * achieved via consistent hashing rather than random sampling per request.
 *
 * We hash (flagKey + ":" + userKey) rather than userKey alone, so the same user
 * lands in independent, uncorrelated buckets across different flags/experiments —
 * otherwise a user who's in the "in" bucket for one flag would suspiciously always
 * be "in" for every flag, biasing overlapping experiments.
 */
@Component
public class RolloutEvaluator {

    private static final long HASH_MODULUS = 10_000L; // gives 0.01% rollout precision

    public boolean isInRollout(String flagKey, String userKey, int rolloutPercentage) {
        if (rolloutPercentage <= 0) return false;
        if (rolloutPercentage >= 100) return true;

        long bucket = bucketFor(flagKey, userKey);
        long threshold = (long) rolloutPercentage * (HASH_MODULUS / 100);
        return bucket < threshold;
    }

    private long bucketFor(String flagKey, String userKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((flagKey + ":" + userKey).getBytes(StandardCharsets.UTF_8));
            // Use the first 8 bytes as an unsigned long, then mod into our bucket space
            long value = 0;
            for (int i = 0; i < 8; i++) {
                value = (value << 8) | (hash[i] & 0xFF);
            }
            return Math.floorMod(value, HASH_MODULUS);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed available on every JVM; this branch is unreachable in practice
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
