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

package io.opentelemetry.sdk.trace;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link NoopSpanProcessorTest}. */
@RunWith(JUnit4.class)
public class NoopSpanProcessorTest {
  @Mock private ReadableSpan readableSpan;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void noCrash() {
    SpanProcessor noopSpanProcessor = NoopSpanProcessor.getInstance();
    noopSpanProcessor.onStart(readableSpan);
    assertThat(noopSpanProcessor.isStartRequired()).isFalse();
    noopSpanProcessor.onEnd(readableSpan);
    assertThat(noopSpanProcessor.isEndRequired()).isFalse();
    noopSpanProcessor.forceFlush();
    noopSpanProcessor.shutdown();
  }
}
