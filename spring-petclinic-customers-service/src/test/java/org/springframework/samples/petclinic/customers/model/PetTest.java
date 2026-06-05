package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PetTest {

    @Test
    void shouldSetAndGetProperties() {
        Pet pet = new Pet();
        Date birthDate = new Date();

        pet.setId(1);
        pet.setName("Basil");
        pet.setBirthDate(birthDate);

        PetType type = new PetType();
        type.setId(1);
        type.setName("cat");
        pet.setType(type);

        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Bush");
        pet.setOwner(owner);

        assertEquals(1, pet.getId());
        assertEquals("Basil", pet.getName());
        assertEquals(birthDate, pet.getBirthDate());
        assertEquals(type, pet.getType());
        assertEquals(owner, pet.getOwner());
    }

    @Test
    void shouldReturnNullForUnsetFields() {
        Pet pet = new Pet();
        assertNull(pet.getId());
        assertNull(pet.getName());
        assertNull(pet.getBirthDate());
        assertNull(pet.getType());
        assertNull(pet.getOwner());
    }

    @Test
    void shouldImplementEquals() {
        Pet pet1 = new Pet();
        pet1.setId(1);
        pet1.setName("Basil");

        Pet pet2 = new Pet();
        pet2.setId(1);
        pet2.setName("Basil");

        assertEquals(pet1, pet2);
    }

    @Test
    void shouldNotEqualDifferentPet() {
        Pet pet1 = new Pet();
        pet1.setId(1);
        pet1.setName("Basil");

        Pet pet2 = new Pet();
        pet2.setId(2);
        pet2.setName("Leo");

        assertNotEquals(pet1, pet2);
    }

    @Test
    void shouldNotEqualNull() {
        Pet pet = new Pet();
        pet.setId(1);
        assertNotEquals(null, pet);
    }

    @Test
    void shouldNotEqualDifferentType() {
        Pet pet = new Pet();
        pet.setId(1);
        assertNotEquals("not a pet", pet);
    }

    @Test
    void shouldHaveConsistentHashCode() {
        Pet pet1 = new Pet();
        pet1.setId(1);
        pet1.setName("Basil");

        Pet pet2 = new Pet();
        pet2.setId(1);
        pet2.setName("Basil");

        assertEquals(pet1.hashCode(), pet2.hashCode());
    }

    @Test
    void shouldReturnToString() {
        PetType type = new PetType();
        type.setName("cat");

        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Bush");

        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Basil");
        pet.setBirthDate(new Date());
        pet.setType(type);
        pet.setOwner(owner);

        String result = pet.toString();
        assertEquals(true, result.contains("Basil"));
        assertEquals(true, result.contains("cat"));
    }
}
