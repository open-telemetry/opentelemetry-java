/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.export.BatchLogProcessor;
import io.opentelemetry.sdk.logs.util.TestLogExporter;
import io.opentelemetry.sdk.resources.Resource;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class DemoTest {

  @Test
  @SuppressWarnings("SystemOut")
  void usageTest() {
    TestLogExporter testLogExporter = new TestLogExporter();

    SdkLogSinkProvider sdkLogSinkProvider =
        SdkLogSinkProvider.builder()
            .setResource(Resource.builder().put("key", "value").build())
            .addLogProcessor(System.out::println)
            .addLogProcessor(
                BatchLogProcessor.builder(testLogExporter).setExporterTimeoutMillis(1000).build())
            .build();

    LogSink logSink = sdkLogSinkProvider.get("my-library");

    for (int i = 0; i < 10; i++) {
      logSink.offer(
          logSink
              .builder()
              .setEpochMillis(System.currentTimeMillis())
              .setBody("my message " + i)
              .setAttributes(Attributes.builder().put("foo", "bar").build())
              .build());
    }

    await()
        .atMost(Duration.ofSeconds(30))
        .untilAsserted(
            () -> {
              List<LogData> logs = testLogExporter.getRecords();

              assertThat(logs.size()).isEqualTo(10);
              assertThat(logs)
                  .allSatisfy(
                      (Consumer<LogData>)
                          logData -> {
                            assertThat(logData.getResource())
                                .isEqualTo(Resource.builder().put("key", "value").build());
                            assertThat(logData.getInstrumentationLibraryInfo())
                                .isEqualTo(InstrumentationLibraryInfo.create("my-library", null));
                            assertThat(logData.getBody().asString()).startsWith("my message");
                            assertThat(logData.getAttributes())
                                .isEqualTo(Attributes.builder().put("foo", "bar").build());
                          });
            });
  }
}
