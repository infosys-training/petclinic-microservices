package org.springframework.samples.petclinic.customers.web.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.web.OwnerRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OwnerEntityMapperTest {

    private final OwnerEntityMapper mapper = new OwnerEntityMapper();

    @Test
    void shouldMapRequestToOwner() {
        Owner owner = new Owner();
        OwnerRequest request = new OwnerRequest("George", "Franklin", "110 W. Liberty St.", "Madison", "6085551023");

        Owner result = mapper.map(owner, request);

        assertEquals("George", result.getFirstName());
        assertEquals("Franklin", result.getLastName());
        assertEquals("110 W. Liberty St.", result.getAddress());
        assertEquals("Madison", result.getCity());
        assertEquals("6085551023", result.getTelephone());
    }

    @Test
    void shouldOverwriteExistingOwnerFields() {
        Owner owner = new Owner();
        owner.setFirstName("OldFirst");
        owner.setLastName("OldLast");
        owner.setAddress("OldAddress");
        owner.setCity("OldCity");
        owner.setTelephone("0000000000");

        OwnerRequest request = new OwnerRequest("NewFirst", "NewLast", "NewAddress", "NewCity", "1111111111");

        Owner result = mapper.map(owner, request);

        assertEquals("NewFirst", result.getFirstName());
        assertEquals("NewLast", result.getLastName());
        assertEquals("NewAddress", result.getAddress());
        assertEquals("NewCity", result.getCity());
        assertEquals("1111111111", result.getTelephone());
    }

    @Test
    void shouldReturnSameOwnerInstance() {
        Owner owner = new Owner();
        OwnerRequest request = new OwnerRequest("George", "Franklin", "110 W. Liberty St.", "Madison", "6085551023");

        Owner result = mapper.map(owner, request);

        assertEquals(owner, result);
    }
}
