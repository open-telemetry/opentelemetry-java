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

import io.opentelemetry.distributedcontext.DistributedContextManager;
import io.opentelemetry.metrics.MeterRegistry;
import io.opentelemetry.sdk.distributedcontext.DistributedContextManagerSdk;
import io.opentelemetry.sdk.metrics.MeterSdkRegistry;
import io.opentelemetry.sdk.trace.TracerSdkRegistry;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.TracerRegistry;
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

  @Autowired private TracerRegistry tracerRegistry;
  @Autowired private MeterRegistry meterRegistry;
  @Autowired private DistributedContextManager distributedContextManager;

  @Test
  public void shouldConstructFullyConfiguredTracerRegistry() {
    assertThat(tracerRegistry).isInstanceOf(TracerSdkRegistry.class);
    TracerSdkRegistry tracerSdkFactory = (TracerSdkRegistry) tracerRegistry;
    TraceConfig traceConfig = tracerSdkFactory.getActiveTraceConfig();
    assertThat(traceConfig.getMaxNumberOfAttributes()).isEqualTo(16);
    assertThat(traceConfig.getMaxNumberOfEvents()).isEqualTo(32);
  }

  @Test
  public void shouldConstructFullyConfiguredMeterRegistry() {
    assertThat(meterRegistry).isInstanceOf(MeterSdkRegistry.class);
  }

  @Test
  public void shouldConstructFullyConfiguredDistributedContextManager() {
    assertThat(distributedContextManager).isInstanceOf(DistributedContextManagerSdk.class);
  }
}
