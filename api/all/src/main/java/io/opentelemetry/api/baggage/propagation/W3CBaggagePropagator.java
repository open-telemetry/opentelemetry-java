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
import java.util.List;
import javax.annotation.Nullable;

/**
 * {@link TextMapPropagator} that implements the W3C specification for baggage header propagation.
 */
public final class W3CBaggagePropagator implements TextMapPropagator {

  private static final String FIELD = "baggage";
  private static final List<String> FIELDS = singletonList(FIELD);
  private static final W3CBaggagePropagator INSTANCE = new W3CBaggagePropagator();
  private static final PercentEscaper URL_ESCAPER = PercentEscaper.create();

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
    baggage.forEach(
        (key, baggageEntry) -> {
          if (baggageIsInvalid(key, baggageEntry)) {
            return;
          }
          headerContent.append(key).append("=").append(encodeValue(baggageEntry.getValue()));
          String metadataValue = baggageEntry.getMetadata().getValue();
          if (metadataValue != null && !metadataValue.isEmpty()) {
            headerContent.append(";").append(encodeValue(metadataValue));
          }
          headerContent.append(",");
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
   * @param context the {@code Context} used to store the extracted value.
   * @param carrier holds propagation fields. For example, an outgoing message or http request.
   * @param getter invoked for each propagation key to get data from the carrier.
   * @return the extracted context
   */
  @Override
  public <C> Context extract(Context context, @Nullable C carrier, TextMapGetter<C> getter) {
    if (context == null) {
      return Context.root();
    }
    if (getter == null) {
      return context;
    }

    List<String> baggageHeaders = getter.getList(carrier, FIELD);
    if (baggageHeaders == null || baggageHeaders.isEmpty()) {
      return context;
    }

    boolean extracted = false;
    BaggageBuilder baggageBuilder = Baggage.builder();
    for (String header : baggageHeaders) {
      if (header.isEmpty()) {
        continue;
      }

      try {
        extractEntries(header, baggageBuilder);
        extracted = true;
      } catch (RuntimeException expected) {
        // invalid baggage header, continue
      }
    }
    return extracted ? context.with(baggageBuilder.build()) : context;
  }

  private static void extractEntries(String baggageHeader, BaggageBuilder baggageBuilder) {
    new Parser(baggageHeader).parseInto(baggageBuilder);
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
