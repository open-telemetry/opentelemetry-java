/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporter;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.json.JSONException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.event.LoggingEvent;

abstract class AbstractOtlpStdoutExporterTest<T> {

  private static PrintStream systemOut;

  private static final ByteArrayOutputStream STREAM = new ByteArrayOutputStream();
  private static final PrintStream PRINT_STREAM = new PrintStream(STREAM);

  @RegisterExtension LogCapturer logs;
  private final String defaultConfigString;
  private final String type;
  private final TestDataExporter<? super T> testDataExporter;
  protected final Class<?> exporterClass;
  private final Class<?> providerClass;
  private final Class<?> componentProviderType;

  public AbstractOtlpStdoutExporterTest(
      String type,
      TestDataExporter<? super T> testDataExporter,
      Class<?> exporterClass,
      Class<?> providerClass,
      Class<?> componentProviderType,
      String defaultConfigString) {
    this.type = type;
    this.testDataExporter = testDataExporter;
    this.exporterClass = exporterClass;
    this.providerClass = providerClass;
    logs = LogCapturer.create().captureForType(exporterClass);
    this.defaultConfigString = defaultConfigString;
    this.componentProviderType = componentProviderType;
  }

  protected abstract T createExporter(
      @Nullable OutputStream outputStream, boolean wrapperJsonObject);

  protected abstract T createDefaultExporter();

  private String output(@Nullable OutputStream outputStream) {
    if (outputStream == null) {
      return logs.getEvents().stream()
          .map(LoggingEvent::getMessage)
          .reduce("", (a, b) -> a + b + "\n")
          .trim();
    }

    try {
      return STREAM.toString(StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeAll
  @SuppressWarnings("SystemOut")
  static void setUpStatic() {
    systemOut = System.out;
    System.setOut(PRINT_STREAM);
  }

  @AfterAll
  @SuppressWarnings("SystemOut")
  static void tearDownStatic() {
    System.setOut(systemOut);
  }

  @BeforeEach
  void setUp() {
    STREAM.reset();
  }

  public static class TestCase {
    @Nullable private final OutputStream outputStream;
    private final boolean wrapperJsonObject;

    public TestCase(@Nullable OutputStream outputStream, boolean wrapperJsonObject) {
      this.outputStream = outputStream;
      this.wrapperJsonObject = wrapperJsonObject;
    }

    @Nullable
    public OutputStream getOutputStream() {
      return outputStream;
    }

    public boolean isWrapperJsonObject() {
      return wrapperJsonObject;
    }
  }

  @SuppressWarnings("SystemOut")
  public static Stream<Arguments> exportTestCases() {
    return ImmutableList.of(
        testCase(System.out, /* wrapperJsonObject= */ true),
        testCase(System.out, /* wrapperJsonObject= */ false),
        testCase(null, /* wrapperJsonObject= */ true),
        testCase(null, /* wrapperJsonObject= */ false))
        .stream();
  }

  private static Arguments testCase(@Nullable PrintStream outputStream, boolean wrapperJsonObject) {
    return Arguments.of(
        "OutputStream="
            + (outputStream == null ? "logger" : "System.out")
            + ", wrapperJsonObject="
            + wrapperJsonObject,
        new TestCase(outputStream, wrapperJsonObject));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("exportTestCases")
  void exportWithProgrammaticConfig(String name, TestCase testCase)
      throws JSONException, IOException {
    setUp();

    T exporter = createExporter(testCase.getOutputStream(), testCase.isWrapperJsonObject());
    testDataExporter.export(exporter);
    testDataExporter.flush(exporter);

    String output = output(testCase.getOutputStream());
    String expectedJson = testDataExporter.getExpectedJson(testCase.isWrapperJsonObject());
    JSONAssert.assertEquals("Got \n" + output, expectedJson, output, false);

    if (testCase.isWrapperJsonObject()) {
      assertThat(output).doesNotContain("\n");
    }
  }

  @Test
  void testShutdown() {
    T exporter = createDefaultExporter();
    assertThat(testDataExporter.shutdown(exporter).isSuccess()).isTrue();
    assertThat(testDataExporter.export(exporter).join(10, TimeUnit.SECONDS).isSuccess()).isFalse();
    assertThat(output(null)).isEmpty();
    assertThat(testDataExporter.shutdown(exporter).isSuccess()).isTrue();
    logs.assertContains("Calling shutdown() multiple times.");
  }

  @Test
  void defaultToString() {
    assertFullToString(createDefaultExporter(), defaultConfigString);

    assertFullToString(
        loadExporter(DefaultConfigProperties.createFromMap(emptyMap()), type), defaultConfigString);
  }

  protected OtlpStdoutLogRecordExporter exporterFromComponentProvider(
      StructuredConfigProperties properties) {
    return (OtlpStdoutLogRecordExporter)
        ((ComponentProvider<?>)
                loadSpi(ComponentProvider.class)
                    .filter(
                        p -> {
                          ComponentProvider<?> c = (ComponentProvider<?>) p;
                          return "otlp-stdout".equals(c.getName())
                              && c.getType().equals(componentProviderType);
                        })
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No provider found")))
            .create(properties);
  }

  @Test
  void componentProviderConfig() {
    StructuredConfigProperties properties = mock(StructuredConfigProperties.class);
    OtlpStdoutLogRecordExporter exporter = exporterFromComponentProvider(properties);

    assertThat(exporter).extracting("wrapperJsonObject").isEqualTo(true);
    assertThat(exporter)
        .extracting("jsonWriter")
        .extracting(Object::toString)
        .isEqualTo("StreamJsonWriter{outputStream=stdout}");
  }

  @SuppressWarnings("unchecked")
  protected T loadExporter(ConfigProperties config, String name) {
    Object provider = loadProvider(name);

    try {
      return (T)
          provider
              .getClass()
              .getDeclaredMethod("createExporter", ConfigProperties.class)
              .invoke(provider, config);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Object loadProvider(String want) {
    return loadSpi(providerClass)
        .filter(
            p -> {
              try {
                return want.equals(p.getClass().getDeclaredMethod("getName").invoke(p));
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            })
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No provider found"));
  }

  protected static Stream<?> loadSpi(Class<?> type) {
    return Streams.stream(ServiceLoader.load(type, type.getClassLoader()).iterator());
  }

  private void assertFullToString(T exporter, String expected) {
    assertThat(exporter.toString()).isEqualTo(expected);
  }
}
