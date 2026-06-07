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
import org.springframework.samples.petclinic.api.dto.OwnerDetails;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    classes = CustomersServiceContractConsumerTest.TestConfig.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@AutoConfigureStubRunner(
    ids = "org.springframework.samples.petclinic.client:spring-petclinic-customers-service:+:stubs",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class CustomersServiceContractConsumerTest {

    @Configuration
    @EnableAutoConfiguration(exclude = {
        GatewayAutoConfiguration.class,
        GatewayReactiveLoadBalancerClientAutoConfiguration.class
    })
    static class TestConfig {
    }

    @StubRunnerPort("spring-petclinic-customers-service")
    int stubPort;

    @Test
    void shouldGetOwnerFromCustomersService() {
        OwnerDetails owner = WebClient.builder()
            .baseUrl("http://localhost:" + stubPort)
            .build()
            .get()
            .uri("/owners/1")
            .retrieve()
            .bodyToMono(OwnerDetails.class)
            .block();

        assertThat(owner).isNotNull();
        assertThat(owner.id()).isEqualTo(1);
        assertThat(owner.firstName()).isEqualTo("George");
        assertThat(owner.lastName()).isEqualTo("Franklin");
        assertThat(owner.address()).isEqualTo("110 W. Liberty St.");
        assertThat(owner.city()).isEqualTo("Madison");
        assertThat(owner.telephone()).isEqualTo("6085551023");
        assertThat(owner.pets()).isNotEmpty();
        assertThat(owner.pets().get(0).name()).isEqualTo("Leo");
    }
}
