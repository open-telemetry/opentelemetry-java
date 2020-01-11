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

import io.opentelemetry.metrics.MeterRegistry;
import io.opentelemetry.sdk.metrics.MeterSdkRegistry;
import org.junit.Test;

/** Unit tests for {@link MeterSdkRegistryBean}. */
public class MeterSdkRegistryBeanTest {

  @Test
  public void shouldConstructMeterRegistryFromDefaults() {
    OpenTelemetryProperties properties = new OpenTelemetryProperties();
    MeterSdkRegistryBean factoryBean = new MeterSdkRegistryBean();
    factoryBean.setProperties(properties);
    factoryBean.afterPropertiesSet();
    MeterRegistry meterRegistry = factoryBean.getObject();
    assertThat(meterRegistry).isInstanceOf(MeterSdkRegistry.class);
  }
}
