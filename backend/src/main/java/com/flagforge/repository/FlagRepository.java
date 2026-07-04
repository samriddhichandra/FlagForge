package com.flagforge.repository;

import com.flagforge.entity.Flag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FlagRepository extends JpaRepository<Flag, UUID> {

    Optional<Flag> findByEnvironmentIdAndKey(UUID environmentId, String key);

    @Query("""
        SELECT f FROM Flag f
        WHERE f.environmentId = :environmentId
        AND (:search IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:enabled IS NULL OR f.enabled = :enabled)
        ORDER BY f.updatedAt DESC
        """)
    Page<Flag> search(@Param("environmentId") UUID environmentId,
                       @Param("search") String search,
                       @Param("enabled") Boolean enabled,
                       Pageable pageable);

    boolean existsByEnvironmentIdAndKey(UUID environmentId, String key);
}
