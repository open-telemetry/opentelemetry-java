/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import com.google.common.io.Resources;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.resources.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.assertj.core.api.AbstractObjectAssert;
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

abstract class AbstractOtlpJsonLoggingExporterTest<T> {

  private static PrintStream systemOut;

  protected static final Resource RESOURCE =
      Resource.create(Attributes.builder().put("key", "value").build());

  private static final ByteArrayOutputStream STREAM = new ByteArrayOutputStream();
  private static final PrintStream PRINT_STREAM = new PrintStream(STREAM);

  @RegisterExtension LogCapturer logs;
  private final String defaultConfigString;
  private final Class<?> providerClass;

  private final Class<?> componentProviderType;

  private final String expectedFileNoWrapper;
  private final String expectedFileWrapper;

  public AbstractOtlpJsonLoggingExporterTest(
      Class<?> exporterClass,
      Class<?> providerClass,
      Class<?> componentProviderType,
      String expectedFileNoWrapper,
      String expectedFileWrapper,
      String defaultConfigString) {
    this.providerClass = providerClass;
    this.componentProviderType = componentProviderType;
    this.expectedFileNoWrapper = expectedFileNoWrapper;
    this.expectedFileWrapper = expectedFileWrapper;
    logs = LogCapturer.create().captureForType(exporterClass);
    this.defaultConfigString = defaultConfigString;
  }

  protected abstract T createExporter(
      @Nullable OutputStream outputStream, boolean wrapperJsonObject);

  protected abstract T createDefaultExporter();

  protected abstract T createDefaultStdoutExporter();

  protected abstract T toBuilderAndBack(T exporter);

  protected abstract CompletableResultCode export(T exporter);

  protected abstract CompletableResultCode flush(T exporter);

  protected abstract CompletableResultCode shutdown(T exporter);

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
    return Stream.of(System.out, null)
        .flatMap(
            outputStream ->
                Stream.of(true, false)
                    .flatMap(
                        wrapperJsonObject ->
                            Stream.of(
                                Arguments.of(
                                    "OutputStream="
                                        + (outputStream == null ? "logger" : "System.out")
                                        + ", wrapperJsonObject="
                                        + wrapperJsonObject,
                                    new TestCase(outputStream, wrapperJsonObject)))));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("exportTestCases")
  void exportWithProgrammaticConfig(String name, TestCase testCase)
      throws JSONException, IOException {
    setUp();

    T exporter = createExporter(testCase.getOutputStream(), testCase.isWrapperJsonObject());
    export(exporter);
    flush(exporter);

    String output = output(testCase.getOutputStream());

    String expectedFile =
        testCase.isWrapperJsonObject() ? expectedFileWrapper : expectedFileNoWrapper;

    String expectedJson =
        Resources.toString(Resources.getResource(expectedFile), StandardCharsets.UTF_8);

    JSONAssert.assertEquals("Got \n" + output, expectedJson, output, false);

    if (testCase.isWrapperJsonObject()) {
      assertThat(output).doesNotContain("\n");
    }
  }

  @Test
  void testShutdown() {
    T exporter = createDefaultExporter();
    assertThat(shutdown(exporter).isSuccess()).isTrue();
    assertThat(export(exporter).join(10, TimeUnit.SECONDS).isSuccess()).isFalse();
    assertThat(output(null)).isEmpty();
    assertThat(shutdown(exporter).isSuccess()).isTrue();
    logs.assertContains("Calling shutdown() multiple times.");
  }

  @Test
  void loggingOtlpProviderConfig() {
    assertFullToString(createDefaultExporter(), defaultConfigString);

    assertFullToString(
        loadExporter(DefaultConfigProperties.createFromMap(emptyMap()), "logging-otlp"),
        defaultConfigString);
  }

  @Test
  void stdoutProviderConfig() {
    assertStdoutProperties(
        createDefaultStdoutExporter(),
        ImmutableMap.of(
            "wrapperJsonObject", "true",
            "jsonWriter", "StreamJsonWriter{outputStream=stdout}"));

    assertStdoutProperties(
        loadExporter(DefaultConfigProperties.createFromMap(emptyMap()), "otlp-stdout"),
        ImmutableMap.of(
            "wrapperJsonObject", "true",
            "jsonWriter", "StreamJsonWriter{outputStream=stdout}"));
  }

  @Test
  void stdoutComponentProviderConfig() {
    StructuredConfigProperties properties = mock(StructuredConfigProperties.class);

    assertStdoutProperties(
        exporterFromComponentProvider(properties),
        ImmutableMap.of(
            "wrapperJsonObject", "true",
            "jsonWriter", "StreamJsonWriter{outputStream=stdout}"));
  }

  @SuppressWarnings("unchecked")
  protected T exporterFromComponentProvider(StructuredConfigProperties properties) {
    return (T)
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

  private static Stream<?> loadSpi(Class<?> type) {
    return Streams.stream(ServiceLoader.load(type, type.getClassLoader()).iterator());
  }

  private void assertFullToString(T exporter, String expected) {
    assertThat(exporter.toString()).contains(expected);
    assertThat(toBuilderAndBack(exporter).toString()).contains(expected);
  }

  private void assertStdoutProperties(T exporter, Map<String, String> expected) {
    AbstractObjectAssert<?, ?> assertThat = assertThat(exporter).extracting("delegate");

    expected.forEach(
        (key, value) -> assertThat.extracting(key).extracting(Object::toString).isEqualTo(value));
    assertThat(toBuilderAndBack(exporter).toString()).isEqualTo(exporter.toString());
  }
}
