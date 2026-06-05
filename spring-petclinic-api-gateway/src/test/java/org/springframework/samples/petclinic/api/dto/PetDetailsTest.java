package org.springframework.samples.petclinic.api.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PetDetailsTest {

    @Test
    void shouldBuildPetDetailsWithAllFields() {
        PetType type = new PetType("cat");
        List<VisitDetails> visits = new ArrayList<>();
        visits.add(new VisitDetails(1, 10, "2024-01-01", "checkup"));

        PetDetails pet = PetDetails.PetDetailsBuilder.aPetDetails()
            .id(10)
            .name("Garfield")
            .birthDate("2020-01-01")
            .type(type)
            .visits(visits)
            .build();

        assertEquals(10, pet.id());
        assertEquals("Garfield", pet.name());
        assertEquals("2020-01-01", pet.birthDate());
        assertEquals("cat", pet.type().name());
        assertEquals(1, pet.visits().size());
    }

    @Test
    void shouldInitializeVisitsToEmptyListWhenNull() {
        PetDetails pet = new PetDetails(1, "Garfield", "2020-01-01", null, null);
        assertNotNull(pet.visits());
        assertTrue(pet.visits().isEmpty());
    }

    @Test
    void shouldPreserveProvidedVisitsList() {
        List<VisitDetails> visits = new ArrayList<>();
        visits.add(new VisitDetails(1, 10, "2024-01-01", "checkup"));

        PetDetails pet = new PetDetails(10, "Garfield", "2020-01-01", null, visits);
        assertEquals(1, pet.visits().size());
    }
}
