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

package io.opentelemetry.opentracingshim;

import static org.junit.Assert.assertEquals;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.sdk.distributedcontext.CorrelationContextManagerSdk;
import io.opentelemetry.sdk.trace.TracerSdk;
import org.junit.Test;

public class TraceShimTest {

  @Test
  public void createTracerShim_default() {
    TracerShim tracerShim = (TracerShim) TraceShim.createTracerShim();
    assertEquals(OpenTelemetry.getTracerFactory().get("opentracingshim"), tracerShim.tracer());
    assertEquals(OpenTelemetry.getDistributedContextManager(), tracerShim.contextManager());
  }

  @Test(expected = NullPointerException.class)
  public void createTracerShim_nullTracer() {
    TraceShim.createTracerShim(null, OpenTelemetry.getDistributedContextManager());
  }

  @Test(expected = NullPointerException.class)
  public void createTracerShim_nullContextManager() {
    TraceShim.createTracerShim(OpenTelemetry.getTracerFactory().get("opentracingshim"), null);
  }

  @Test
  public void createTracerShim() {
    TracerSdk tracer = new TracerSdk();
    CorrelationContextManagerSdk contextManager = new CorrelationContextManagerSdk();
    TracerShim tracerShim = (TracerShim) TraceShim.createTracerShim(tracer, contextManager);
    assertEquals(tracer, tracerShim.tracer());
    assertEquals(contextManager, tracerShim.contextManager());
  }
}
