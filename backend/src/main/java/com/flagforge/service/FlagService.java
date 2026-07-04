package com.flagforge.service;

import com.flagforge.dto.FlagDtos.CreateFlagRequest;
import com.flagforge.dto.FlagDtos.FlagResponse;
import com.flagforge.entity.Flag;
import com.flagforge.exception.ApiExceptions.DuplicateFlagKeyException;
import com.flagforge.exception.ApiExceptions.FlagNotFoundException;
import com.flagforge.repository.FlagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FlagService {

    private static final Duration CACHE_TTL = Duration.ofSeconds(60);

    private final FlagRepository flagRepository;
    private final StringRedisTemplate redisTemplate;

    @Transactional(readOnly = true)
    public Page<FlagResponse> search(UUID environmentId, String search, Boolean enabled, Pageable pageable) {
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
        return flagRepository.search(environmentId, normalizedSearch, enabled, pageable)
                .map(FlagResponse::from);
    }

    @Transactional
    public FlagResponse create(UUID environmentId, CreateFlagRequest request) {
        if (flagRepository.existsByEnvironmentIdAndKey(environmentId, request.key())) {
            throw new DuplicateFlagKeyException(request.key());
        }

        Flag flag = new Flag();
        flag.setEnvironmentId(environmentId);
        flag.setKey(request.key());
        flag.setName(request.name());
        flag.setType(request.type());
        flag.setEnabled(false);

        Flag saved = flagRepository.save(flag);
        return FlagResponse.from(saved);
    }

    @Transactional
    public FlagResponse setEnabled(UUID flagId, boolean enabled) {
        Flag flag = flagRepository.findById(flagId)
                .orElseThrow(() -> new FlagNotFoundException(flagId.toString()));

        flag.setEnabled(enabled);
        flag.setVersion(flag.getVersion() + 1);
        flag.setUpdatedAt(Instant.now());
        Flag saved = flagRepository.save(flag);

        // Explicit cache invalidation is the primary consistency mechanism.
        invalidateCache(flag.getEnvironmentId(), flag.getKey());

        return FlagResponse.from(saved);
    }

    public Flag getForEvaluation(UUID environmentId, String flagKey) {
        String cacheKey = cacheKey(environmentId, flagKey);
        Flag flag = flagRepository.findByEnvironmentIdAndKey(environmentId, flagKey)
                .orElseThrow(() -> new FlagNotFoundException(flagKey));
        redisTemplate.opsForValue().set(cacheKey, String.valueOf(flag.getVersion()), CACHE_TTL);
        return flag;
    }

    private void invalidateCache(UUID environmentId, String flagKey) {
        redisTemplate.delete(cacheKey(environmentId, flagKey));
    }

    private String cacheKey(UUID environmentId, String flagKey) {
        return "flag:%s:%s".formatted(environmentId, flagKey);
    }
}
