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

import io.opentelemetry.metrics.MeterFactory;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.metrics.MeterSdkFactory;
import io.opentelemetry.sdk.metrics.MeterSdkFactoryProvider;
import io.opentelemetry.sdk.resources.EnvVarResource;
import io.opentelemetry.sdk.resources.Resource;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Creates an OpenTelemetry {@link MeterFactory} from the default SDK the Spring way. */
@Component
public class MeterSdkFactoryBean implements FactoryBean<MeterFactory>, InitializingBean {

  private OpenTelemetryProperties properties;
  private Clock clock;
  private Resource resource;
  private MeterFactory meterFactory;

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
   * @param clock the clock
   */
  @Autowired(required = false)
  public void setClock(Clock clock) {
    this.clock = clock;
  }

  /**
   * Sets the Spring-managed implementation of OpenTelemetry {@link Resource} which populates global
   * application attributes.
   *
   * @param resource the resource populator
   */
  @Autowired(required = false)
  public void setResource(Resource resource) {
    this.resource = resource;
  }

  @Override
  public void afterPropertiesSet() {
    meterFactory = initializeMeterFactory();
  }

  @Override
  public MeterFactory getObject() {
    if (meterFactory == null) {
      meterFactory = initializeMeterFactory();
    }
    return meterFactory;
  }

  @Override
  public Class<?> getObjectType() {
    return MeterFactory.class;
  }

  private MeterFactory initializeMeterFactory() {
    MeterSdkFactoryProvider provider = new MeterSdkFactoryProvider();
    MeterSdkFactory factory = (MeterSdkFactory) provider.create();
    if (properties.getMeter().isExport()) {
      constructMeterSharedState();
    }
    return factory;
  }

  private Object constructMeterSharedState() {
    Map<String, Object> state = new HashMap<>();
    state.put("clock", prepareClock());
    state.put("resource", prepareResource());
    return state;
  }

  private Clock prepareClock() {
    if (clock != null) {
      return clock;
    }
    return MillisClock.getInstance();
  }

  private Resource prepareResource() {
    if (resource != null) {
      return resource;
    }
    return EnvVarResource.getResource();
  }
}
