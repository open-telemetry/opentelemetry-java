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

import io.opentelemetry.correlationcontext.CorrelationContextManager;
import io.opentelemetry.sdk.correlationcontext.CorrelationContextManagerSdk;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Creates an OpenTelemetry {@link CorrelationContextManager} from the default SDK the Spring way.
 */
public class CorrelationContextManagerSdkBean
    implements FactoryBean<CorrelationContextManager>, InitializingBean {

  private OpenTelemetryProperties properties;
  private CorrelationContextManager correlationContextManager;

  /**
   * Sets the Spring application properties used to configure the OpenTelemetry SDK.
   *
   * @param properties the properties
   */
  @Autowired
  public void setProperties(OpenTelemetryProperties properties) {
    this.properties = properties;
  }

  @Override
  public void afterPropertiesSet() {
    correlationContextManager = initializeCorrelationContextManager();
  }

  @Override
  public CorrelationContextManager getObject() {
    if (correlationContextManager == null) {
      correlationContextManager = initializeCorrelationContextManager();
    }
    return correlationContextManager;
  }

  @Override
  public Class<?> getObjectType() {
    return CorrelationContextManager.class;
  }

  private CorrelationContextManager initializeCorrelationContextManager() {
    CorrelationContextManagerSdk manager = new CorrelationContextManagerSdk();
    if (properties.isEnabled()) {
      manager.getHttpTextFormat();
    }
    return manager;
  }
}
