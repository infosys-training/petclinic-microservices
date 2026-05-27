package org.springframework.samples.petclinic.genai;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.genai.dto.OwnerDetails;
import org.springframework.samples.petclinic.genai.dto.PetDetails;
import org.springframework.samples.petclinic.genai.dto.PetRequest;
import org.springframework.samples.petclinic.genai.dto.PetType;
import org.springframework.samples.petclinic.genai.dto.Specialty;
import org.springframework.samples.petclinic.genai.dto.Vet;
import tools.jackson.core.JacksonException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PetclinicToolsTest {

    @Mock
    private AIDataProvider aiDataProvider;

    @InjectMocks
    private PetclinicTools petclinicTools;

    @Test
    void listOwnersShouldReturnAllOwners() {
        OwnerDetails owner = new OwnerDetails(1, "John", "Doe", "123 Main St", "Springfield", "1234567890", Collections.emptyList());
        given(aiDataProvider.getAllOwners()).willReturn(List.of(owner));

        List<OwnerDetails> result = petclinicTools.listOwners();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).firstName()).isEqualTo("John");
        assertThat(result.get(0).lastName()).isEqualTo("Doe");
    }

    @Test
    void listOwnersShouldReturnEmptyListWhenNoOwners() {
        given(aiDataProvider.getAllOwners()).willReturn(Collections.emptyList());

        List<OwnerDetails> result = petclinicTools.listOwners();

        assertThat(result).isEmpty();
    }

    @Test
    void addOwnerToPetclinicShouldReturnCreatedOwner() {
        OwnerRequest request = new OwnerRequest("Jane", "Smith", "456 Oak Ave", "Shelbyville", "9876543210");
        OwnerDetails expectedOwner = new OwnerDetails(2, "Jane", "Smith", "456 Oak Ave", "Shelbyville", "9876543210", Collections.emptyList());
        given(aiDataProvider.addOwnerToPetclinic(request)).willReturn(expectedOwner);

        OwnerDetails result = petclinicTools.addOwnerToPetclinic(request);

        assertThat(result.id()).isEqualTo(2);
        assertThat(result.firstName()).isEqualTo("Jane");
        assertThat(result.lastName()).isEqualTo("Smith");
        assertThat(result.address()).isEqualTo("456 Oak Ave");
        assertThat(result.city()).isEqualTo("Shelbyville");
        assertThat(result.telephone()).isEqualTo("9876543210");
    }

    @Test
    void listVetsShouldReturnVetInformation() throws JacksonException {
        Vet vetRequest = new Vet(1, "James", "Carter", Set.of(new Specialty(1, "radiology")));
        given(aiDataProvider.getVets(vetRequest)).willReturn(List.of("James Carter - radiology"));

        List<String> result = petclinicTools.listVets(vetRequest);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).contains("James Carter");
    }

    @Test
    void listVetsShouldReturnEmptyListOnJacksonException() throws JacksonException {
        Vet vetRequest = new Vet(1, "James", "Carter", Set.of());
        given(aiDataProvider.getVets(vetRequest)).willThrow(new TestJacksonException("Test error"));

        List<String> result = petclinicTools.listVets(vetRequest);

        assertThat(result).isEmpty();
    }

    @Test
    void addPetToOwnerShouldReturnCreatedPet() {
        PetRequest petRequest = new PetRequest(0, new Date(), "Buddy", 2);
        PetDetails expectedPet = new PetDetails(1, "Buddy", "2024-01-01", new PetType("dog"), Collections.emptyList());
        given(aiDataProvider.addPetToOwner(eq(1), any(PetRequest.class))).willReturn(expectedPet);

        PetDetails result = petclinicTools.addPetToOwner(1, petRequest);

        assertThat(result.id()).isEqualTo(1);
        assertThat(result.name()).isEqualTo("Buddy");
        assertThat(result.type().name()).isEqualTo("dog");
    }

    /**
     * A test-only JacksonException subclass for testing error handling.
     */
    private static class TestJacksonException extends JacksonException {
        TestJacksonException(String message) {
            super(message);
        }
    }
}
