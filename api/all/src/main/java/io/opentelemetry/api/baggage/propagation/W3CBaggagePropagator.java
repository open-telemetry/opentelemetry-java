/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage.propagation;

import static java.util.Collections.singletonList;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.api.baggage.BaggageEntry;
import io.opentelemetry.api.internal.PercentEscaper;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * {@link TextMapPropagator} that implements the W3C specification for baggage header propagation.
 */
public final class W3CBaggagePropagator implements TextMapPropagator {

  // Limits from https://www.w3.org/TR/baggage/#limits
  private static final int MAX_BAGGAGE_ENTRIES = 64;
  private static final int MAX_BAGGAGE_BYTES = 8192;

  private static final String FIELD = "baggage";
  private static final List<String> FIELDS = singletonList(FIELD);
  private static final W3CBaggagePropagator INSTANCE = new W3CBaggagePropagator();
  private static final PercentEscaper URL_ESCAPER = PercentEscaper.create();
  private static final Logger LOGGER = Logger.getLogger(W3CBaggagePropagator.class.getName());

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
  public <C> void inject(Context context, @Nullable C carrier, TextMapSetter<C> setter) {
    if (context == null || setter == null) {
      return;
    }
    Baggage baggage = Baggage.fromContext(context);
    if (baggage.isEmpty()) {
      return;
    }
    String headerContent = baggageToString(baggage);

    if (!headerContent.isEmpty()) {
      setter.set(carrier, FIELD, headerContent);
    }
  }

  private static String baggageToString(Baggage baggage) {
    StringBuilder headerContent = new StringBuilder();
    int[] entryCount = {0};
    baggage.forEach(
        (key, baggageEntry) -> {
          if (baggageIsInvalid(key, baggageEntry)) {
            return;
          }
          if (entryCount[0] >= MAX_BAGGAGE_ENTRIES) {
            return;
          }
          String encodedValue = encodeValue(baggageEntry.getValue());
          String metadataValue = baggageEntry.getMetadata().getValue();
          String encodedMetadata =
              (metadataValue != null && !metadataValue.isEmpty())
                  ? encodeValue(metadataValue)
                  : null;
          // Exit early if adding this entry causes the total length to exceed the limit
          // encodedEntryLength includes a trailing comma; the final string trims exactly one,
          // so the net contribution to the final length is entryLength - 1.
          if (headerContent.length() + encodedEntryLength(key, encodedValue, encodedMetadata) - 1
              > MAX_BAGGAGE_BYTES) {
            return;
          }
          headerContent.append(key).append("=").append(encodedValue);
          if (encodedMetadata != null) {
            headerContent.append(";").append(encodedMetadata);
          }
          headerContent.append(",");
          entryCount[0]++;
        });

    if (headerContent.length() == 0) {
      return "";
    }

    // Trim trailing comma
    headerContent.setLength(headerContent.length() - 1);
    return headerContent.toString();
  }

  private static String encodeValue(String value) {
    return URL_ESCAPER.escape(value);
  }

  /**
   * Returns the length of the serialized entry as it would appear in the baggage header, including
   * the trailing comma used by the trailing-comma pattern in {@link #baggageToString}. The length
   * accounts for {@code "key=encodedValue,"} plus {@code ";encodedMetadata"} when metadata is
   * present.
   */
  private static int encodedEntryLength(
      String key, String encodedValue, @Nullable String encodedMetadata) {
    int length = key.length() + 1 + encodedValue.length() + 1; // "key=value,"
    if (encodedMetadata != null) {
      length += 1 + encodedMetadata.length(); // ";metadata"
    }
    return length;
  }

  @Override
  public <C> Context extract(Context context, @Nullable C carrier, TextMapGetter<C> getter) {
    if (context == null) {
      return Context.root();
    }
    if (getter == null) {
      return context;
    }

    return extractMulti(context, carrier, getter);
  }

  private static <C> Context extractMulti(
      Context context, @Nullable C carrier, TextMapGetter<C> getter) {
    Iterator<String> baggageHeaders = getter.getAll(carrier, FIELD);
    if (baggageHeaders == null) {
      return context;
    }

    boolean extracted = false;
    BaggageBuilder baggageBuilder = Baggage.builder();
    int totalBytes = 0;
    int totalEntries = 0;

    while (baggageHeaders.hasNext()) {
      String header = baggageHeaders.next();
      if (header.isEmpty()) {
        continue;
      }

      totalBytes += header.length();
      if (totalBytes > MAX_BAGGAGE_BYTES || totalEntries >= MAX_BAGGAGE_ENTRIES) {
        LOGGER.fine("Baggage header exceeded W3C limits, dropping remaining entries");
        break;
      }

      try {
        int added = extractEntries(header, baggageBuilder, MAX_BAGGAGE_ENTRIES - totalEntries);
        extracted = true;
        totalEntries += added;
      } catch (RuntimeException expected) {
        // invalid baggage header, continue
      }
    }

    return extracted ? context.with(baggageBuilder.build()) : context;
  }

  private static int extractEntries(
      String baggageHeader, BaggageBuilder baggageBuilder, int maxEntries) {
    return new Parser(baggageHeader, maxEntries).parseInto(baggageBuilder);
  }

  private static boolean baggageIsInvalid(String key, BaggageEntry baggageEntry) {
    return !isValidBaggageKey(key) || !isValidBaggageValue(baggageEntry.getValue());
  }

  /**
   * Determines whether the given {@code String} is a valid entry key.
   *
   * @param name the entry key name to be validated.
   * @return whether the name is valid.
   */
  private static boolean isValidBaggageKey(String name) {
    return name != null && !name.trim().isEmpty() && StringUtils.isPrintableString(name);
  }

  /**
   * Determines whether the given {@code String} is a valid entry value.
   *
   * @param value the entry value to be validated.
   * @return whether the value is valid.
   */
  private static boolean isValidBaggageValue(String value) {
    return value != null;
  }

  @Override
  public String toString() {
    return "W3CBaggagePropagator";
  }
}
