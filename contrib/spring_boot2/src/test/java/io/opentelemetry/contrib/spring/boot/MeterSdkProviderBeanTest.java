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

import io.opentelemetry.metrics.MeterProvider;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.metrics.MeterSdkProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MeterSdkProviderBean}. */
@RunWith(JUnit4.class)
public class MeterSdkProviderBeanTest {

  @Test
  public void shouldConstructMeterRegistryFromDefaults() {
    OpenTelemetryProperties properties = new OpenTelemetryProperties();
    MeterSdkProviderBean factoryBean = new MeterSdkProviderBean();
    factoryBean.setProperties(properties);
    addDependencies(factoryBean);
    factoryBean.afterPropertiesSet();
    MeterProvider meterProvider = factoryBean.getObject();
    assertThat(meterProvider).isInstanceOf(MeterSdkProvider.class);
  }

  private static void addDependencies(MeterSdkProviderBean factoryBean) {
    factoryBean.setOtelClock(MillisClock.getInstance());
    ServiceResourceFactory serviceResourceFactory = new ServiceResourceFactory("junit", null, null);
    factoryBean.setOtelResource(serviceResourceFactory.getObject());
  }
}
