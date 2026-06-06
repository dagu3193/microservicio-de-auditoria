CREATE TABLE IF NOT EXISTS audit_logs (
    id            VARCHAR(36)     PRIMARY KEY,
    change_type   VARCHAR(30)     NOT NULL,
    item_id       VARCHAR(100)    NOT NULL,
    item_name     VARCHAR(200)    NOT NULL,
    old_value     VARCHAR(200),
    new_value     VARCHAR(200)    NOT NULL,
    reason        VARCHAR(500),
    changed_by    VARCHAR(100)    NOT NULL,
    changed_at    TIMESTAMP       NOT NULL
);

-- Indices para queries de auditoria rapidas
CREATE INDEX idx_audit_logs_type ON audit_logs(change_type);
CREATE INDEX idx_audit_logs_item ON audit_logs(item_id);
CREATE INDEX idx_audit_logs_changed_by ON audit_logs(changed_by);
CREATE INDEX idx_audit_logs_changed_at ON audit_logs(changed_at);

COMMENT ON TABLE audit_logs IS 'Registro de auditoría de cambios en precios y cupos';
