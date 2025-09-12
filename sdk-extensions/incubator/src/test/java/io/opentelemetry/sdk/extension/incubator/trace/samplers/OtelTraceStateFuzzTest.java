/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import static org.assertj.core.api.Assertions.assertThat;

import com.pholser.junit.quickcheck.generator.InRange;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import edu.berkeley.cs.jqf.fuzz.random.NoGuidance;
import io.opentelemetry.api.trace.TraceState;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

@SuppressWarnings("SystemOut")
class OtelTraceStateFuzzTest {

  @RunWith(JQF.class)
  public static class TestCases {
    @Fuzz
    public void roundTripRandomValues(long rv, long th) {
      OtelTraceState input = new OtelTraceState(rv, th, Collections.emptyList());
      OtelTraceState output =
          OtelTraceState.parse(TraceState.builder().put("ot", input.serialize()).build());
      assertState(output, rv, th);
    }

    @Fuzz
    public void roundTripValidValues(
        @InRange(minLong = 0, maxLong = ImmutableSamplingIntent.MAX_RANDOM_VALUE) long rv,
        @InRange(minLong = 0, maxLong = ImmutableSamplingIntent.MAX_THRESHOLD) long th) {
      OtelTraceState input = new OtelTraceState(rv, th, Collections.emptyList());
      OtelTraceState output =
          OtelTraceState.parse(TraceState.builder().put("ot", input.serialize()).build());
      assertState(output, rv, th);
    }

    private static void assertState(OtelTraceState state, long rv, long th) {
      boolean hasRv = false;
      boolean hasTh = false;
      if (rv >= 0 && rv <= ImmutableSamplingIntent.MAX_RANDOM_VALUE) {
        assertThat(state.getRandomValue()).isEqualTo(rv);
        hasRv = true;
      } else {
        assertThat(state.getRandomValue()).isEqualTo(ImmutableSamplingIntent.INVALID_RANDOM_VALUE);
      }
      if (th >= 0 && th <= ImmutableSamplingIntent.MAX_THRESHOLD) {
        assertThat(state.getThreshold()).isEqualTo(th);
        hasTh = state.getThreshold() != ImmutableSamplingIntent.MAX_THRESHOLD;
      } else {
        assertThat(state.getThreshold()).isEqualTo(ImmutableSamplingIntent.INVALID_THRESHOLD);
      }
      String[] parts = state.serialize().split(";");
      String thStr = null;
      String rvStr = null;
      if (hasRv && hasTh) {
        assertThat(parts).hasSize(2);
        thStr = parts[0];
        rvStr = parts[1];
      } else if (hasRv) {
        assertThat(parts).hasSize(1);
        rvStr = parts[0];
      } else if (hasTh) {
        assertThat(parts).hasSize(1);
        thStr = parts[0];
      }

      if (hasRv) {
        assertThat(rvStr).startsWith("rv:");
        rvStr = rvStr.substring("rv:".length());
        assertThat(rvStr).hasSize(OtelTraceState.MAX_VALUE_LENGTH);
        assertThat(rvStr).isHexadecimal();
      }

      if (hasTh) {
        assertThat(thStr).startsWith("th:");
        thStr = thStr.substring("th:".length());
        assertThat(thStr).hasSizeBetween(1, OtelTraceState.MAX_VALUE_LENGTH);
        assertThat(thStr).isHexadecimal();
        if (th == 0) {
          assertThat(thStr).isEqualTo("0");
        } else {
          // No trailing zeros
          assertThat(thStr).doesNotMatch("[^0]0+$");
        }
      }
    }
  }

  // driver methods to avoid having to use the vintage junit engine, and to enable increasing the
  // number of iterations:

  @Test
  void roundTripFuzzing() {
    Result result = runTestCase("roundTripRandomValues");
    assertThat(result.wasSuccessful()).isTrue();
  }

  @Test
  void roundTripValidValues() {
    Result result = runTestCase("roundTripValidValues");
    assertThat(result.wasSuccessful()).isTrue();
  }

  private static Result runTestCase(String testCaseName) {
    return GuidedFuzzing.run(
        TestCases.class, testCaseName, new NoGuidance(10000, System.out), System.out);
  }
}
