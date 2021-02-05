/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.api.baggage.BaggageEntryMetadata;
import io.opentelemetry.context.Context;
import io.opentracing.SpanContext;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class SpanContextShim extends BaseShimObject implements SpanContext {

  private final io.opentelemetry.api.trace.SpanContext context;
  private final Baggage baggage;

  public SpanContextShim(SpanShim spanShim) {
    this(
        spanShim.telemetryInfo(),
        spanShim.getSpan().getSpanContext(),
        spanShim.telemetryInfo().emptyBaggage());
  }

  public SpanContextShim(
      TelemetryInfo telemetryInfo, io.opentelemetry.api.trace.SpanContext context) {
    this(telemetryInfo, context, telemetryInfo.emptyBaggage());
  }

  public SpanContextShim(
      TelemetryInfo telemetryInfo,
      io.opentelemetry.api.trace.SpanContext context,
      Baggage baggage) {
    super(telemetryInfo);
    this.context = context;
    this.baggage = baggage;
  }

  SpanContextShim newWithKeyValue(String key, String value) {
    Context parentContext = Context.current().with(baggage);

    BaggageBuilder builder = Baggage.builder().setParent(parentContext);
    builder.put(key, value, BaggageEntryMetadata.empty());

    return new SpanContextShim(telemetryInfo(), context, builder.build());
  }

  io.opentelemetry.api.trace.SpanContext getSpanContext() {
    return context;
  }

  Baggage getBaggage() {
    return baggage;
  }

  @Override
  public String toTraceId() {
    return context.getTraceIdHex();
  }

  @Override
  public String toSpanId() {
    return context.getSpanIdHex();
  }

  @Override
  public Iterable<Map.Entry<String, String>> baggageItems() {
    List<Map.Entry<String, String>> items = new ArrayList<>(baggage.size());
    baggage.forEach(
        (key, baggageEntry) ->
            items.add(new AbstractMap.SimpleImmutableEntry<>(key, baggageEntry.getValue())));
    return items;
  }

  @SuppressWarnings("ReturnMissingNullable")
  String getBaggageItem(String key) {
    return baggage.getEntryValue(key);
  }
}
