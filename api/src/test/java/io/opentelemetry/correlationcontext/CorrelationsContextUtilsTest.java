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

package io.opentelemetry.correlationcontext;

import static com.google.common.truth.Truth.assertThat;

import io.grpc.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link CorrelationsContextUtils}. */
@RunWith(JUnit4.class)
public final class CorrelationsContextUtilsTest {

  @Test
  public void testGetCurrentCorrelationContext_Default() {
    CorrelationContext corrContext = CorrelationsContextUtils.getCurrentCorrelationContext();
    assertThat(corrContext).isSameInstanceAs(EmptyCorrelationContext.getInstance());
  }

  @Test
  public void testGetCurrentCorrelationContext_SetCorrContext() {
    CorrelationContext corrContext =
        DefaultCorrelationContextManager.getInstance().contextBuilder().build();
    Context orig =
        CorrelationsContextUtils.withCorrelationContext(corrContext, Context.current()).attach();
    try {
      assertThat(CorrelationsContextUtils.getCurrentCorrelationContext())
          .isSameInstanceAs(corrContext);
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  public void testGetCorrelationContext_DefaultContext() {
    CorrelationContext corrContext =
        CorrelationsContextUtils.getCorrelationContext(Context.current());
    assertThat(corrContext).isSameInstanceAs(EmptyCorrelationContext.getInstance());
  }

  @Test
  public void testGetCorrelationContext_ExplicitContext() {
    CorrelationContext corrContext =
        DefaultCorrelationContextManager.getInstance().contextBuilder().build();
    Context context =
        CorrelationsContextUtils.withCorrelationContext(corrContext, Context.current());
    assertThat(CorrelationsContextUtils.getCorrelationContext(context))
        .isSameInstanceAs(corrContext);
  }

  @Test
  public void testGetCorrelationContextWithoutDefault_DefaultContext() {
    CorrelationContext corrContext =
        CorrelationsContextUtils.getCorrelationContextWithoutDefault(Context.current());
    assertThat(corrContext).isNull();
  }

  @Test
  public void testGetCorrelationContextWithoutDefault_ExplicitContext() {
    CorrelationContext corrContext =
        DefaultCorrelationContextManager.getInstance().contextBuilder().build();
    Context context =
        CorrelationsContextUtils.withCorrelationContext(corrContext, Context.current());
    assertThat(CorrelationsContextUtils.getCorrelationContext(context))
        .isSameInstanceAs(corrContext);
  }
}
