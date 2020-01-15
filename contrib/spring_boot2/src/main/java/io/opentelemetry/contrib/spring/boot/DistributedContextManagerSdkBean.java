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

import io.opentelemetry.distributedcontext.DistributedContextManager;
import io.opentelemetry.sdk.distributedcontext.DistributedContextManagerSdk;
import io.opentelemetry.sdk.distributedcontext.DistributedContextManagerSdkProvider;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Creates an OpenTelemetry {@link DistributedContextManager} from the default SDK the Spring way.
 */
public class DistributedContextManagerSdkBean
    implements FactoryBean<DistributedContextManager>, InitializingBean {

  private OpenTelemetryProperties properties;
  private DistributedContextManager distributedContextManager;

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
    distributedContextManager = initializeDistributedContextManager();
  }

  @Override
  public DistributedContextManager getObject() {
    if (distributedContextManager == null) {
      distributedContextManager = initializeDistributedContextManager();
    }
    return distributedContextManager;
  }

  @Override
  public Class<?> getObjectType() {
    return DistributedContextManager.class;
  }

  private DistributedContextManager initializeDistributedContextManager() {
    DistributedContextManagerSdkProvider provider = new DistributedContextManagerSdkProvider();
    DistributedContextManagerSdk manager = (DistributedContextManagerSdk) provider.create();
    if (properties.isEnabled()) {
      manager.getHttpTextFormat();
    }
    return manager;
  }
}
