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
package org.springframework.samples.petclinic.audit.web;

import java.util.List;
import jakarta.validation.Valid;

import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.audit.model.AuditLog;
import org.springframework.samples.petclinic.audit.model.AuditLogRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("audit-logs")
@Timed("petclinic.audit")
class AuditLogResource {

    private static final Logger log = LoggerFactory.getLogger(AuditLogResource.class);

    private final AuditLogRepository auditLogRepository;

    AuditLogResource(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuditLog create(@Valid @RequestBody AuditLog auditLog) {
        log.info("Saving audit log entry: action={}, performedBy={}, service={}",
            auditLog.getAction(), auditLog.getPerformedBy(), auditLog.getServiceName());
        return auditLogRepository.save(auditLog);
    }

    @GetMapping
    public List<AuditLog> findAll() {
        return auditLogRepository.findAll();
    }

    @GetMapping("{id}")
    public AuditLog findById(@PathVariable("id") Integer id) {
        return auditLogRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("AuditLog not found with id: " + id));
    }

    @GetMapping("by-service")
    public List<AuditLog> findByServiceName(@RequestParam("serviceName") String serviceName) {
        return auditLogRepository.findByServiceName(serviceName);
    }

    @GetMapping("by-performer")
    public List<AuditLog> findByPerformedBy(@RequestParam("performedBy") String performedBy) {
        return auditLogRepository.findByPerformedBy(performedBy);
    }

    @GetMapping("by-action")
    public List<AuditLog> findByAction(@RequestParam("action") String action) {
        return auditLogRepository.findByAction(action);
    }
}
