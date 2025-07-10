/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import static io.opentelemetry.sdk.internal.ScopeConfiguratorBuilder.nameEquals;
import static io.opentelemetry.sdk.logs.internal.LoggerConfig.disabled;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.common.ExtendedAttributeKey;
import io.opentelemetry.api.incubator.common.ExtendedAttributes;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.data.internal.ExtendedLogRecordData;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.logs.internal.SdkLoggerProviderUtil;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Demonstrating usage of extended Logs Bridge API. */
class ExtendedLogsBridgeApiUsageTest {

  private static final java.util.logging.Logger logger =
      java.util.logging.Logger.getLogger(ExtendedLogsBridgeApiUsageTest.class.getName());

  @Test
  void loggerEnabled() {
    // Setup SdkLoggerProvider
    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    SdkLoggerProviderBuilder loggerProviderBuilder =
        SdkLoggerProvider.builder()
            // Default resource used for demonstration purposes
            .setResource(Resource.getDefault())
            // In-memory exporter used for demonstration purposes
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter));
    // Disable loggerB
    SdkLoggerProviderUtil.addLoggerConfiguratorCondition(
        loggerProviderBuilder, nameEquals("loggerB"), disabled());
    SdkLoggerProvider loggerProvider = loggerProviderBuilder.build();

    // Create loggerA and loggerB
    ExtendedLogger loggerA = (ExtendedLogger) loggerProvider.get("loggerA");
    ExtendedLogger loggerB = (ExtendedLogger) loggerProvider.get("loggerB");

    // Check if logger is enabled before emitting log and avoid unnecessary computation
    if (loggerA.isEnabled(Severity.INFO)) {
      loggerA
          .logRecordBuilder()
          .setSeverity(Severity.INFO)
          .setBody("hello world!")
          .setAllAttributes(Attributes.builder().put("result", flipCoin()).build())
          .emit();
    }
    if (loggerB.isEnabled(Severity.INFO)) {
      loggerB
          .logRecordBuilder()
          .setSeverity(Severity.INFO)
          .setBody("hello world!")
          .setAllAttributes(Attributes.builder().put("result", flipCoin()).build())
          .emit();
    }

    // loggerA is enabled, loggerB is disabled
    assertThat(loggerA.isEnabled(Severity.INFO)).isTrue();
    assertThat(loggerB.isEnabled(Severity.INFO)).isFalse();

    // Collected data only consists of logs from loggerA. Note, loggerB's logs would be
    // omitted from the results even if logs were emitted. The check if enabled simply avoids
    // unnecessary computation.
    assertThat(exporter.getFinishedLogRecordItems())
        .allSatisfy(
            logRecordData ->
                assertThat(logRecordData.getInstrumentationScopeInfo().getName())
                    .isEqualTo("loggerA"));
  }

  private static final Random random = new Random();

  private static String flipCoin() {
    return random.nextBoolean() ? "heads" : "tails";
  }

  // Primitive keys
  AttributeKey<String> strKey = AttributeKey.stringKey("acme.string");
  AttributeKey<Long> longKey = AttributeKey.longKey("acme.long");
  AttributeKey<Boolean> booleanKey = AttributeKey.booleanKey("acme.boolean");
  AttributeKey<Double> doubleKey = AttributeKey.doubleKey("acme.double");

  // Primitive array keys
  AttributeKey<List<String>> strArrKey = AttributeKey.stringArrayKey("acme.string_array");
  AttributeKey<List<Long>> longArrKey = AttributeKey.longArrayKey("acme.long_array");
  AttributeKey<List<Boolean>> booleanArrKey = AttributeKey.booleanArrayKey("acme.boolean_array");
  AttributeKey<List<Double>> doubleArrKey = AttributeKey.doubleArrayKey("acme.double_array");

  // Extended keys
  ExtendedAttributeKey<ExtendedAttributes> mapKey =
      ExtendedAttributeKey.extendedAttributesKey("acme.map");

  @Test
  @SuppressLogger(ExtendedLogsBridgeApiUsageTest.class)
  void extendedAttributesUsage() {
    // Initialize from builder. Varargs style initialization (ExtendedAttributes.of(...) not
    // supported.
    ExtendedAttributes extendedAttributes =
        ExtendedAttributes.builder()
            .put(strKey, "value")
            .put(longKey, 1L)
            .put(booleanKey, true)
            .put(doubleKey, 1.1)
            .put(strArrKey, Arrays.asList("value1", "value2"))
            .put(longArrKey, Arrays.asList(1L, 2L))
            .put(booleanArrKey, Arrays.asList(true, false))
            .put(doubleArrKey, Arrays.asList(1.1, 2.2))
            .put(
                mapKey,
                ExtendedAttributes.builder().put("childStr", "value").put("childLong", 1L).build())
            .build();

    // Retrieval
    assertThat(extendedAttributes.get(strKey)).isEqualTo("value");
    assertThat(extendedAttributes.get(longKey)).isEqualTo(1);
    assertThat(extendedAttributes.get(booleanKey)).isEqualTo(true);
    assertThat(extendedAttributes.get(doubleKey)).isEqualTo(1.1);
    assertThat(extendedAttributes.get(strArrKey)).isEqualTo(Arrays.asList("value1", "value2"));
    assertThat(extendedAttributes.get(longArrKey)).isEqualTo(Arrays.asList(1L, 2L));
    assertThat(extendedAttributes.get(booleanArrKey)).isEqualTo(Arrays.asList(true, false));
    assertThat(extendedAttributes.get(doubleArrKey)).isEqualTo(Arrays.asList(1.1, 2.2));
    assertThat(extendedAttributes.get(mapKey))
        .isEqualTo(
            ExtendedAttributes.builder().put("childStr", "value").put("childLong", 1L).build());

    // Iteration
    // Output:
    // acme.boolean(BOOLEAN): true
    // acme.boolean_array(BOOLEAN_ARRAY): [true, false]
    // acme.double(DOUBLE): 1.1
    // acme.double_array(DOUBLE_ARRAY): [1.1, 2.2]
    // acme.long(LONG): 1
    // acme.long_array(LONG_ARRAY): [1, 2]
    // acme.map(EXTENDED_ATTRIBUTES): {childLong=1, childStr="value"}
    // acme.string(STRING): value
    // acme.string_array(STRING_ARRAY): [value1, value2]
    extendedAttributes.forEach(
        (extendedAttributeKey, object) ->
            logger.info(
                String.format(
                    "%s(%s): %s",
                    extendedAttributeKey.getKey(), extendedAttributeKey.getType(), object)));
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated code
  void logRecordBuilder_ExtendedAttributes() {
    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    SdkLoggerProvider loggerProvider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter))
            .build();

    Logger logger = loggerProvider.get("logger");

    // Can set either standard or extended attributes on
    ((ExtendedLogRecordBuilder) logger.logRecordBuilder())
        .setBody("message")
        .setAttribute(strKey, "value")
        .setAttribute(longKey, 1L)
        .setAttribute(booleanKey, true)
        .setAttribute(doubleKey, 1.1)
        .setAttribute(strArrKey, Arrays.asList("value1", "value2"))
        .setAttribute(longArrKey, Arrays.asList(1L, 2L))
        .setAttribute(booleanArrKey, Arrays.asList(true, false))
        .setAttribute(doubleArrKey, Arrays.asList(1.1, 2.2))
        .setAttribute(
            mapKey,
            ExtendedAttributes.builder().put("childStr", "value").put("childLong", 1L).build())
        .setAllAttributes(Attributes.builder().put("key1", "value").build())
        .setAllAttributes(ExtendedAttributes.builder().put("key2", "value").build())
        .emit();

    assertThat(exporter.getFinishedLogRecordItems())
        .satisfiesExactly(
            logRecordData -> {
              assertThat(logRecordData).isInstanceOf(ExtendedLogRecordData.class);
              ExtendedLogRecordData extendedLogRecordData = (ExtendedLogRecordData) logRecordData;

              // Optionally access standard attributes, which filters out any extended attribute
              // types
              assertThat(extendedLogRecordData.getAttributes())
                  .isEqualTo(
                      Attributes.builder()
                          .put(strKey, "value")
                          .put(longKey, 1L)
                          .put(booleanKey, true)
                          .put(doubleKey, 1.1)
                          .put(strArrKey, Arrays.asList("value1", "value2"))
                          .put(longArrKey, Arrays.asList(1L, 2L))
                          .put(booleanArrKey, Arrays.asList(true, false))
                          .put(doubleArrKey, Arrays.asList(1.1, 2.2))
                          .put("key1", "value")
                          .put("key2", "value")
                          .build());

              // But preferably access and serialize full extended attributes
              assertThat(extendedLogRecordData.getExtendedAttributes())
                  .isEqualTo(
                      ExtendedAttributes.builder()
                          .put(strKey, "value")
                          .put(longKey, 1L)
                          .put(booleanKey, true)
                          .put(doubleKey, 1.1)
                          .put(strArrKey, Arrays.asList("value1", "value2"))
                          .put(longArrKey, Arrays.asList(1L, 2L))
                          .put(booleanArrKey, Arrays.asList(true, false))
                          .put(doubleArrKey, Arrays.asList(1.1, 2.2))
                          .put(
                              mapKey,
                              ExtendedAttributes.builder()
                                  .put("childStr", "value")
                                  .put("childLong", 1L)
                                  .build())
                          .put("key1", "value")
                          .put("key2", "value")
                          .build());
            });
  }
}
