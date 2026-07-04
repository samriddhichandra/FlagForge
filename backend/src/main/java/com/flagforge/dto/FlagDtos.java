package com.flagforge.dto;

import com.flagforge.entity.Flag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;
import java.util.UUID;

public class FlagDtos {

    public record CreateFlagRequest(
            @NotBlank
            @Pattern(regexp = "^[a-z0-9-]{3,150}$", message = "key must be lowercase alphanumeric with hyphens, 3-150 chars")
            String key,

            @NotBlank
            String name,

            @NotNull
            Flag.FlagType type
    ) {}

    public record UpdateFlagRequest(
            Boolean enabled,
            String name
    ) {}

    public record FlagResponse(
            UUID id,
            String key,
            String name,
            Flag.FlagType type,
            boolean enabled,
            int version,
            Instant updatedAt
    ) {
        public static FlagResponse from(Flag flag) {
            return new FlagResponse(
                    flag.getId(), flag.getKey(), flag.getName(),
                    flag.getType(), flag.isEnabled(), flag.getVersion(), flag.getUpdatedAt()
            );
        }
    }

    public record EvaluationResponse(
            String flagKey,
            boolean enabled,
            String variant,
            String reason
    ) {}
}
