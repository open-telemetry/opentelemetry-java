/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.baggage.propagation;

import static java.util.Collections.singletonList;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.baggage.Baggage;
import io.opentelemetry.baggage.BaggageManager;
import io.opentelemetry.baggage.BaggageUtils;
import io.opentelemetry.baggage.EmptyBaggage;
import io.opentelemetry.baggage.Entry;
import io.opentelemetry.baggage.EntryMetadata;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.util.List;

/**
 * {@link TextMapPropagator} that implements the W3C specification for baggage header propagation.
 */
public class W3CBaggagePropagator implements TextMapPropagator {

  private static final String FIELD = "baggage";
  private static final List<String> FIELDS = singletonList(FIELD);
  private static final W3CBaggagePropagator INSTANCE =
      new W3CBaggagePropagator(OpenTelemetry.getBaggageManager());

  private final BaggageManager baggageManager;

  // visible for testing
  W3CBaggagePropagator(BaggageManager baggageManager) {
    this.baggageManager = baggageManager;
  }

  /**
   * Singleton instance of the W3C Baggage Propagator. Uses the {@link BaggageManager} from the
   * {@link OpenTelemetry} global.
   */
  public static W3CBaggagePropagator getInstance() {
    return INSTANCE;
  }

  @Override
  public List<String> fields() {
    return FIELDS;
  }

  @Override
  public <C> void inject(Context context, C carrier, Setter<C> setter) {
    Baggage baggage = BaggageUtils.getBaggage(context);
    if (baggage == null) {
      return;
    }
    StringBuilder headerContent = new StringBuilder();
    for (Entry entry : baggage.getEntries()) {
      headerContent.append(entry.getKey()).append("=").append(entry.getValue());
      String metadataValue = entry.getEntryMetadata().getValue();
      if (metadataValue != null && !metadataValue.isEmpty()) {
        headerContent.append(";").append(metadataValue);
      }
      headerContent.append(",");
    }
    if (headerContent.length() > 0) {
      headerContent.setLength(headerContent.length() - 1);
      setter.set(carrier, FIELD, headerContent.toString());
    }
  }

  @Override
  public <C> Context extract(Context context, C carrier, Getter<C> getter) {
    String baggageHeader = getter.get(carrier, FIELD);
    if (baggageHeader == null) {
      return context;
    }
    if (baggageHeader.isEmpty()) {
      return BaggageUtils.withBaggage(EmptyBaggage.getInstance(), context);
    }

    Baggage.Builder baggageBuilder = baggageManager.baggageBuilder();
    try {
      extractEntries(baggageHeader, baggageBuilder);
    } catch (Exception e) {
      return BaggageUtils.withBaggage(EmptyBaggage.getInstance(), context);
    }
    return BaggageUtils.withBaggage(baggageBuilder.build(), context);
  }

  @SuppressWarnings("StringSplitter")
  private static void extractEntries(String baggageHeader, Baggage.Builder baggageBuilder) {
    // todo: optimize this implementation; it can probably done with a single pass through the
    // string.
    String[] entries = baggageHeader.split(",");
    for (String entry : entries) {
      String metadata = "";
      int beginningOfMetadata = entry.indexOf(";");
      if (beginningOfMetadata > 0) {
        metadata = entry.substring(beginningOfMetadata + 1);
        entry = entry.substring(0, beginningOfMetadata);
      }
      String[] keyAndValue = entry.split("=");
      for (int i = 0; i < keyAndValue.length; i += 2) {
        String key = keyAndValue[i].trim();
        String value = keyAndValue[i + 1].trim();
        baggageBuilder.put(key, value, EntryMetadata.create(metadata.trim()));
      }
    }
  }
}
