package org.springframework.samples.petclinic.genai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AIBeanConfigurationTest {

    @Mock
    private EmbeddingModel embeddingModel;

    @Test
    void vectorStoreBeanShouldBeCreated() {
        AIBeanConfiguration config = new AIBeanConfiguration();
        VectorStore vectorStore = config.vectorStore(embeddingModel);
        assertThat(vectorStore).isNotNull();
    }

    @Test
    void loadBalancedWebClientBuilderShouldBeCreated() {
        AIBeanConfiguration config = new AIBeanConfiguration();
        WebClient.Builder builder = config.loadBalancedWebClientBuilder();
        assertThat(builder).isNotNull();
    }
}
