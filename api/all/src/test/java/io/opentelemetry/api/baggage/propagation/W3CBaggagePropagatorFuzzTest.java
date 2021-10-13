/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage.propagation;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.generator.java.lang.AbstractStringGenerator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageEntryMetadata;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.runner.RunWith;

@RunWith(JQF.class)
@SuppressWarnings("JavadocMethod")
public class W3CBaggagePropagatorFuzzTest {

  private final W3CBaggagePropagator baggagePropagator = W3CBaggagePropagator.getInstance();

  @Fuzz
  public void safeForRandomInputs(String baggage) {
    Context context =
        baggagePropagator.extract(
            Context.root(), ImmutableMap.of("baggage", baggage), new MapTextMapGetter());
    assertThat(context).isNotNull();
  }

  @Fuzz
  public void roundTripRandomValues(String baggageValue, String metadataBlob) {
    Baggage baggage =
        Baggage.builder().put("b", baggageValue, BaggageEntryMetadata.create(metadataBlob)).build();
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
        Baggage.builder().put("b", baggageValue, BaggageEntryMetadata.create(metadataBlob)).build();
    Map<String, String> carrier = new HashMap<>();
    baggagePropagator.inject(Context.root().with(baggage), carrier, Map::put);
    Context extractedContext =
        baggagePropagator.extract(Context.root(), carrier, new MapTextMapGetter());
    Baggage extractedBaggage = Baggage.fromContext(extractedContext);
    assertThat(extractedBaggage).isEqualTo(baggage);
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
