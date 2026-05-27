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
package org.springframework.samples.petclinic.discovery;

import com.netflix.eureka.EurekaServerContext;
import com.netflix.eureka.EurekaServerContextHolder;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class DiscoveryServerHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            EurekaServerContext serverContext = EurekaServerContextHolder.getInstance().getServerContext();
            int registeredApps = serverContext.getRegistry().getSortedApplications().size();
            return Health.up()
                .withDetail("service", "discovery-server")
                .withDetail("registeredApplications", registeredApps)
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("service", "discovery-server")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
