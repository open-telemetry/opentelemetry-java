/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage.propagation;

import static org.assertj.core.api.Assertions.assertThat;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import edu.berkeley.cs.jqf.fuzz.random.NoGuidance;
import io.opentelemetry.api.internal.PercentEscaper;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

class PercentEscaperFuzzTest {
  @RunWith(JQF.class)
  public static class TestCases {
    private final PercentEscaper percentEscaper = new PercentEscaper();

    @SuppressWarnings("JdkObsolete") // Recommended alternative was introduced in java 10
    @Fuzz
    public void roundTripWithUrlDecoder(String value) throws Exception {
      String escaped = percentEscaper.escape(value);
      String decoded = URLDecoder.decode(escaped, StandardCharsets.UTF_8.name());
      assertThat(decoded).isEqualTo(value);
    }
  }

  // driver methods to avoid having to use the vintage junit engine, and to enable increasing the
  // number of iterations:

  @Test
  @SuppressWarnings("SystemOut")
  public void lotsOfFuzz() {
    Result result =
        GuidedFuzzing.run(
            TestCases.class,
            "roundTripWithUrlDecoder",
            new NoGuidance(10000, System.out),
            System.out);
    assertThat(result.wasSuccessful()).isTrue();
  }
}
