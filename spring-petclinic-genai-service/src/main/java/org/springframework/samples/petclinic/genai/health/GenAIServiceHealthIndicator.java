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
package org.springframework.samples.petclinic.genai.health;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class GenAIServiceHealthIndicator implements HealthIndicator {

    private final ChatClient.Builder chatClientBuilder;

    public GenAIServiceHealthIndicator(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    @Override
    public Health health() {
        try {
            boolean clientAvailable = chatClientBuilder != null;
            return Health.up()
                .withDetail("service", "genai-service")
                .withDetail("aiClientConfigured", clientAvailable)
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("service", "genai-service")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
