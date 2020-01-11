/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.contrib.spring.boot.actuate;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.IdsGenerator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Provides auto-configuration of OpenTelemetry tracing and metrics collection and export in Spring
 * Boot actuator-based applications.
 */
@Configuration
@ComponentScan(basePackages = "io.opentelemetry.contrib.spring.boot.actuate")
@ConditionalOnProperty(value = "management.opentelemetry.enabled", matchIfMissing = true)
@EnableConfigurationProperties(OpenTelemetryProperties.class)
public class OpenTelemetryAutoConfiguration {

  @Value("${spring.application.name}")
  private String applicationName;

  @Autowired(required = false)
  private GitProperties gitProperties;

  @Autowired(required = false)
  private BuildProperties buildProperties;

  /**
   * Returns the default {@link Clock} implementation if another clock bean has not been defined.
   *
   * @return the clock
   */
  @ConditionalOnMissingBean
  @Bean
  public Clock otelTracerClock() {
    return MillisClock.getInstance();
  }

  /**
   * Returns the default {@link IdsGenerator} implementation if another generator bean has not been
   * defined.
   *
   * @return the ids generator
   */
  @ConditionalOnMissingBean
  @Bean
  public IdsGenerator otelTracerIdsGenerator() {
    return new SpringManagedRandomIdsGenerator();
  }

  /**
   * Returns the composite {@link Resource} with all info that can be auto-configured included.
   *
   * @param resourceList all resource beans in the application context
   * @return the composite resource
   */
  @Bean
  public Resource otelResource(List<Resource> resourceList) {
    Resource bean = constructServiceResource();
    for (Resource resource : resourceList) {
      bean = bean.merge(resource);
    }
    return bean;
  }

  private Resource constructServiceResource() {
    ServiceResourceFactory factory =
        new ServiceResourceFactory(applicationName, buildProperties, gitProperties);
    return factory.getObject();
  }
}
