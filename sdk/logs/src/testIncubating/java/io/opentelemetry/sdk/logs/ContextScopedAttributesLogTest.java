/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.common.ContextAttributes;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.logs.internal.SdkLoggerProviderUtil;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContextScopedAttributesLogTest {

  private InMemoryLogRecordExporter exporter;
  private SdkLoggerProviderBuilder builder;

  @BeforeEach
  void setUp() {
    exporter = InMemoryLogRecordExporter.create();
    builder =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter));
  }

  @Test
  void disabledByDefault() {
    SdkLoggerProvider provider = builder.build();
    Logger logger = provider.get("test");

    Context ctx = ContextAttributes.set(Context.current(), Attributes.of(stringKey("k1"), "v1"));
    try (Scope ignored = ctx.makeCurrent()) {
      logger.logRecordBuilder().setBody("hello").emit();
    }

    assertThat(exporter.getFinishedLogRecordItems())
        .satisfiesExactly(
            log -> assertThat(log).hasBody("hello").hasAttributes(Attributes.empty()));
  }

  @Test
  void enabled_mergesWithExplicitPrecedence() {
    SdkLoggerProviderUtil.setContextScopedAttributesFilter(builder, (scope, key) -> true);
    SdkLoggerProvider provider = builder.build();
    Logger logger = provider.get("test");

    Context ctx =
        ContextAttributes.set(
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

    assertThat(exporter.getFinishedLogRecordItems())
        .satisfiesExactly(log ->
            assertThat(log)
                .hasBody("hello")
                .hasAttributes(
                    attributeEntry("k1", "from-context"),
                    attributeEntry("k2", "explicit"),
                    attributeEntry("k3", "v3")));
  }

  @Test
  void usesExplicitContext() {
    SdkLoggerProviderUtil.setContextScopedAttributesFilter(builder, (scope, key) -> true);
    SdkLoggerProvider provider = builder.build();
    Logger logger = provider.get("test");

    Context explicitCtx =
        ContextAttributes.set(Context.root(), Attributes.of(stringKey("ctx"), "explicit"));
    Context currentCtx =
        ContextAttributes.set(Context.root(), Attributes.of(stringKey("ctx"), "current"));

    try (Scope ignored = currentCtx.makeCurrent()) {
      logger.logRecordBuilder().setContext(explicitCtx).setBody("hello").emit();
    }

    assertThat(exporter.getFinishedLogRecordItems())
        .satisfiesExactly(log ->
            assertThat(log).hasBody("hello").hasAttributes(attributeEntry("ctx", "explicit")));
  }

  @Test
  void sameNameDifferentType_explicitWins() {
    SdkLoggerProviderUtil.setContextScopedAttributesFilter(builder, (scope, key) -> true);
    SdkLoggerProvider provider = builder.build();
    Logger logger = provider.get("test");

    Context ctx =
        ContextAttributes.set(
            Context.current(), Attributes.of(stringKey("collision"), "contextString"));
    try (Scope ignored = ctx.makeCurrent()) {
      logger
          .logRecordBuilder()
          .setAttribute(longKey("collision"), 999L)
          .setBody("collision test")
          .emit();
    }

    assertThat(exporter.getFinishedLogRecordItems())
        .satisfiesExactly(log ->
            assertThat(log)
                .hasBody("collision test")
                .hasAttributes(attributeEntry("collision", 999L)));
  }

  @Test
  void filterPredicate_filtersByScopeAndKey() {
    SdkLoggerProviderUtil.setContextScopedAttributesFilter(
        builder,
        (scope, key) ->
            scope.getName().equals("allowed-logger") && key.getKey().startsWith("allowed."));
    SdkLoggerProvider provider = builder.build();

    Logger allowedLogger = provider.get("allowed-logger");
    Logger otherLogger = provider.get("other-logger");

    Context ctx =
        ContextAttributes.set(
            Context.current(),
            Attributes.of(stringKey("allowed.k"), "v1", stringKey("denied.k"), "v2"));

    try (Scope ignored = ctx.makeCurrent()) {
      allowedLogger.logRecordBuilder().setBody("msg1").emit();
      otherLogger.logRecordBuilder().setBody("msg2").emit();
    }

    assertThat(exporter.getFinishedLogRecordItems())
        .satisfiesExactly(
            log1 ->
                assertThat(log1).hasBody("msg1").hasAttributes(attributeEntry("allowed.k", "v1")),
            log2 -> assertThat(log2).hasBody("msg2").hasAttributes(Attributes.empty()));
  }

  @Test
  void logRecordAttributeLimit() {
    builder.setLogLimits(() -> LogLimits.builder().setMaxNumberOfAttributes(1).build());
    SdkLoggerProviderUtil.setContextScopedAttributesFilter(builder, (scope, key) -> true);
    SdkLoggerProvider provider = builder.build();
    Logger logger = provider.get("test");

    Context ctx = ContextAttributes.set(Context.root(), Attributes.of(stringKey("scope"), "ctx"));

    try (Scope ignored = ctx.makeCurrent()) {
      logger.logRecordBuilder().setAttribute(stringKey("explicit"), "val").emit();
    }

    assertThat(exporter.getFinishedLogRecordItems())
        .satisfiesExactly(record ->
            assertThat(record)
                .hasAttributes(attributeEntry("explicit", "val"))
                .hasTotalAttributeCount(2));
  }

  @Test
  void explicitExceedsLimitAndContextPresent_tracksDroppedAttributesCorrectly() {
    builder.setLogLimits(() -> LogLimits.builder().setMaxNumberOfAttributes(2).build());
    SdkLoggerProviderUtil.setContextScopedAttributesFilter(builder, (scope, key) -> true);
    SdkLoggerProvider provider = builder.build();
    Logger logger = provider.get("test");

    Context ctx =
        ContextAttributes.set(
            Context.root(),
            Attributes.of(stringKey("ctx1"), "ctxVal1", stringKey("ctx2"), "ctxVal2"));

    try (Scope ignored = ctx.makeCurrent()) {
      logger
          .logRecordBuilder()
          .setAttribute(stringKey("exp1"), "expVal1")
          .setAttribute(stringKey("exp2"), "expVal2")
          .setAttribute(stringKey("exp3"), "expVal3")
          .setBody("test log with dropped attributes")
          .emit();
    }

    assertThat(exporter.getFinishedLogRecordItems())
        .satisfiesExactly(record ->
            assertThat(record)
                .hasBody("test log with dropped attributes")
                .hasAttributes(attributeEntry("exp1", "expVal1"), attributeEntry("exp2", "expVal2"))
                .hasTotalAttributeCount(5));
  }
}
