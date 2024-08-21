/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import com.google.common.io.Resources;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
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

  @SuppressWarnings("unused")
  private final Class<?> componentProviderType; // todo remove once used

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
      @Nullable OutputStream outputStream, MemoryMode memoryMode, boolean wrapperJsonObject);

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
    private final MemoryMode memoryMode;
    private final boolean wrapperJsonObject;

    public TestCase(
        @Nullable OutputStream outputStream, MemoryMode memoryMode, boolean wrapperJsonObject) {
      this.outputStream = outputStream;
      this.memoryMode = memoryMode;
      this.wrapperJsonObject = wrapperJsonObject;
    }

    @Nullable
    public OutputStream getOutputStream() {
      return outputStream;
    }

    public MemoryMode getMemoryMode() {
      return memoryMode;
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
                Stream.of(MemoryMode.IMMUTABLE_DATA, MemoryMode.REUSABLE_DATA)
                    .flatMap(
                        memoryMode ->
                            Stream.of(true, false)
                                .flatMap(
                                    wrapperJsonObject ->
                                        Stream.of(
                                            Arguments.of(
                                                "OutputStream="
                                                    + (outputStream == null
                                                        ? "logger"
                                                        : "System.out")
                                                    + ", MemoryMode="
                                                    + memoryMode
                                                    + ", wrapperJsonObject="
                                                    + wrapperJsonObject,
                                                new TestCase(
                                                    outputStream,
                                                    memoryMode,
                                                    wrapperJsonObject))))));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("exportTestCases")
  void exportWithProgrammaticConfig(String name, TestCase testCase)
      throws JSONException, IOException {
    setUp();

    if (testCase.getMemoryMode() == MemoryMode.REUSABLE_DATA && !testCase.isWrapperJsonObject()) {
      assertThatCode(
              () ->
                  createExporter(
                      testCase.getOutputStream(),
                      testCase.getMemoryMode(),
                      testCase.isWrapperJsonObject()))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Reusable data mode is not supported without wrapperJsonObject");
      return;
    }

    T exporter =
        createExporter(
            testCase.getOutputStream(), testCase.getMemoryMode(), testCase.isWrapperJsonObject());
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
    assertToStringProperties(
        createDefaultStdoutExporter(),
        ImmutableMap.of(
            "memoryMode", "IMMUTABLE_DATA",
            "wrapperJsonObject", "true",
            "jsonWriter", "StreamJsonWriter{outputStream=stdout}"));

    assertToStringProperties(
        loadExporter(DefaultConfigProperties.createFromMap(emptyMap()), "otlp-stdout"),
        ImmutableMap.of(
            "memoryMode", "IMMUTABLE_DATA",
            "wrapperJsonObject", "true",
            "jsonWriter", "StreamJsonWriter{outputStream=stdout}"));

    assertToStringProperties(
        loadExporter(
            DefaultConfigProperties.createFromMap(
                singletonMap("otel.java.experimental.exporter.memory_mode", "reusable_data")),
            "otlp-stdout"),
        ImmutableMap.of("memoryMode", "REUSABLE_DATA"));
  }

  @Test
  void stdoutComponentProviderConfig() {
    //    ComponentProvider<?> provider = (ComponentProvider<?>)
    // loadSpi(ComponentProvider.class).filter(
    //        p -> {
    //          ComponentProvider<?> c = (ComponentProvider<?>) p;
    //          return "otlp-stdout".equals(c.getName()) && c.getType()
    //              .equals(componentProviderType);
    //        }).findFirst().orElseThrow(() -> new IllegalStateException("No provider found"));

    // todo: implement DefaultStructuredConfigProperties

    //    assertToString(
    //        provider.create(DefaultStructuredConfigProperties.createFromMap(emptyMap())),
    //        loadExporter(DefaultConfigProperties.createFromMap(emptyMap()), "otlp-stdout"),
    //        "{memoryMode=IMMUTABLE_DATA, wrapperJsonObject=true,
    // jsonWriter=StreamJsonWriter{outputStream=stdout}}");
    //
    //    assertToString(
    //        provider.create((DefaultStructuredConfigProperties.createFromMap(singletonMap(
    //            "memory_mode", "reusable_data"
    //        )), "otlp-stdout"),
    //        "{memoryMode=REUSABLE_DATA, wrapperJsonObject=true,
    // jsonWriter=StreamJsonWriter{outputStream=stdout}}");
    //
    //    stdoutStructuredPropertiesTestCases().forEach(
    //        (config, expected) -> {
    //          assertToString(
    //              (T) provider.create(config),
    //              expected);
    //        });
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
    assertThat(exporter.toString()).isEqualTo(expected);
    assertThat(toBuilderAndBack(exporter).toString()).isEqualTo(expected);
  }

  private void assertToStringProperties(T exporter, Map<String, String> expected) {
    expected.forEach(
        (key, value) ->
            assertThat(exporter).extracting(key).extracting(Object::toString).isEqualTo(value));
    assertThat(toBuilderAndBack(exporter).toString()).isEqualTo(exporter.toString());
  }
}
