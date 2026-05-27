package org.springframework.samples.petclinic.genai;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.samples.petclinic.genai.dto.Specialty;
import org.springframework.samples.petclinic.genai.dto.Vet;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VectorStoreControllerTest {

    @Mock
    private VectorStore vectorStore;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    private VectorStoreController vectorStoreController;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient);
        vectorStoreController = new VectorStoreController(vectorStore, webClientBuilder);
    }

    @Test
    void convertListToJsonResourceShouldReturnValidResource() throws Exception {
        Vet vet1 = new Vet(1, "James", "Carter", Set.of(new Specialty(1, "radiology")));
        Vet vet2 = new Vet(2, "Helen", "Leary", Set.of(new Specialty(2, "surgery")));

        Resource resource = vectorStoreController.convertListToJsonResource(List.of(vet1, vet2));

        assertThat(resource).isNotNull();
        byte[] content = resource.getInputStream().readAllBytes();
        String json = new String(content);
        assertThat(json).contains("James");
        assertThat(json).contains("Carter");
        assertThat(json).contains("Helen");
        assertThat(json).contains("Leary");
        assertThat(json).contains("radiology");
        assertThat(json).contains("surgery");
    }

    @Test
    void convertListToJsonResourceShouldHandleEmptyList() throws Exception {
        Resource resource = vectorStoreController.convertListToJsonResource(List.of());

        assertThat(resource).isNotNull();
        byte[] content = resource.getInputStream().readAllBytes();
        String json = new String(content);
        assertThat(json).isEqualTo("[]");
    }

    @Test
    void convertListToJsonResourceShouldHandleVetWithNoSpecialties() throws Exception {
        Vet vet = new Vet(1, "Sharon", "Jenkins", Set.of());

        Resource resource = vectorStoreController.convertListToJsonResource(List.of(vet));

        assertThat(resource).isNotNull();
        byte[] content = resource.getInputStream().readAllBytes();
        String json = new String(content);
        assertThat(json).contains("Sharon");
        assertThat(json).contains("Jenkins");
    }

    @Test
    void convertListToJsonResourceShouldHandleVetWithMultipleSpecialties() throws Exception {
        Vet vet = new Vet(1, "Henry", "Stevens",
            Set.of(new Specialty(1, "radiology"), new Specialty(2, "dentistry")));

        Resource resource = vectorStoreController.convertListToJsonResource(List.of(vet));

        assertThat(resource).isNotNull();
        byte[] content = resource.getInputStream().readAllBytes();
        String json = new String(content);
        assertThat(json).contains("Henry");
        assertThat(json).contains("radiology");
        assertThat(json).contains("dentistry");
    }
}
