/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.common.ContextAttributes;
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
}
