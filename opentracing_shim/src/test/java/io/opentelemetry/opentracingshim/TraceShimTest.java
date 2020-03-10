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
import io.opentelemetry.sdk.correlationcontext.CorrelationContextManagerSdk;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TraceShimTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void createTracerShim_default() {
    TracerShim tracerShim = (TracerShim) TraceShim.createTracerShim();
    assertEquals(OpenTelemetry.getTracerProvider().get("opentracingshim"), tracerShim.tracer());
    assertEquals(OpenTelemetry.getCorrelationContextManager(), tracerShim.contextManager());
  }

  @Test
  public void createTracerShim_nullTracer() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("tracerProvider");
    TraceShim.createTracerShim(null, OpenTelemetry.getCorrelationContextManager());
  }

  @Test
  public void createTracerShim_nullContextManager() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("contextManager");
    TraceShim.createTracerShim(OpenTelemetry.getTracerProvider(), null);
  }

  @Test
  public void createTracerShim() {
    TracerSdkProvider sdk = TracerSdkProvider.builder().build();
    CorrelationContextManagerSdk contextManager = new CorrelationContextManagerSdk();
    TracerShim tracerShim = (TracerShim) TraceShim.createTracerShim(sdk, contextManager);
    assertEquals(sdk.get("opentracingshim"), tracerShim.tracer());
    assertEquals(contextManager, tracerShim.contextManager());
  }
}
