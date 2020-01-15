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

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.distributedcontext.DistributedContextManager;
import io.opentelemetry.sdk.distributedcontext.DistributedContextManagerSdk;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link DistributedContextManagerSdkBean}. */
@RunWith(JUnit4.class)
public class DistributedContextManagerSdkBeanTest {

  @Test
  public void shouldConstructDistributedContextManagerFromDefaults() {
    OpenTelemetryProperties properties = new OpenTelemetryProperties();
    DistributedContextManagerSdkBean factoryBean = new DistributedContextManagerSdkBean();
    factoryBean.setProperties(properties);
    factoryBean.afterPropertiesSet();
    DistributedContextManager manager = factoryBean.getObject();
    assertThat(manager).isInstanceOf(DistributedContextManagerSdk.class);
  }
}
