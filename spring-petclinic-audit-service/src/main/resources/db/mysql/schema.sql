CREATE TABLE IF NOT EXISTS audit_logs (
  id            INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  timestamp     TIMESTAMP,
  action        VARCHAR(255) NOT NULL,
  performed_by  VARCHAR(255),
  service_name  VARCHAR(255),
  details       VARCHAR(8192),
  INDEX(service_name),
  INDEX(performed_by),
  INDEX(action)
) engine=InnoDB;
