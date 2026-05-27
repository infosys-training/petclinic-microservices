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
package org.springframework.samples.petclinic.vets.health;

import javax.sql.DataSource;
import java.sql.Connection;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class VetsServiceHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    private final CacheManager cacheManager;

    public VetsServiceHealthIndicator(DataSource dataSource, CacheManager cacheManager) {
        this.dataSource = dataSource;
        this.cacheManager = cacheManager;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            boolean cacheAvailable = cacheManager.getCacheNames() != null;
            return Health.up()
                .withDetail("service", "vets-service")
                .withDetail("database", connection.getMetaData().getDatabaseProductName())
                .withDetail("cacheAvailable", cacheAvailable)
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("service", "vets-service")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
