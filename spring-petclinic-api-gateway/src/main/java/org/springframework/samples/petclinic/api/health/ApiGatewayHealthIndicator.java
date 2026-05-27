/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.api.health;

import java.util.List;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.ReactiveHealthIndicator;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ApiGatewayHealthIndicator implements ReactiveHealthIndicator {

    private final DiscoveryClient discoveryClient;

    public ApiGatewayHealthIndicator(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    public Mono<Health> health() {
        return Mono.fromCallable(() -> {
            List<String> services = discoveryClient.getServices();
            return Health.up()
                .withDetail("service", "api-gateway")
                .withDetail("discoveredServices", services)
                .build();
        }).onErrorResume(e -> Mono.just(
            Health.down()
                .withDetail("service", "api-gateway")
                .withDetail("error", e.getMessage())
                .build()
        ));
    }
}
