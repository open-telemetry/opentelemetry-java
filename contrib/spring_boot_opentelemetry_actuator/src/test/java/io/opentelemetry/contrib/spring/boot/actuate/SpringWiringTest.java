/*
 * Copyright 2019, OpenTelemetry Authors
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.opentelemetry.sdk.trace.TracerSdkFactory;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.TracerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/** Testing of Spring wiring. */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {OpenTelemetryConfiguration.class})
@TestPropertySource(locations = {"classpath:application.properties"})
public class SpringWiringTest {

  @Autowired private TracerFactory tracerFactory;

  @Test
  public void shouldConstructFullyConfiguredTracerFactory() {
    assertTrue(tracerFactory instanceof TracerSdkFactory);
    TracerSdkFactory tracerSdkFactory = (TracerSdkFactory) tracerFactory;
    TraceConfig traceConfig = tracerSdkFactory.getActiveTraceConfig();
    assertEquals(16, traceConfig.getMaxNumberOfAttributes());
    assertEquals(32, traceConfig.getMaxNumberOfEvents());
  }
}
