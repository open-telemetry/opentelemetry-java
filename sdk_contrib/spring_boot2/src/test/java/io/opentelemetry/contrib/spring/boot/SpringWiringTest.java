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

import io.opentelemetry.correlationcontext.CorrelationContextManager;
import io.opentelemetry.metrics.MeterProvider;
import io.opentelemetry.sdk.correlationcontext.CorrelationContextManagerSdk;
import io.opentelemetry.sdk.metrics.MeterSdkProvider;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.TracerProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/** Testing of Spring wiring. */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {OpenTelemetryAutoConfiguration.class})
@TestPropertySource(locations = {"classpath:application.properties"})
public class SpringWiringTest {

  @Autowired private TracerProvider tracerProvider;
  @Autowired private MeterProvider meterProvider;
  @Autowired private CorrelationContextManager correlationContextManager;

  @Test
  public void shouldConstructFullyConfiguredTracerRegistry() {
    assertThat(tracerProvider).isInstanceOf(TracerSdkProvider.class);
    TracerSdkProvider tracerSdkFactory = (TracerSdkProvider) tracerProvider;
    TraceConfig traceConfig = tracerSdkFactory.getActiveTraceConfig();
    assertThat(traceConfig.getMaxNumberOfAttributes()).isEqualTo(16);
    assertThat(traceConfig.getMaxNumberOfEvents()).isEqualTo(32);
  }

  @Test
  public void shouldConstructFullyConfiguredMeterRegistry() {
    assertThat(meterProvider).isInstanceOf(MeterSdkProvider.class);
  }

  @Test
  public void shouldConstructFullyConfiguredCorrelationContextManager() {
    assertThat(correlationContextManager).isInstanceOf(CorrelationContextManagerSdk.class);
  }
}
