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

import io.opentelemetry.distributedcontext.DistributedContextManager;
import io.opentelemetry.metrics.MeterRegistry;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.IdsGenerator;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.trace.TracerRegistry;
import java.util.List;
import org.springframework.beans.factory.BeanCreationException;
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
  public Clock otelClock() {
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
  public IdsGenerator otelIdsGenerator() {
    return new SpringManagedRandomIdsGenerator();
  }

  /**
   * Returns the composite {@link Resource} with all info that can be auto-configured included.
   *
   * @param resourceList all resource supplier beans in the application context
   * @return the composite resource
   */
  @Bean
  public Resource otelResource(List<ResourceProvider> resourceList) {
    Resource bean = constructServiceResource();
    for (ResourceProvider resource : resourceList) {
      try {
        bean = bean.merge(resource.getObject());
      } catch (RuntimeException cause) {
        throw cause;
      } catch (Exception cause) {
        throw new BeanCreationException("Unable to retrieve Resouce info", cause);
      }
    }
    return bean;
  }

  /**
   * Returns a {@link TracerRegistry} from the OpenTelemetry SDK configured with the components
   * supplied as parameters. Only executes if another tracer registry bean has not been defined.
   *
   * @param properties the configuration properties from Spring property sources
   * @param clock the timestamp generator to use
   * @param idsGenerator the id generator to use
   * @param resource the resource labels to add to telemetry data
   * @param spanProcessors all tracing span processors managed by Spring
   * @param spanExporters all trace data exporters managed by Spring
   * @return the tracer registry
   */
  @ConditionalOnMissingBean
  @Bean
  public TracerRegistry tracerRegistry(
      OpenTelemetryProperties properties,
      Clock clock,
      IdsGenerator idsGenerator,
      Resource resource,
      List<SpanProcessor> spanProcessors,
      List<SpanExporter> spanExporters) {
    TracerSdkRegistryBean factory = new TracerSdkRegistryBean();
    factory.setProperties(properties);
    factory.setOtelClock(clock);
    factory.setOtelIdsGenerator(idsGenerator);
    factory.setOtelResource(resource);
    factory.setSpanProcessors(spanProcessors);
    factory.setSpanExporters(spanExporters);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  /**
   * Returns a {@link MeterRegistry} from the OpenTelemetry SDK configured with the components
   * supplied as parameters. Only executes if another meter registry bean has not been defined.
   *
   * @param properties the configuration properties from Spring property sources
   * @param clock the timestamp generator to use
   * @param resource the resource labels to add to telemetry data
   * @return the meter registry
   */
  @ConditionalOnMissingBean
  @Bean
  public MeterRegistry meterRegistry(
      OpenTelemetryProperties properties, Clock clock, Resource resource) {
    MeterSdkRegistryBean factory = new MeterSdkRegistryBean();
    factory.setProperties(properties);
    factory.setClock(clock);
    factory.setResource(resource);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  /**
   * Returns a {@link DistributedContextManager} from the OpenTelemetry SDK configured with the
   * components supplied as parameters. Only executes if another manager bean has not been defined.
   *
   * @param properties the configuration properties from Spring property sources
   * @return the distributed context manager
   */
  @ConditionalOnMissingBean
  @Bean
  public DistributedContextManager distributedContextManager(OpenTelemetryProperties properties) {
    DistributedContextManagerSdkBean factory = new DistributedContextManagerSdkBean();
    factory.setProperties(properties);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  private Resource constructServiceResource() {
    ServiceResourceFactory factory =
        new ServiceResourceFactory(applicationName, buildProperties, gitProperties);
    return factory.getObject();
  }
}
