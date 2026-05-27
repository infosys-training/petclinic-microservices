package org.springframework.samples.petclinic.audit.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.audit.model.AuditLog;
import org.springframework.samples.petclinic.audit.model.AuditLogRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditLogResource.class)
@ActiveProfiles("test")
class AuditLogResourceTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    AuditLogRepository auditLogRepository;

    @Test
    void shouldFetchAllAuditLogs() throws Exception {
        given(auditLogRepository.findAll())
            .willReturn(
                asList(
                    AuditLog.AuditLogBuilder.anAuditLog()
                        .id(1)
                        .action("CREATE_OWNER")
                        .serviceName("customers-service")
                        .performedBy("admin")
                        .build(),
                    AuditLog.AuditLogBuilder.anAuditLog()
                        .id(2)
                        .action("CREATE_VISIT")
                        .serviceName("visits-service")
                        .performedBy("vet-james")
                        .build()
                )
            );

        mvc.perform(get("/audit-logs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].action").value("CREATE_OWNER"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].action").value("CREATE_VISIT"));
    }

    @Test
    void shouldFetchAuditLogById() throws Exception {
        given(auditLogRepository.findById(1))
            .willReturn(Optional.of(
                AuditLog.AuditLogBuilder.anAuditLog()
                    .id(1)
                    .action("CREATE_OWNER")
                    .serviceName("customers-service")
                    .performedBy("admin")
                    .details("Created owner George Franklin")
                    .build()
            ));

        mvc.perform(get("/audit-logs/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.action").value("CREATE_OWNER"))
            .andExpect(jsonPath("$.serviceName").value("customers-service"));
    }

    @Test
    void shouldReturn404WhenAuditLogNotFound() throws Exception {
        given(auditLogRepository.findById(999))
            .willReturn(Optional.empty());

        mvc.perform(get("/audit-logs/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateAuditLog() throws Exception {
        AuditLog saved = AuditLog.AuditLogBuilder.anAuditLog()
            .id(1)
            .action("CREATE_OWNER")
            .serviceName("customers-service")
            .performedBy("admin")
            .details("Created owner George Franklin")
            .build();

        given(auditLogRepository.save(any(AuditLog.class))).willReturn(saved);

        mvc.perform(post("/audit-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"CREATE_OWNER\",\"performedBy\":\"admin\",\"serviceName\":\"customers-service\",\"details\":\"Created owner George Franklin\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.action").value("CREATE_OWNER"));
    }

    @Test
    void shouldFetchAuditLogsByServiceName() throws Exception {
        given(auditLogRepository.findByServiceName("customers-service"))
            .willReturn(
                asList(
                    AuditLog.AuditLogBuilder.anAuditLog()
                        .id(1)
                        .action("CREATE_OWNER")
                        .serviceName("customers-service")
                        .build()
                )
            );

        mvc.perform(get("/audit-logs/by-service?serviceName=customers-service"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].serviceName").value("customers-service"));
    }
}
