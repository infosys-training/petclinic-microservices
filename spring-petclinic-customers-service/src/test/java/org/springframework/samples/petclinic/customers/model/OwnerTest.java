package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OwnerTest {

    @Test
    void shouldSetAndGetProperties() {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Franklin");
        owner.setAddress("110 W. Liberty St.");
        owner.setCity("Madison");
        owner.setTelephone("6085551023");

        assertEquals("George", owner.getFirstName());
        assertEquals("Franklin", owner.getLastName());
        assertEquals("110 W. Liberty St.", owner.getAddress());
        assertEquals("Madison", owner.getCity());
        assertEquals("6085551023", owner.getTelephone());
    }

    @Test
    void shouldReturnEmptyPetsListByDefault() {
        Owner owner = new Owner();
        assertNotNull(owner.getPets());
        assertTrue(owner.getPets().isEmpty());
    }

    @Test
    void shouldAddPetAndSetOwner() {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Franklin");

        Pet pet = new Pet();
        pet.setName("Basil");
        owner.addPet(pet);

        assertEquals(1, owner.getPets().size());
        assertEquals("Basil", owner.getPets().get(0).getName());
        assertEquals(owner, pet.getOwner());
    }

    @Test
    void shouldSortPetsByName() {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Franklin");

        Pet pet1 = new Pet();
        pet1.setName("Zorro");
        owner.addPet(pet1);

        Pet pet2 = new Pet();
        pet2.setName("Alpha");
        owner.addPet(pet2);

        assertEquals("Alpha", owner.getPets().get(0).getName());
        assertEquals("Zorro", owner.getPets().get(1).getName());
    }

    @Test
    void shouldReturnToString() {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Franklin");
        owner.setAddress("110 W. Liberty St.");
        owner.setCity("Madison");
        owner.setTelephone("6085551023");

        String result = owner.toString();
        assertTrue(result.contains("George"));
        assertTrue(result.contains("Franklin"));
    }
}
