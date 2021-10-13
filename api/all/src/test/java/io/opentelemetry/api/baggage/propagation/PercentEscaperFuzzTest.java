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
import java.net.URLDecoder;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

@RunWith(JQF.class)
public class PercentEscaperFuzzTest {
  private final PercentEscaper percentEscaper = new PercentEscaper();

  @Fuzz
  public void roundTripWithUrlDecoder(String value) throws Exception {
    String escaped = percentEscaper.escape(value);
    String decoded = URLDecoder.decode(escaped, "UTF-8");
    assertThat(decoded).isEqualTo(value);
  }

  /**
   * There doesn't seem to be any way to make the fuzzing run more than the default 100 times
   * without using custom guidance, but this does seem to do it.
   */
  @Test
  @SuppressWarnings("SystemOut")
  public void lotsOfFuzz() {
    Result result =
        GuidedFuzzing.run(
            PercentEscaperFuzzTest.class,
            "roundTripWithUrlDecoder",
            new NoGuidance(10000, System.out),
            System.out);
    assertThat(result.wasSuccessful()).isTrue();
  }
}
