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

package io.opentelemetry.sdk.contrib.trace.testbed.suspendresumepropagation;

import static com.google.common.truth.Truth.assertThat;
import static io.opentelemetry.sdk.contrib.trace.testbed.TestUtils.createTracerShim;

import io.opentelemetry.sdk.trace.export.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SpanData;
import io.opentelemetry.trace.Tracer;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * These tests are intended to simulate the kind of async models that are common in java async
 * frameworks.
 */
public class SuspendResumePropagationTest {
  private final InMemorySpanExporter exporter = InMemorySpanExporter.create();
  private final Tracer tracer = createTracerShim(exporter);

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

    List<SpanData> finished = exporter.getFinishedSpanItems();
    assertThat(finished.size()).isEqualTo(2);

    assertThat(finished.get(0).getName()).isEqualTo("job 1");
    assertThat(finished.get(1).getName()).isEqualTo("job 2");

    assertThat(finished.get(0).getParentSpanId().isValid()).isFalse();
    assertThat(finished.get(1).getParentSpanId().isValid()).isFalse();
  }
}
