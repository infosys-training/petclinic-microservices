package org.springframework.samples.petclinic.genai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetclinicChatClientTest {

    @Mock
    private ChatMemory chatMemory;

    @Mock
    private PetclinicTools petclinicTools;

    private PetclinicChatClient createChatClient(ChatClient chatClient) {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient.Builder afterSystem = mock(ChatClient.Builder.class);
        ChatClient.Builder afterAdvisors = mock(ChatClient.Builder.class);
        ChatClient.Builder afterTools = mock(ChatClient.Builder.class);

        when(builder.defaultSystem(anyString())).thenReturn(afterSystem);
        when(afterSystem.defaultAdvisors(any(Advisor[].class))).thenReturn(afterAdvisors);
        when(afterAdvisors.defaultTools(any(Object[].class))).thenReturn(afterTools);
        when(afterTools.build()).thenReturn(chatClient);

        return new PetclinicChatClient(builder, chatMemory, petclinicTools);
    }

    @Test
    void shouldReturnChatResponse() {
        ChatClient chatClient = mock(ChatClient.class);
        ChatClientRequestSpec promptSpec = mock(ChatClientRequestSpec.class);
        CallResponseSpec callSpec = mock(CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(promptSpec);
        when(promptSpec.user(anyString())).thenReturn(promptSpec);
        when(promptSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn("Hello! I'm the PetClinic assistant.");

        PetclinicChatClient petclinicChatClient = createChatClient(chatClient);
        String response = petclinicChatClient.exchange("Hello");

        assertThat(response).isEqualTo("Hello! I'm the PetClinic assistant.");
    }

    @Test
    void shouldReturnErrorMessageWhenExceptionOccurs() {
        ChatClient chatClient = mock(ChatClient.class);
        ChatClientRequestSpec promptSpec = mock(ChatClientRequestSpec.class);

        when(chatClient.prompt()).thenReturn(promptSpec);
        when(promptSpec.user(anyString())).thenReturn(promptSpec);
        when(promptSpec.call()).thenThrow(new RuntimeException("API unavailable"));

        PetclinicChatClient petclinicChatClient = createChatClient(chatClient);
        String response = petclinicChatClient.exchange("Hello");

        assertThat(response).isEqualTo("Chat is currently unavailable. Please try again later.");
    }
}
