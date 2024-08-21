/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.google.common.io.Resources;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.resources.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
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
  private final Class<?> exporterClass;

  @RegisterExtension LogCapturer logs;
  private final String expectedFileNoWrapper;
  private final String expectedFileWrapper;

  public AbstractOtlpJsonLoggingExporterTest(
      Class<?> exporterClass, String expectedFileNoWrapper, String expectedFileWrapper) {
    this.exporterClass = exporterClass;
    logs = LogCapturer.create().captureForType(exporterClass);
    this.expectedFileNoWrapper = expectedFileNoWrapper;
    this.expectedFileWrapper = expectedFileWrapper;
  }

  protected abstract T createExporter(
      @Nullable OutputStream outputStream, MemoryMode memoryMode, boolean wrapperJsonObject);

  protected abstract T createDefaultExporter();

  protected abstract T createDefaultStdoutExporter();

  protected abstract T createExporterWithProperties(ConfigProperties properties);

  protected abstract T createStdoutExporterWithProperties(ConfigProperties properties);

  protected abstract T createStdoutExporterWithStructuredProperties(
      StructuredConfigProperties properties);

  protected abstract T toBuilderAndBack(T exporter);

  protected abstract CompletableResultCode export(T exporter);

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
  void testExport(String name, TestCase testCase) throws JSONException, IOException {
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
  void config() {
    assertToString(
        createDefaultExporter(),
        "{memoryMode=IMMUTABLE_DATA, wrapperJsonObject=false, jsonWriter=LoggerJsonWriter}");
    assertToString(
        createDefaultStdoutExporter(),
        "{memoryMode=IMMUTABLE_DATA, wrapperJsonObject=true, jsonWriter=StreamJsonWriter{outputStream=stdout}}");
  }

  private void assertToString(T exporter, String expected) {
    assertThat(exporter.toString()).isEqualTo(exporterClass.getSimpleName() + expected);
    assertThat(toBuilderAndBack(exporter).toString())
        .isEqualTo(exporterClass.getSimpleName() + expected);
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
}
