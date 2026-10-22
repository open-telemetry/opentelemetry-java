/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.common.ContextAttributes;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.logs.internal.SdkLoggerProviderUtil;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import java.util.List;
import org.junit.jupiter.api.Test;

class ContextScopedAttributesLogTest {

  @Test
  void contextScopedAttributes_disabledByDefault() {
    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    SdkLoggerProvider provider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter))
            .build();
    Logger logger = provider.get("test");

    Context ctx = ContextAttributes.add(Context.current(), Attributes.of(stringKey("k1"), "v1"));
    try (Scope ignored = ctx.makeCurrent()) {
      logger.logRecordBuilder().setBody("hello").emit();
    }

    List<LogRecordData> logs = exporter.getFinishedLogRecordItems();
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getAttributes()).isEmpty();
  }

  @Test
  void contextScopedAttributes_enabled_mergesWithExplicitPrecedence() {
    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    SdkLoggerProviderBuilder builder =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter));
    SdkLoggerProviderUtil.setContextScopedAttributesEnabled(builder, true);
    SdkLoggerProvider provider = builder.build();
    Logger logger = provider.get("test");

    Context ctx =
        ContextAttributes.add(
            Context.current(),
            Attributes.of(stringKey("k1"), "from-context", stringKey("k2"), "from-context"));
    try (Scope ignored = ctx.makeCurrent()) {
      logger
          .logRecordBuilder()
          .setAttribute(stringKey("k2"), "explicit")
          .setAttribute(stringKey("k3"), "v3")
          .setBody("hello")
          .emit();
    }

    List<LogRecordData> logs = exporter.getFinishedLogRecordItems();
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getAttributes())
        .isEqualTo(
            Attributes.of(
                stringKey("k1"),
                "from-context",
                stringKey("k2"),
                "explicit",
                stringKey("k3"),
                "v3"));
  }

  @Test
  void contextScopedAttributes_usesExplicitContext() {
    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    SdkLoggerProviderBuilder builder =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter));
    SdkLoggerProviderUtil.setContextScopedAttributesEnabled(builder, true);
    SdkLoggerProvider provider = builder.build();
    Logger logger = provider.get("test");

    Context explicitCtx =
        ContextAttributes.add(Context.root(), Attributes.of(stringKey("ctx"), "explicit"));
    Context currentCtx =
        ContextAttributes.add(Context.root(), Attributes.of(stringKey("ctx"), "current"));

    try (Scope ignored = currentCtx.makeCurrent()) {
      logger.logRecordBuilder().setContext(explicitCtx).setBody("hello").emit();
    }

    List<LogRecordData> logs = exporter.getFinishedLogRecordItems();
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getAttributes()).isEqualTo(Attributes.of(stringKey("ctx"), "explicit"));
  }

  @Test
  void contextScopedAttributes_reemitDoesNotRetainStaleContextAttributes() {
    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    SdkLoggerProviderBuilder builder =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter));
    SdkLoggerProviderUtil.setContextScopedAttributesEnabled(builder, true);
    SdkLoggerProvider provider = builder.build();
    Logger logger = provider.get("test");

    Context ctx1 = ContextAttributes.add(Context.root(), Attributes.of(stringKey("scope"), "ctx1"));
    Context ctx2 = ContextAttributes.add(Context.root(), Attributes.of(stringKey("scope"), "ctx2"));

    LogRecordBuilder logBuilder =
        logger.logRecordBuilder().setAttribute(stringKey("explicit"), "val");
    logBuilder.setContext(ctx1).emit();
    logBuilder.setContext(ctx2).emit();

    List<LogRecordData> logs = exporter.getFinishedLogRecordItems();
    assertThat(logs).hasSize(2);
    assertThat(logs.get(0).getAttributes())
        .isEqualTo(Attributes.of(stringKey("explicit"), "val", stringKey("scope"), "ctx1"));
    assertThat(logs.get(1).getAttributes())
        .isEqualTo(Attributes.of(stringKey("explicit"), "val", stringKey("scope"), "ctx2"));
  }

  @Test
  void contextScopedAttributes_logRecordAttributeLimit() {
    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    SdkLoggerProviderBuilder builder =
        SdkLoggerProvider.builder()
            .setLogLimits(() -> LogLimits.builder().setMaxNumberOfAttributes(1).build())
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter));
    SdkLoggerProviderUtil.setContextScopedAttributesEnabled(builder, true);
    SdkLoggerProvider provider = builder.build();
    Logger logger = provider.get("test");

    Context ctx = ContextAttributes.add(Context.root(), Attributes.of(stringKey("scope"), "ctx"));

    try (Scope ignored = ctx.makeCurrent()) {
      logger.logRecordBuilder().setAttribute(stringKey("explicit"), "val").emit();
    }

    List<LogRecordData> logs = exporter.getFinishedLogRecordItems();
    assertThat(logs).hasSize(1);
    LogRecordData record = logs.get(0);
    assertThat(record.getAttributes()).isEqualTo(Attributes.of(stringKey("explicit"), "val"));
    assertThat(record.getTotalAttributeCount()).isEqualTo(2);
  }

  @Test
  void
      contextScopedAttributes_explicitExceedsLimitAndContextPresent_tracksDroppedAttributesCorrectly() {
    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    int maxNumberOfAttributes = 2;

    SdkLoggerProviderBuilder builder =
        SdkLoggerProvider.builder()
            .setLogLimits(
                () -> LogLimits.builder().setMaxNumberOfAttributes(maxNumberOfAttributes).build())
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter));
    SdkLoggerProviderUtil.setContextScopedAttributesEnabled(builder, true);
    SdkLoggerProvider provider = builder.build();
    Logger logger = provider.get("test");

    // 2 distinct context attributes
    Context ctx =
        ContextAttributes.add(
            Context.root(),
            Attributes.of(stringKey("ctx1"), "ctxVal1", stringKey("ctx2"), "ctxVal2"));

    try (Scope ignored = ctx.makeCurrent()) {
      // 3 explicit attributes (1 dropped on builder) + 2 context attributes (both dropped on merge)
      logger
          .logRecordBuilder()
          .setAttribute(stringKey("exp1"), "expVal1")
          .setAttribute(stringKey("exp2"), "expVal2")
          .setAttribute(stringKey("exp3"), "expVal3")
          .setBody("test log with dropped attributes")
          .emit();
    }

    List<LogRecordData> logs = exporter.getFinishedLogRecordItems();
    assertThat(logs).hasSize(1);
    LogRecordData record = logs.get(0);

    // Explicit attributes take precedence and are retained up to limit (2)
    assertThat(record.getAttributes())
        .isEqualTo(Attributes.of(stringKey("exp1"), "expVal1", stringKey("exp2"), "expVal2"));
    assertThat(record.getAttributes().size()).isEqualTo(2);

    // Total offered: 3 explicit + 2 context = 5
    assertThat(record.getTotalAttributeCount()).isEqualTo(5);

    // Dropped count: 5 total - 2 retained = 3 dropped
    int droppedAttributesCount = record.getTotalAttributeCount() - record.getAttributes().size();
    assertThat(droppedAttributesCount).isEqualTo(3);
  }
}
