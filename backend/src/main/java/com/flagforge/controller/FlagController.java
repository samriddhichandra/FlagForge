package com.flagforge.controller;

import com.flagforge.dto.FlagDtos.CreateFlagRequest;
import com.flagforge.dto.FlagDtos.FlagResponse;
import com.flagforge.service.FlagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/environments/{environmentId}/flags")
@RequiredArgsConstructor
@Tag(name = "Flags", description = "Flag CRUD operations")
public class FlagController {

    private final FlagService flagService;

    @GetMapping
    @PreAuthorize("@rbac.hasRole(#projectId, 'VIEWER')")
    @Operation(summary = "List flags in an environment")
    public Page<FlagResponse> list(
            @PathVariable UUID projectId,
            @PathVariable UUID environmentId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean enabled,
            Pageable pageable) {
        return flagService.search(environmentId, search, enabled, pageable);
    }

    @PostMapping
    @PreAuthorize("@rbac.hasRole(#projectId, 'EDITOR')")
    @Operation(summary = "Create a new flag in an environment")
    public ResponseEntity<FlagResponse> create(
            @PathVariable UUID projectId,
            @PathVariable UUID environmentId,
            @Valid @RequestBody CreateFlagRequest request) {
        FlagResponse response = flagService.create(environmentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{flagId}/enabled")
    @PreAuthorize("@rbac.hasRole(#projectId, 'EDITOR')")
    @Operation(summary = "Enable or disable a flag and trigger real-time propagation")
    public ResponseEntity<FlagResponse> setEnabled(
            @PathVariable UUID projectId,
            @PathVariable UUID environmentId,
            @PathVariable UUID flagId,
            @RequestParam boolean enabled) {
        return ResponseEntity.ok(flagService.setEnabled(flagId, enabled));
    }
}
