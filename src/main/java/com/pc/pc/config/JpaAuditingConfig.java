package com.pc.pc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA auditing so {@code @CreatedDate} / {@code @LastModifiedDate}
 * fields (see {@code Auditable}) are populated automatically on save.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
