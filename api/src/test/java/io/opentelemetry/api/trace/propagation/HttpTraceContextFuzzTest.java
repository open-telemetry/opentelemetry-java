/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace.propagation;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import io.opentelemetry.context.Context;
import java.util.Map;
import org.junit.runner.RunWith;

@RunWith(JQF.class)
@SuppressWarnings("JavadocMethod")
public class HttpTraceContextFuzzTest {
  private final HttpTraceContext httpTraceContext = HttpTraceContext.getInstance();

  @Fuzz
  public void safeForRandomInputs(String traceParentHeader, String traceStateHeader) {
    Context context =
        httpTraceContext.extract(
            Context.root(),
            ImmutableMap.of("traceparent", traceParentHeader, "tracestate", traceStateHeader),
            Map::get);
    assertThat(context).isNotNull();
  }
}
