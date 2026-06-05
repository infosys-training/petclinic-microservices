package org.springframework.samples.petclinic.vets.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VetTest {

    @Test
    void shouldSetAndGetProperties() {
        Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("James");
        vet.setLastName("Carter");

        assertEquals(1, vet.getId());
        assertEquals("James", vet.getFirstName());
        assertEquals("Carter", vet.getLastName());
    }

    @Test
    void shouldReturnEmptySpecialtiesByDefault() {
        Vet vet = new Vet();
        assertNotNull(vet.getSpecialties());
        assertTrue(vet.getSpecialties().isEmpty());
        assertEquals(0, vet.getNrOfSpecialties());
    }

    @Test
    void shouldAddSpecialty() {
        Vet vet = new Vet();
        Specialty specialty = new Specialty();
        specialty.setName("radiology");

        vet.addSpecialty(specialty);

        assertEquals(1, vet.getNrOfSpecialties());
        assertEquals("radiology", vet.getSpecialties().get(0).getName());
    }

    @Test
    void shouldSortSpecialtiesByName() {
        Vet vet = new Vet();

        Specialty s1 = new Specialty();
        s1.setName("surgery");
        vet.addSpecialty(s1);

        Specialty s2 = new Specialty();
        s2.setName("dentistry");
        vet.addSpecialty(s2);

        assertEquals("dentistry", vet.getSpecialties().get(0).getName());
        assertEquals("surgery", vet.getSpecialties().get(1).getName());
    }

    @Test
    void shouldAddMultipleSpecialties() {
        Vet vet = new Vet();

        Specialty s1 = new Specialty();
        s1.setName("radiology");
        vet.addSpecialty(s1);

        Specialty s2 = new Specialty();
        s2.setName("surgery");
        vet.addSpecialty(s2);

        Specialty s3 = new Specialty();
        s3.setName("dentistry");
        vet.addSpecialty(s3);

        assertEquals(3, vet.getNrOfSpecialties());
    }
}
