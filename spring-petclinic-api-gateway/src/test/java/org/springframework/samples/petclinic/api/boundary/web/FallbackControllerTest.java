package org.springframework.samples.petclinic.api.boundary.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(controllers = FallbackController.class)
class FallbackControllerTest {

    @Autowired
    private WebTestClient client;

    @Test
    void shouldReturnServiceUnavailableOnFallback() {
        client.post()
            .uri("/fallback")
            .exchange()
            .expectStatus().isEqualTo(503)
            .expectBody(String.class)
            .isEqualTo("Chat is currently unavailable. Please try again later.");
    }

    @Test
    void shouldRejectGetOnFallback() {
        client.get()
            .uri("/fallback")
            .exchange()
            .expectStatus().isEqualTo(405);
    }
}
