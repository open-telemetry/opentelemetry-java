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

package io.opentelemetry.opentracingshim.testbed.suspendresumepropagation;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.inmemorytrace.InMemoryTracer;
import io.opentelemetry.opentracingshim.TraceShim;
import io.opentelemetry.trace.SpanData;
import io.opentracing.Tracer;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * These tests are intended to simulate the kind of async models that are common in java async
 * frameworks.
 *
 * @author tylerbenson
 */
public class SuspendResumePropagationTest {
  private final InMemoryTracer mockTracer = new InMemoryTracer();
  private final Tracer tracer = TraceShim.createTracerShim(mockTracer);

  @Before
  public void before() {}

  @Test
  public void testContinuationInterleaving() {
    SuspendResume job1 = new SuspendResume(1, tracer);
    SuspendResume job2 = new SuspendResume(2, tracer);

    // Pretend that the framework is controlling actual execution here.
    job1.doPart("some work for 1");
    job2.doPart("some work for 2");
    job1.doPart("other work for 1");
    job2.doPart("other work for 2");
    job2.doPart("more work for 2");
    job1.doPart("more work for 1");

    job1.done();
    job2.done();

    List<SpanData> finished = mockTracer.getFinishedSpanDataItems();
    assertThat(finished.size()).isEqualTo(2);

    assertThat(finished.get(0).getName()).isEqualTo("job 1");
    assertThat(finished.get(1).getName()).isEqualTo("job 2");

    assertThat(finished.get(0).getParentSpanId()).isNull();
    assertThat(finished.get(1).getParentSpanId()).isNull();
  }
}
