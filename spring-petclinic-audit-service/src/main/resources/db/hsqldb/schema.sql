DROP TABLE audit_logs IF EXISTS;

CREATE TABLE audit_logs (
  id            INTEGER IDENTITY PRIMARY KEY,
  timestamp     TIMESTAMP,
  action        VARCHAR(255) NOT NULL,
  performed_by  VARCHAR(255),
  service_name  VARCHAR(255),
  details       VARCHAR(8192)
);

CREATE INDEX audit_logs_service_name ON audit_logs (service_name);
CREATE INDEX audit_logs_performed_by ON audit_logs (performed_by);
CREATE INDEX audit_logs_action ON audit_logs (action);
