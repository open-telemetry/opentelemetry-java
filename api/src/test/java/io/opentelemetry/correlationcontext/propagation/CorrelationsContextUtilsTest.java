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

package io.opentelemetry.correlationcontext.propagation;

import static com.google.common.truth.Truth.assertThat;

import io.grpc.Context;
import io.opentelemetry.correlationcontext.CorrelationContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link CorrelationsContextUtils}. */
@RunWith(JUnit4.class)
public final class CorrelationsContextUtilsTest {
  @Test
  public void testGetCurrentCorrelationContext_DefaultContext() {
    CorrelationContext corrContext =
        CorrelationsContextUtils.getCorrelationContext(Context.current());
    assertThat(corrContext).isNull();
  }

  @Test
  public void testGetCurrentCorrelationContex_DefaultContext_WithoutExplicitContext() {
    CorrelationContext corrContext = CorrelationsContextUtils.getCorrelationContext();
    assertThat(corrContext).isNull();
  }

  @Test
  public void testGetCurrentCorrelationContextWithDefault_DefaultContext() {
    CorrelationContext corrContext =
        CorrelationsContextUtils.getCorrelationContextWithDefault(Context.current());
    assertThat(corrContext).isNotNull();
    assertThat(corrContext.getEntries()).isEmpty();
  }

  @Test
  public void testGetCurrentCorrelationContextWithDefault_ContextSetToNull() {
    Context orig =
        CorrelationsContextUtils.withCorrelationContext(null, Context.current()).attach();
    try {
      CorrelationContext corrContext =
          CorrelationsContextUtils.getCorrelationContextWithDefault(Context.current());
      assertThat(corrContext).isNotNull();
      assertThat(corrContext.getEntries()).isEmpty();
    } finally {
      Context.current().detach(orig);
    }
  }
}
