package com.flagforge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "flag", uniqueConstraints = @UniqueConstraint(columnNames = {"environment_id", "key"}))
@Getter
@Setter
public class Flag {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "environment_id", nullable = false)
    private UUID environmentId;

    @Column(nullable = false, length = 150)
    private String key;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FlagType type;

    @Column(nullable = false)
    private boolean enabled = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_value", nullable = false)
    private String defaultValue = "false";

    @Column(nullable = false)
    private int version = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public enum FlagType {
        BOOLEAN, MULTIVARIATE, PERCENTAGE
    }
}
