/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.correlationcontext;

import static org.assertj.core.api.Assertions.assertThat;

import io.grpc.Context;
import org.junit.jupiter.api.Test;

class CorrelationsContextUtilsTest {

  @Test
  void testGetCurrentCorrelationContext_Default() {
    CorrelationContext corrContext = CorrelationsContextUtils.getCurrentCorrelationContext();
    assertThat(corrContext).isSameAs(EmptyCorrelationContext.getInstance());
  }

  @Test
  void testGetCurrentCorrelationContext_SetCorrContext() {
    CorrelationContext corrContext =
        DefaultCorrelationContextManager.getInstance().contextBuilder().build();
    Context orig =
        CorrelationsContextUtils.withCorrelationContext(corrContext, Context.current()).attach();
    try {
      assertThat(CorrelationsContextUtils.getCurrentCorrelationContext()).isSameAs(corrContext);
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  void testGetCorrelationContext_DefaultContext() {
    CorrelationContext corrContext =
        CorrelationsContextUtils.getCorrelationContext(Context.current());
    assertThat(corrContext).isSameAs(EmptyCorrelationContext.getInstance());
  }

  @Test
  void testGetCorrelationContext_ExplicitContext() {
    CorrelationContext corrContext =
        DefaultCorrelationContextManager.getInstance().contextBuilder().build();
    Context context =
        CorrelationsContextUtils.withCorrelationContext(corrContext, Context.current());
    assertThat(CorrelationsContextUtils.getCorrelationContext(context)).isSameAs(corrContext);
  }

  @Test
  void testGetCorrelationContextWithoutDefault_DefaultContext() {
    CorrelationContext corrContext =
        CorrelationsContextUtils.getCorrelationContextWithoutDefault(Context.current());
    assertThat(corrContext).isNull();
  }

  @Test
  void testGetCorrelationContextWithoutDefault_ExplicitContext() {
    CorrelationContext corrContext =
        DefaultCorrelationContextManager.getInstance().contextBuilder().build();
    Context context =
        CorrelationsContextUtils.withCorrelationContext(corrContext, Context.current());
    assertThat(CorrelationsContextUtils.getCorrelationContext(context)).isSameAs(corrContext);
  }
}
