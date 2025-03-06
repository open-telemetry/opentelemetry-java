/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.sdk.internal.ExceptionAttributeResolver.EXCEPTION_MESSAGE;
import static io.opentelemetry.sdk.internal.ExceptionAttributeResolver.EXCEPTION_STACKTRACE;
import static io.opentelemetry.sdk.internal.ExceptionAttributeResolver.EXCEPTION_TYPE;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.satisfies;

import io.opentelemetry.api.incubator.logs.ExtendedLogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.sdk.internal.ExceptionAttributeResolver;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.logs.internal.SdkLoggerProviderUtil;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

class ExtendedLoggerBuilderTest {

  private final InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
  private final SdkLoggerProviderBuilder loggerProviderBuilder =
      SdkLoggerProvider.builder().addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter));

  @Test
  void setException_DefaultResolver() {
    Logger logger = loggerProviderBuilder.build().get("logger");

    ((ExtendedLogRecordBuilder) logger.logRecordBuilder())
        .setException(new Exception("error"))
        .emit();

    assertThat(exporter.getFinishedLogRecordItems())
        .satisfiesExactly(
            logRecord ->
                assertThat(logRecord)
                    .hasAttributesSatisfyingExactly(
                        equalTo(EXCEPTION_TYPE, "java.lang.Exception"),
                        equalTo(EXCEPTION_MESSAGE, "error"),
                        satisfies(
                            EXCEPTION_STACKTRACE,
                            stacktrace -> stacktrace.startsWith("java.lang.Exception: error\n"))));
  }

  @Test
  void setException_CustomResolver() {
    SdkLoggerProviderUtil.setExceptionAttributeResolver(
        loggerProviderBuilder,
        new ExceptionAttributeResolver() {
          @Override
          public String getExceptionType(Throwable throwable) {
            return "type";
          }

          @Nullable
          @Override
          public String getExceptionMessage(Throwable throwable) {
            return null;
          }

          @Override
          public String getExceptionStacktrace(Throwable throwable) {
            return "stacktrace";
          }
        });

    Logger logger = loggerProviderBuilder.build().get("logger");

    ((ExtendedLogRecordBuilder) logger.logRecordBuilder())
        .setException(new Exception("error"))
        .emit();

    assertThat(exporter.getFinishedLogRecordItems())
        .satisfiesExactly(
            logRecord ->
                assertThat(logRecord)
                    .hasAttributesSatisfyingExactly(
                        equalTo(EXCEPTION_TYPE, "type"),
                        equalTo(EXCEPTION_STACKTRACE, "stacktrace")));
  }
}
