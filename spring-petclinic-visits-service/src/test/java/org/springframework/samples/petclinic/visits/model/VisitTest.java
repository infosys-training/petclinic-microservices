package org.springframework.samples.petclinic.visits.model;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class VisitTest {

    @Test
    void shouldSetAndGetProperties() {
        Visit visit = new Visit();
        Date date = new Date();

        visit.setId(1);
        visit.setDate(date);
        visit.setDescription("annual checkup");
        visit.setPetId(7);

        assertEquals(1, visit.getId());
        assertEquals(date, visit.getDate());
        assertEquals("annual checkup", visit.getDescription());
        assertEquals(7, visit.getPetId());
    }

    @Test
    void shouldHaveDefaultDate() {
        Visit visit = new Visit();
        assertNotNull(visit.getDate());
    }

    @Test
    void shouldBuildVisitWithBuilder() {
        Date date = new Date();
        Visit visit = Visit.VisitBuilder.aVisit()
            .id(1)
            .date(date)
            .description("checkup")
            .petId(5)
            .build();

        assertEquals(1, visit.getId());
        assertEquals(date, visit.getDate());
        assertEquals("checkup", visit.getDescription());
        assertEquals(5, visit.getPetId());
    }

    @Test
    void shouldBuildVisitWithNullFields() {
        Visit visit = Visit.VisitBuilder.aVisit().build();
        assertNull(visit.getId());
        assertNull(visit.getDate());
        assertNull(visit.getDescription());
        assertEquals(0, visit.getPetId());
    }

    @Test
    void shouldBuildVisitWithPartialFields() {
        Visit visit = Visit.VisitBuilder.aVisit()
            .description("partial visit")
            .petId(3)
            .build();

        assertNull(visit.getId());
        assertEquals("partial visit", visit.getDescription());
        assertEquals(3, visit.getPetId());
    }
}
