package org.springframework.samples.petclinic.vets.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SpecialtyTest {

    @Test
    void shouldSetAndGetProperties() {
        Specialty specialty = new Specialty();
        specialty.setName("radiology");

        assertEquals("radiology", specialty.getName());
    }

    @Test
    void shouldReturnNullForUnsetProperties() {
        Specialty specialty = new Specialty();

        assertNull(specialty.getId());
        assertNull(specialty.getName());
    }
}
