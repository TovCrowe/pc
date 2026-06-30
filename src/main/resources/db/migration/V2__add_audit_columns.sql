-- V2__add_audit_columns.sql
-- Adds audit timestamps (created_at / updated_at) to clients and policies.
-- Populated automatically by JPA auditing (see Auditable + JpaAuditingConfig).
-- DEFAULT CURRENT_TIMESTAMP backfills existing rows so the NOT NULL constraint holds.
-- One column per statement + CURRENT_TIMESTAMP keeps this portable across PostgreSQL and H2.

ALTER TABLE clients ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE clients ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;


ALTER TABLE policies ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE policies ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
