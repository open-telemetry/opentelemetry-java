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
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporter;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogRecordExporterProvider;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.json.JSONException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.event.LoggingEvent;

class OtlpStdoutLogRecordExporterTest {

  private static final ByteArrayOutputStream SYSTEM_OUT_STREAM = new ByteArrayOutputStream();
  private static final PrintStream SYSTEM_OUT_PRINT_STREAM = new PrintStream(SYSTEM_OUT_STREAM);
  private static PrintStream systemOut;
  private static final Class<?> EXPORTER_CLASS = OtlpStdoutLogRecordExporter.class;
  private static final String DEFAULT_CONFIG_STRING =
      "OtlpStdoutLogRecordExporter{jsonWriter=StreamJsonWriter{outputStream=stdout}, wrapperJsonObject=true}";
  private static final String TYPE = "experimental-otlp/stdout";
  private static final TestDataExporter<? super OtlpStdoutLogRecordExporter> TEST_DATA_EXPORTER =
      TestDataExporter.forLogs();
  private static final Class<?> PROVIDER_CLASS = ConfigurableLogRecordExporterProvider.class;
  private static final Class<?> COMPONENT_PROVIDER_TYPE = LogRecordExporter.class;

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(OtlpStdoutLogRecordExporter.class);

  @TempDir Path tempDir;

  @BeforeAll
  @SuppressWarnings("SystemOut")
  static void setUpStatic() {
    systemOut = System.out;
    System.setOut(SYSTEM_OUT_PRINT_STREAM);
  }

  @AfterAll
  @SuppressWarnings("SystemOut")
  static void tearDownStatic() {
    System.setOut(systemOut);
  }

  @BeforeEach
  void setUp() {
    SYSTEM_OUT_STREAM.reset();
  }

  static Stream<Arguments> exportTestCases() {
    return ImmutableList.of(
        testCase(OutputType.SYSTEM_OUT, /* wrapperJsonObject= */ true),
        testCase(OutputType.SYSTEM_OUT, /* wrapperJsonObject= */ false),
        testCase(OutputType.FILE, /* wrapperJsonObject= */ true),
        testCase(OutputType.FILE, /* wrapperJsonObject= */ false),
        testCase(OutputType.FILE_AND_BUFFERED_WRITER, /* wrapperJsonObject= */ true),
        testCase(OutputType.FILE_AND_BUFFERED_WRITER, /* wrapperJsonObject= */ false),
        testCase(OutputType.LOGGER, /* wrapperJsonObject= */ true),
        testCase(OutputType.LOGGER, /* wrapperJsonObject= */ false))
        .stream();
  }

  private static Arguments testCase(OutputType type, boolean wrapperJsonObject) {
    return Arguments.of(
        "output=" + type + ", wrapperJsonObject=" + wrapperJsonObject,
        new TestCase(type, wrapperJsonObject));
  }

  private static Stream<?> loadSpi(Class<?> type) {
    return Streams.stream(ServiceLoader.load(type, type.getClassLoader()).iterator());
  }

  private static OtlpStdoutLogRecordExporter createDefaultExporter() {
    return OtlpStdoutLogRecordExporter.builder().build();
  }

  private static OtlpStdoutLogRecordExporter createExporter(
      @Nullable OutputStream outputStream, boolean wrapperJsonObject) {
    OtlpStdoutLogRecordExporterBuilder builder =
        OtlpStdoutLogRecordExporter.builder().setWrapperJsonObject(wrapperJsonObject);
    if (outputStream != null) {
      builder.setOutput(outputStream);
    } else {
      builder.setOutput(Logger.getLogger(EXPORTER_CLASS.getName()));
    }
    return builder.build();
  }

  private String output(@Nullable OutputStream outputStream, @Nullable Path file) {
    if (outputStream == null) {
      return logs.getEvents().stream()
          .map(LoggingEvent::getMessage)
          .reduce("", (a, b) -> a + b + "\n")
          .trim();
    }

    if (file != null) {
      try {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    try {
      return SYSTEM_OUT_STREAM.toString(StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("SystemOut")
  @ParameterizedTest(name = "{0}")
  @MethodSource("exportTestCases")
  void exportWithProgrammaticConfig(String name, TestCase testCase)
      throws JSONException, IOException {
    OutputStream outputStream;
    Path file = null;
    switch (testCase.getOutputType()) {
      case LOGGER:
        outputStream = null;
        break;
      case SYSTEM_OUT:
        outputStream = System.out;
        break;
      case FILE:
        file = tempDir.resolve("test.log");
        outputStream = Files.newOutputStream(file);
        break;
      case FILE_AND_BUFFERED_WRITER:
        file = tempDir.resolve("test.log");
        outputStream = new BufferedOutputStream(Files.newOutputStream(file));
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + testCase.getOutputType());
    }
    OtlpStdoutLogRecordExporter exporter =
        createExporter(outputStream, testCase.isWrapperJsonObject());
    TEST_DATA_EXPORTER.export(exporter);

    String output = output(outputStream, file);
    String expectedJson = TEST_DATA_EXPORTER.getExpectedJson(testCase.isWrapperJsonObject());
    JSONAssert.assertEquals("Got \n" + output, expectedJson, output, false);

    if (testCase.isWrapperJsonObject()) {
      assertThat(output).doesNotContain("\n");
    }
  }

  @Test
  void testShutdown() {
    OtlpStdoutLogRecordExporter exporter = createDefaultExporter();
    assertThat(TEST_DATA_EXPORTER.shutdown(exporter).isSuccess()).isTrue();
    assertThat(TEST_DATA_EXPORTER.export(exporter).join(10, TimeUnit.SECONDS).isSuccess())
        .isFalse();
    assertThat(TEST_DATA_EXPORTER.flush(exporter).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    assertThat(output(null, null)).isEmpty();
    assertThat(TEST_DATA_EXPORTER.shutdown(exporter).isSuccess()).isTrue();
    logs.assertContains("Calling shutdown() multiple times.");
  }

  @Test
  void defaultToString() {
    assertThat(createDefaultExporter().toString()).isEqualTo(DEFAULT_CONFIG_STRING);

    assertThat(loadExporter(DefaultConfigProperties.createFromMap(emptyMap()), TYPE).toString())
        .isEqualTo(DEFAULT_CONFIG_STRING);
  }

  private OtlpStdoutLogRecordExporter exporterFromComponentProvider(
      DeclarativeConfigProperties properties) {
    return (OtlpStdoutLogRecordExporter)
        ((ComponentProvider<?>)
                loadSpi(ComponentProvider.class)
                    .filter(
                        p -> {
                          ComponentProvider<?> c = (ComponentProvider<?>) p;
                          return "experimental-otlp/stdout".equals(c.getName())
                              && c.getType().equals(COMPONENT_PROVIDER_TYPE);
                        })
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No provider found")))
            .create(properties);
  }

  @Test
  void componentProviderConfig() {
    DeclarativeConfigProperties properties = mock(DeclarativeConfigProperties.class);
    OtlpStdoutLogRecordExporter exporter = exporterFromComponentProvider(properties);

    assertThat(exporter).extracting("wrapperJsonObject").isEqualTo(true);
    assertThat(exporter)
        .extracting("jsonWriter")
        .extracting(Object::toString)
        .isEqualTo("StreamJsonWriter{outputStream=stdout}");
  }

  private OtlpStdoutLogRecordExporter loadExporter(ConfigProperties config, String name) {
    Object provider = loadProvider(name);

    try {
      return (OtlpStdoutLogRecordExporter)
          provider
              .getClass()
              .getDeclaredMethod("createExporter", ConfigProperties.class)
              .invoke(provider, config);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Object loadProvider(String want) {
    return loadSpi(PROVIDER_CLASS)
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

  enum OutputType {
    LOGGER,
    SYSTEM_OUT,
    FILE,
    FILE_AND_BUFFERED_WRITER
  }

  static class TestCase {
    private final boolean wrapperJsonObject;
    private final OutputType outputType;

    public TestCase(OutputType outputType, boolean wrapperJsonObject) {
      this.outputType = outputType;
      this.wrapperJsonObject = wrapperJsonObject;
    }

    public OutputType getOutputType() {
      return outputType;
    }

    public boolean isWrapperJsonObject() {
      return wrapperJsonObject;
    }
  }
}
