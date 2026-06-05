package org.springframework.samples.petclinic.customers.web;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetRepository;
import org.springframework.samples.petclinic.customers.model.PetType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PetResource.class)
@ActiveProfiles("test")
class PetResourceNegativeTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    PetRepository petRepository;

    @MockitoBean
    OwnerRepository ownerRepository;

    @Test
    void shouldReturn404WhenPetNotFound() throws Exception {
        given(petRepository.findById(999)).willReturn(Optional.empty());

        mvc.perform(get("/owners/1/pets/999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenCreatingPetForNonExistentOwner() throws Exception {
        given(ownerRepository.findById(999)).willReturn(Optional.empty());

        String petJson = "{\"id\":0,\"name\":\"Leo\",\"birthDate\":\"2020-01-01\",\"typeId\":1}";

        mvc.perform(post("/owners/999/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(petJson))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreatePetSuccessfully() throws Exception {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Bush");

        PetType petType = new PetType();
        petType.setId(1);
        petType.setName("cat");

        Pet savedPet = new Pet();
        savedPet.setId(1);
        savedPet.setName("Leo");
        savedPet.setBirthDate(new Date());
        savedPet.setType(petType);

        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));
        given(petRepository.findPetTypeById(1)).willReturn(Optional.of(petType));
        given(petRepository.save(any(Pet.class))).willReturn(savedPet);

        String petJson = "{\"id\":0,\"name\":\"Leo\",\"birthDate\":\"2020-01-01\",\"typeId\":1}";

        mvc.perform(post("/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(petJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Leo"));
    }

    @Test
    void shouldUpdatePetSuccessfully() throws Exception {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Bush");

        PetType petType = new PetType();
        petType.setId(1);
        petType.setName("cat");

        Pet existingPet = new Pet();
        existingPet.setId(1);
        existingPet.setName("Leo");
        existingPet.setBirthDate(new Date());
        existingPet.setType(petType);
        owner.addPet(existingPet);

        given(petRepository.findById(1)).willReturn(Optional.of(existingPet));
        given(petRepository.findPetTypeById(eq(1))).willReturn(Optional.of(petType));
        given(petRepository.save(any(Pet.class))).willReturn(existingPet);

        String petJson = "{\"id\":1,\"name\":\"Leonardo\",\"birthDate\":\"2020-01-01\",\"typeId\":1}";

        mvc.perform(put("/owners/1/pets/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(petJson))
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldGetPetTypes() throws Exception {
        PetType cat = new PetType();
        cat.setId(1);
        cat.setName("cat");

        PetType dog = new PetType();
        dog.setId(2);
        dog.setName("dog");

        given(petRepository.findPetTypes()).willReturn(List.of(cat, dog));

        mvc.perform(get("/petTypes").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("cat"))
            .andExpect(jsonPath("$[1].name").value("dog"));
    }

    @Test
    void shouldReturnEmptyPetTypes() throws Exception {
        given(petRepository.findPetTypes()).willReturn(Collections.emptyList());

        mvc.perform(get("/petTypes").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }
}
