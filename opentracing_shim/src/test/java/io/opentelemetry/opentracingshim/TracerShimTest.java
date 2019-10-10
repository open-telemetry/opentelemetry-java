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

import static org.junit.Assert.assertNull;

import io.opentelemetry.distributedcontext.DefaultDistributedContextManager;
import io.opentelemetry.trace.DefaultTracer;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import java.util.Collections;
import org.junit.Test;

public class TracerShimTest {

  @Test
  public void extract_nullContext() {
    TracerShim testClass =
        new TracerShim(
            new TelemetryInfo(DefaultTracer.getInstance(), new DefaultDistributedContextManager()));

    SpanContext result =
        testClass.extract(
            Format.Builtin.TEXT_MAP, new TextMapAdapter(Collections.<String, String>emptyMap()));
    assertNull(result);
  }
}
