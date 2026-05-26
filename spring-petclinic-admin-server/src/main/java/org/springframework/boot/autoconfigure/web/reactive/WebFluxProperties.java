package org.springframework.boot.autoconfigure.web.reactive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Compatibility stub for Spring Boot Admin 3.4.x running on Spring Boot 4.0.
 * Moved to {@code org.springframework.boot.webflux.autoconfigure.WebFluxProperties}.
 */
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "spring.webflux")
public class WebFluxProperties {

	private String basePath;

	public String getBasePath() {
		return this.basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

}
