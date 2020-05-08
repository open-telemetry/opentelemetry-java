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

package io.opentelemetry.sdk.correlationcontext;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.correlationcontext.CorrelationContext;
import io.opentelemetry.correlationcontext.EmptyCorrelationContext;
import io.opentelemetry.currentcontext.CurrentContext;
import io.opentelemetry.currentcontext.Scope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link CorrelationContextManagerSdk}. */
@RunWith(JUnit4.class)
// Need to suppress warnings for MustBeClosed because Android 14 does not support
// try-with-resources.
@SuppressWarnings("MustBeClosedChecker")
public class CorrelationContextManagerSdkTest {
  @Mock private CorrelationContext distContext;
  private final CorrelationContextManagerSdk contextManager = new CorrelationContextManagerSdk();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testGetCurrentContext_DefaultContext() {
    assertThat(contextManager.getCurrentContext())
        .isSameInstanceAs(EmptyCorrelationContext.getInstance());
  }

  @Test
  public void testGetCurrentContext_ContextSetToNull() {
    try (Scope ignored = CurrentContext.withCorrelationContext(null)) {
      CorrelationContext distContext = contextManager.getCurrentContext();
      assertThat(distContext).isNotNull();
      assertThat(distContext.getEntries()).isEmpty();
    }
  }

  @Test
  public void testWithCorrelationContext() {
    assertThat(contextManager.getCurrentContext())
        .isSameInstanceAs(EmptyCorrelationContext.getInstance());
    try (Scope ignored = contextManager.withContext(distContext)) {
      assertThat(contextManager.getCurrentContext()).isSameInstanceAs(distContext);
    }
    assertThat(contextManager.getCurrentContext())
        .isSameInstanceAs(EmptyCorrelationContext.getInstance());
  }
}
