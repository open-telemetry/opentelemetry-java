/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage.propagation;

import static java.util.Collections.singletonList;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.api.baggage.BaggageEntryMetadata;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

/**
 * {@link TextMapPropagator} that implements the W3C specification for baggage header propagation.
 */
public final class W3CBaggagePropagator implements TextMapPropagator {

  private static final String FIELD = "baggage";
  private static final List<String> FIELDS = singletonList(FIELD);
  private static final W3CBaggagePropagator INSTANCE = new W3CBaggagePropagator();

  /** Singleton instance of the W3C Baggage Propagator. */
  public static W3CBaggagePropagator getInstance() {
    return INSTANCE;
  }

  private W3CBaggagePropagator() {}

  @Override
  public Collection<String> fields() {
    return FIELDS;
  }

  @Override
  public <C> void inject(Context context, C carrier, TextMapSetter<C> setter) {
    if (context == null || setter == null) {
      return;
    }
    Baggage baggage = Baggage.fromContext(context);
    if (baggage.isEmpty()) {
      return;
    }
    StringBuilder headerContent = new StringBuilder();
    baggage.forEach(
        (key, baggageEntry) -> {
          headerContent.append(key).append("=").append(baggageEntry.getValue());
          String metadataValue = baggageEntry.getMetadata().getValue();
          if (metadataValue != null && !metadataValue.isEmpty()) {
            headerContent.append(";").append(metadataValue);
          }
          headerContent.append(",");
        });
    if (headerContent.length() > 0) {
      headerContent.setLength(headerContent.length() - 1);
      setter.set(carrier, FIELD, headerContent.toString());
    }
  }

  @Override
  public <C> Context extract(Context context, @Nullable C carrier, TextMapGetter<C> getter) {
    if (context == null) {
      return Context.root();
    }
    if (getter == null) {
      return context;
    }

    String baggageHeader = getter.get(carrier, FIELD);
    if (baggageHeader == null) {
      return context;
    }
    if (baggageHeader.isEmpty()) {
      return context;
    }

    BaggageBuilder baggageBuilder = Baggage.builder();
    try {
      extractEntries(baggageHeader, baggageBuilder);
    } catch (RuntimeException e) {
      return context;
    }
    return context.with(baggageBuilder.build());
  }

  private static void extractEntries(String baggageHeader, BaggageBuilder baggageBuilder) {
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
        baggageBuilder.put(key, value, BaggageEntryMetadata.create(metadata.trim()));
      }
    }
  }
}
