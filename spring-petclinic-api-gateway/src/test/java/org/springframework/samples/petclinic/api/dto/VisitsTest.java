package org.springframework.samples.petclinic.api.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisitsTest {

    @Test
    void shouldCreateWithItems() {
        VisitDetails visit = new VisitDetails(1, 10, "2024-01-01", "checkup");
        Visits visits = new Visits(List.of(visit));

        assertEquals(1, visits.items().size());
        assertEquals("checkup", visits.items().get(0).description());
    }

    @Test
    void shouldCreateEmptyVisitsWithDefaultConstructor() {
        Visits visits = new Visits();

        assertNotNull(visits.items());
        assertTrue(visits.items().isEmpty());
    }

    @Test
    void shouldCreateWithEmptyList() {
        Visits visits = new Visits(List.of());

        assertNotNull(visits.items());
        assertTrue(visits.items().isEmpty());
    }
}
