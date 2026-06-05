package org.springframework.samples.petclinic.api.contracts;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerPort;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.gateway.config.GatewayAutoConfiguration;
import org.springframework.cloud.gateway.config.GatewayReactiveLoadBalancerClientAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.petclinic.api.dto.Visits;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    classes = VisitsServiceContractConsumerTest.TestConfig.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@AutoConfigureStubRunner(
    ids = "org.springframework.samples.petclinic.visits:spring-petclinic-visits-service:+:stubs",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class VisitsServiceContractConsumerTest {

    @Configuration
    @EnableAutoConfiguration(exclude = {
        GatewayAutoConfiguration.class,
        GatewayReactiveLoadBalancerClientAutoConfiguration.class
    })
    static class TestConfig {
    }

    @StubRunnerPort("spring-petclinic-visits-service")
    int stubPort;

    @Test
    void shouldGetVisitsFromVisitsService() {
        Visits visits = WebClient.builder()
            .baseUrl("http://localhost:" + stubPort)
            .build()
            .get()
            .uri("/pets/visits?petId=7")
            .retrieve()
            .bodyToMono(Visits.class)
            .block();

        assertThat(visits).isNotNull();
        assertThat(visits.items()).isNotEmpty();
        assertThat(visits.items().get(0).petId()).isEqualTo(7);
        assertThat(visits.items().get(0).description()).isEqualTo("rabies shot");
    }
}
