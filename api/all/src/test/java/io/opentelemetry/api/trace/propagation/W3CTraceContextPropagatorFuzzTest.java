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
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.runner.RunWith;

@RunWith(JQF.class)
@SuppressWarnings("JavadocMethod")
public class W3CTraceContextPropagatorFuzzTest {
  private final TextMapPropagator w3cTraceContextPropagator =
      W3CTraceContextPropagator.getInstance();

  @Fuzz
  public void safeForRandomInputs(String traceParentHeader, String traceStateHeader) {
    Context context =
        w3cTraceContextPropagator.extract(
            Context.groot(),
            ImmutableMap.of("traceparent", traceParentHeader, "tracestate", traceStateHeader),
            new TextMapGetter<Map<String, String>>() {
              @Override
              public Iterable<String> keys(Map<String, String> carrier) {
                return carrier.keySet();
              }

              @Nullable
              @Override
              public String get(Map<String, String> carrier, String key) {
                return carrier.get(key);
              }
            });

    assertThat(context).isNotNull();
  }
}
