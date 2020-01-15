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

package io.opentelemetry.contrib.spring.boot;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.contrib.spring.boot.OpenTelemetryProperties.SamplerName;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.trace.TracerSdkRegistry;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TracerSdkRegistryBean}. */
@RunWith(JUnit4.class)
public class TracerSdkRegistryBeanTest {

  @Test
  public void shouldConstructCustomSamplerIfConfigured() {
    OpenTelemetryProperties properties = new OpenTelemetryProperties();
    properties.getTracer().getSampler().setName(SamplerName.CUSTOM);
    properties
        .getTracer()
        .getSampler()
        .setImplClass("io.opentelemetry.contrib.spring.boot.TestOnlySampler");
    properties.getTracer().getSampler().getProperties().put("reservoir", "10");
    properties.getTracer().getSampler().getProperties().put("rate", "2.5");
    properties.getTracer().getSampler().getProperties().put("enabled", "true");
    TracerSdkRegistryBean factoryBean = new TracerSdkRegistryBean();
    addDependencies(factoryBean);
    factoryBean.setProperties(properties);
    factoryBean.afterPropertiesSet();
    TracerSdkRegistry tracerSdkRegistry = (TracerSdkRegistry) factoryBean.getObject();
    TraceConfig traceConfig = tracerSdkRegistry.getActiveTraceConfig();
    assertThat(traceConfig.getSampler()).isInstanceOf(TestOnlySampler.class);
  }

  @Test
  public void shouldUseInjectedSamplerIfPresent() {
    TestOnlySampler sampler = new TestOnlySampler();
    OpenTelemetryProperties properties = new OpenTelemetryProperties();
    TracerSdkRegistryBean factoryBean = new TracerSdkRegistryBean();
    addDependencies(factoryBean);
    factoryBean.setProperties(properties);
    factoryBean.setSampler(sampler);
    factoryBean.afterPropertiesSet();
    TracerSdkRegistry tracerSdkRegistry = (TracerSdkRegistry) factoryBean.getObject();
    TraceConfig traceConfig = tracerSdkRegistry.getActiveTraceConfig();
    assertThat(traceConfig.getSampler()).isSameInstanceAs(sampler);
  }

  private static void addDependencies(TracerSdkRegistryBean factoryBean) {
    factoryBean.setOtelClock(MillisClock.getInstance());
    factoryBean.setOtelIdsGenerator(new SpringManagedRandomIdsGenerator());
    ServiceResourceFactory serviceResourceFactory = new ServiceResourceFactory("junit", null, null);
    factoryBean.setOtelResource(serviceResourceFactory.getObject());
  }
}
