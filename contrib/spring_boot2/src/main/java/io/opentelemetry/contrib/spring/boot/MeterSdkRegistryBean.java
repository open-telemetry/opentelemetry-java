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

import io.opentelemetry.metrics.MeterRegistry;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.MeterSdkRegistry;
import io.opentelemetry.sdk.resources.Resource;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/** Creates an OpenTelemetry {@link MeterRegistry} from the default SDK the Spring way. */
public class MeterSdkRegistryBean implements FactoryBean<MeterRegistry>, InitializingBean {

  private OpenTelemetryProperties properties;
  private Clock otelClock;
  private Resource otelResource;
  private MeterRegistry meterRegistry;

  /**
   * Sets the Spring application properties used to configure the OpenTelemetry SDK.
   *
   * @param properties the properties
   */
  @Autowired
  public void setProperties(OpenTelemetryProperties properties) {
    this.properties = properties;
  }

  /**
   * Sets the Spring-managed OpenTelemetry clock implementation for generating span timestamps.
   *
   * @param otelClock the clock
   */
  @Autowired
  public void setOtelClock(Clock otelClock) {
    this.otelClock = otelClock;
  }

  /**
   * Sets the Spring-managed implementation of OpenTelemetry {@link Resource} which populates global
   * application attributes.
   *
   * @param otelResource the resource populator
   */
  @Autowired
  @Qualifier("otelResource")
  public void setOtelResource(Resource otelResource) {
    this.otelResource = otelResource;
  }

  @Override
  public void afterPropertiesSet() {
    meterRegistry = initializeMeterRegistry();
  }

  @Override
  public MeterRegistry getObject() {
    if (meterRegistry == null) {
      meterRegistry = initializeMeterRegistry();
    }
    return meterRegistry;
  }

  @Override
  public Class<?> getObjectType() {
    return MeterRegistry.class;
  }

  private MeterRegistry initializeMeterRegistry() {
    MeterSdkRegistry registry =
        MeterSdkRegistry.builder().setClock(otelClock).setResource(otelResource).build();
    if (properties.getMeter().isExport()) {
      registry.get("io.opentelemetry.contrib.spring.boot", "1.0");
    }
    return registry;
  }
}
