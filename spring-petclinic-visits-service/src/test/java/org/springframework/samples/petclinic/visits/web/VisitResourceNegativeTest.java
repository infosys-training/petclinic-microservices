package org.springframework.samples.petclinic.visits.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.model.VisitRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VisitResource.class)
@ActiveProfiles("test")
class VisitResourceNegativeTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    VisitRepository visitRepository;

    @Test
    void shouldCreateVisit() throws Exception {
        Visit visit = Visit.VisitBuilder.aVisit()
            .id(1)
            .petId(1)
            .description("annual checkup")
            .date(new Date())
            .build();

        given(visitRepository.save(any(Visit.class))).willReturn(visit);

        String visitJson = "{\"date\":\"2024-01-15\",\"description\":\"annual checkup\"}";

        mvc.perform(post("/owners/1/pets/1/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(visitJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.description").value("annual checkup"));
    }

    @Test
    void shouldFindVisitsByPetId() throws Exception {
        Visit visit = Visit.VisitBuilder.aVisit()
            .id(1)
            .petId(7)
            .description("rabies shot")
            .build();

        given(visitRepository.findByPetId(7)).willReturn(List.of(visit));

        mvc.perform(get("/owners/1/pets/7/visits").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].description").value("rabies shot"))
            .andExpect(jsonPath("$[0].petId").value(7));
    }

    @Test
    void shouldReturnEmptyVisitsForPetWithNoVisits() throws Exception {
        given(visitRepository.findByPetId(99)).willReturn(Collections.emptyList());

        mvc.perform(get("/owners/1/pets/99/visits").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnEmptyVisitsForUnknownPetIds() throws Exception {
        given(visitRepository.findByPetIdIn(List.of(999))).willReturn(Collections.emptyList());

        mvc.perform(get("/pets/visits?petId=999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void shouldFetchVisitsForMultiplePets() throws Exception {
        Visit v1 = Visit.VisitBuilder.aVisit().id(1).petId(1).description("checkup").build();
        Visit v2 = Visit.VisitBuilder.aVisit().id(2).petId(2).description("vaccination").build();

        given(visitRepository.findByPetIdIn(List.of(1, 2))).willReturn(List.of(v1, v2));

        mvc.perform(get("/pets/visits?petId=1,2").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.items[0].description").value("checkup"))
            .andExpect(jsonPath("$.items[1].description").value("vaccination"));
    }
}
