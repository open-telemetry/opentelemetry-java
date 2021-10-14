/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage.propagation;

import static org.assertj.core.api.Assertions.assertThat;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.generator.java.lang.AbstractStringGenerator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import edu.berkeley.cs.jqf.fuzz.random.NoGuidance;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageEntryMetadata;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

@SuppressWarnings("SystemOut")
class W3CBaggagePropagatorFuzzTest {

  @RunWith(JQF.class)
  public static class TestCases {
    private final W3CBaggagePropagator baggagePropagator = W3CBaggagePropagator.getInstance();

    @Fuzz
    public void roundTripRandomValues(String baggageValue, String metadataBlob) {
      Baggage baggage =
          Baggage.builder()
              .put("b", baggageValue, BaggageEntryMetadata.create(metadataBlob))
              .build();
      Map<String, String> carrier = new HashMap<>();
      baggagePropagator.inject(Context.root().with(baggage), carrier, Map::put);
      Context extractedContext =
          baggagePropagator.extract(Context.root(), carrier, new MapTextMapGetter());
      Baggage extractedBaggage = Baggage.fromContext(extractedContext);
      assertThat(extractedBaggage).isEqualTo(baggage);
    }

    /** Test ascii values, to favor values with baggage delimiters in them. */
    @Fuzz
    public void roundTripAsciiValues(
        @From(AsciiGenerator.class) String baggageValue,
        @From(AsciiGenerator.class) String metadataBlob) {
      Baggage baggage =
          Baggage.builder()
              .put("b", baggageValue, BaggageEntryMetadata.create(metadataBlob))
              .build();
      Map<String, String> carrier = new HashMap<>();
      baggagePropagator.inject(Context.root().with(baggage), carrier, Map::put);
      Context extractedContext =
          baggagePropagator.extract(Context.root(), carrier, new MapTextMapGetter());
      Baggage extractedBaggage = Baggage.fromContext(extractedContext);
      assertThat(extractedBaggage).isEqualTo(baggage);
    }
  }

  // driver methods to avoid having to use the vintage junit engine, and to enable increasing the
  // number of iterations:

  @Test
  void roundTripAsciiFuzzing() {
    Result result = runTestCase("roundTripAsciiValues");
    assertThat(result.wasSuccessful()).isTrue();
  }

  @Test
  void roundTripFuzzing() {
    Result result = runTestCase("roundTripRandomValues");
    assertThat(result.wasSuccessful()).isTrue();
  }

  private static Result runTestCase(String roundTripRandomValues) {
    return GuidedFuzzing.run(
        TestCases.class, roundTripRandomValues, new NoGuidance(10000, System.out), System.out);
  }

  public static class AsciiGenerator extends AbstractStringGenerator {

    @Override
    protected int nextCodePoint(SourceOfRandomness random) {
      return random.nextChar(' ', '~');
    }

    @Override
    protected boolean codePointInRange(int codePoint) {
      return codePoint >= ' ' && codePoint <= '~';
    }
  }

  private static class MapTextMapGetter implements TextMapGetter<Map<String, String>> {
    @Override
    public Iterable<String> keys(Map<String, String> carrier) {
      return carrier.keySet();
    }

    @Nullable
    @Override
    public String get(Map<String, String> carrier, String key) {
      return carrier.get(key);
    }
  }
}
