package org.springframework.samples.petclinic.api.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OwnerDetailsTest {

    @Test
    void shouldGetPetIds() {
        PetDetails pet1 = PetDetails.PetDetailsBuilder.aPetDetails().id(10).build();
        PetDetails pet2 = PetDetails.PetDetailsBuilder.aPetDetails().id(20).build();

        OwnerDetails owner = OwnerDetails.OwnerDetailsBuilder.anOwnerDetails()
            .id(1)
            .firstName("George")
            .lastName("Franklin")
            .pets(List.of(pet1, pet2))
            .build();

        assertEquals(List.of(10, 20), owner.getPetIds());
    }

    @Test
    void shouldReturnEmptyPetIdsWhenNoPets() {
        OwnerDetails owner = OwnerDetails.OwnerDetailsBuilder.anOwnerDetails()
            .pets(List.of())
            .build();

        assertNotNull(owner.getPetIds());
        assertTrue(owner.getPetIds().isEmpty());
    }

    @Test
    void shouldBuildOwnerWithAllFields() {
        OwnerDetails owner = OwnerDetails.OwnerDetailsBuilder.anOwnerDetails()
            .id(1)
            .firstName("George")
            .lastName("Franklin")
            .address("110 W. Liberty St.")
            .city("Madison")
            .telephone("6085551023")
            .pets(List.of())
            .build();

        assertEquals(1, owner.id());
        assertEquals("George", owner.firstName());
        assertEquals("Franklin", owner.lastName());
        assertEquals("110 W. Liberty St.", owner.address());
        assertEquals("Madison", owner.city());
        assertEquals("6085551023", owner.telephone());
    }
}
