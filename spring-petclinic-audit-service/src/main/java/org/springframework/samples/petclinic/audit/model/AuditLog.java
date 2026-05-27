/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.audit.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Date;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date timestamp = new Date();

    @NotBlank
    @Size(max = 255)
    @Column(name = "action")
    private String action;

    @Size(max = 255)
    @Column(name = "performed_by")
    private String performedBy;

    @Size(max = 255)
    @Column(name = "service_name")
    private String serviceName;

    @Size(max = 8192)
    @Column(name = "details")
    private String details;

    public Integer getId() {
        return this.id;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public String getAction() {
        return this.action;
    }

    public String getPerformedBy() {
        return this.performedBy;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getDetails() {
        return this.details;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public static final class AuditLogBuilder {
        private Integer id;
        private Date timestamp;
        private @NotBlank @Size(max = 255) String action;
        private @Size(max = 255) String performedBy;
        private @Size(max = 255) String serviceName;
        private @Size(max = 8192) String details;

        private AuditLogBuilder() {
        }

        public static AuditLogBuilder anAuditLog() {
            return new AuditLogBuilder();
        }

        public AuditLogBuilder id(Integer id) {
            this.id = id;
            return this;
        }

        public AuditLogBuilder timestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public AuditLogBuilder action(String action) {
            this.action = action;
            return this;
        }

        public AuditLogBuilder performedBy(String performedBy) {
            this.performedBy = performedBy;
            return this;
        }

        public AuditLogBuilder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public AuditLogBuilder details(String details) {
            this.details = details;
            return this;
        }

        public AuditLog build() {
            AuditLog auditLog = new AuditLog();
            auditLog.setId(id);
            auditLog.setTimestamp(timestamp);
            auditLog.setAction(action);
            auditLog.setPerformedBy(performedBy);
            auditLog.setServiceName(serviceName);
            auditLog.setDetails(details);
            return auditLog;
        }
    }
}
