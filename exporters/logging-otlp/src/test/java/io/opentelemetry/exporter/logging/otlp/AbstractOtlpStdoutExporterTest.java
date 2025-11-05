/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.common.export.MemoryMode;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
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

abstract class AbstractOtlpStdoutExporterTest<T> {

  private static PrintStream systemOut;

  private static final String TYPE = "experimental-otlp/stdout";

  private static final ByteArrayOutputStream SYSTEM_OUT_STREAM = new ByteArrayOutputStream();
  private static final PrintStream SYSTEM_OUT_PRINT_STREAM = new PrintStream(SYSTEM_OUT_STREAM);

  @RegisterExtension LogCapturer logs;
  private int skipLogs;
  private final String defaultConfigString;
  private final TestDataExporter<? super T> testDataExporter;
  protected final Class<?> exporterClass;
  private final Class<?> providerClass;
  private final Class<?> componentProviderType;

  @TempDir Path tempDir;

  public AbstractOtlpStdoutExporterTest(
      TestDataExporter<? super T> testDataExporter,
      Class<?> exporterClass,
      Class<?> providerClass,
      Class<?> componentProviderType,
      String defaultConfigString) {
    this.testDataExporter = testDataExporter;
    this.exporterClass = exporterClass;
    this.providerClass = providerClass;
    logs = LogCapturer.create().captureForType(exporterClass);
    this.defaultConfigString = defaultConfigString;
    this.componentProviderType = componentProviderType;
  }

  protected abstract T createExporter(
      @Nullable OutputStream outputStream, MemoryMode memoryMode, boolean wrapperJsonObject);

  protected abstract T createDefaultExporter();

  private String output(@Nullable OutputStream outputStream, @Nullable Path file) {
    if (outputStream == null) {
      return logs.getEvents().stream()
          .skip(skipLogs)
          .map(LoggingEvent::getMessage)
          .reduce("", (a, b) -> a + b + "\n")
          .trim();
    }

    if (file != null) {
      try {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8).trim();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    try {
      return SYSTEM_OUT_STREAM.toString(StandardCharsets.UTF_8.name()).trim();
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

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

  enum OutputType {
    LOGGER,
    SYSTEM_OUT,
    FILE,
    FILE_AND_BUFFERED_WRITER
  }

  public static class TestCase {
    private final MemoryMode memoryMode;
    private final boolean wrapperJsonObject;
    private final OutputType outputType;

    public TestCase(OutputType outputType, MemoryMode memoryMode, boolean wrapperJsonObject) {
      this.outputType = outputType;
      this.memoryMode = memoryMode;
      this.wrapperJsonObject = wrapperJsonObject;
    }

    public OutputType getOutputType() {
      return outputType;
    }

    public boolean isWrapperJsonObject() {
      return wrapperJsonObject;
    }

    public MemoryMode getMemoryMode() {
      return memoryMode;
    }
  }

  static Stream<Arguments> exportTestCases() {
    return ImmutableList.of(
        testCase(OutputType.SYSTEM_OUT, MemoryMode.IMMUTABLE_DATA, /* wrapperJsonObject= */ true),
        testCase(OutputType.SYSTEM_OUT, MemoryMode.IMMUTABLE_DATA, /* wrapperJsonObject= */ false),
        testCase(OutputType.FILE, MemoryMode.IMMUTABLE_DATA, /* wrapperJsonObject= */ true),
        testCase(OutputType.FILE, MemoryMode.IMMUTABLE_DATA, /* wrapperJsonObject= */ false),
        testCase(
            OutputType.FILE_AND_BUFFERED_WRITER,
            MemoryMode.IMMUTABLE_DATA,
            /* wrapperJsonObject= */ true),
        testCase(
            OutputType.FILE_AND_BUFFERED_WRITER,
            MemoryMode.IMMUTABLE_DATA,
            /* wrapperJsonObject= */ false),
        testCase(OutputType.LOGGER, MemoryMode.IMMUTABLE_DATA, /* wrapperJsonObject= */ true),
        testCase(OutputType.LOGGER, MemoryMode.IMMUTABLE_DATA, /* wrapperJsonObject= */ false),
        testCase(OutputType.SYSTEM_OUT, MemoryMode.REUSABLE_DATA, /* wrapperJsonObject= */ true),
        testCase(OutputType.SYSTEM_OUT, MemoryMode.REUSABLE_DATA, /* wrapperJsonObject= */ false),
        testCase(OutputType.FILE, MemoryMode.REUSABLE_DATA, /* wrapperJsonObject= */ true),
        testCase(OutputType.FILE, MemoryMode.REUSABLE_DATA, /* wrapperJsonObject= */ false),
        testCase(
            OutputType.FILE_AND_BUFFERED_WRITER,
            MemoryMode.REUSABLE_DATA,
            /* wrapperJsonObject= */ true),
        testCase(
            OutputType.FILE_AND_BUFFERED_WRITER,
            MemoryMode.REUSABLE_DATA,
            /* wrapperJsonObject= */ false),
        testCase(OutputType.LOGGER, MemoryMode.REUSABLE_DATA, /* wrapperJsonObject= */ true),
        testCase(OutputType.LOGGER, MemoryMode.REUSABLE_DATA, /* wrapperJsonObject= */ false))
        .stream();
  }

  private static Arguments testCase(
      OutputType type, MemoryMode memoryMode, boolean wrapperJsonObject) {
    return Arguments.of(
        "output="
            + type
            + ", wrapperJsonObject="
            + wrapperJsonObject
            + ", memoryMode="
            + memoryMode,
        new TestCase(type, memoryMode, wrapperJsonObject));
  }

  @SuppressWarnings("SystemOut")
  @ParameterizedTest(name = "{0}")
  @MethodSource("exportTestCases")
  void exportWithProgrammaticConfig(String name, TestCase testCase) throws Exception {
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

    Supplier<T> exporter =
        () ->
            createExporter(outputStream, testCase.getMemoryMode(), testCase.isWrapperJsonObject());

    if (testCase.getMemoryMode() == MemoryMode.REUSABLE_DATA && !testCase.isWrapperJsonObject()) {
      assertThatExceptionOfType(IllegalArgumentException.class)
          .isThrownBy(exporter::get)
          .withMessage("Reusable data mode is not supported without wrapperJsonObject");
      return;
    }

    testDataExporter.export(exporter.get());

    String output = output(outputStream, file);
    String expectedJson = testDataExporter.getExpectedJson(testCase.isWrapperJsonObject());
    JSONAssert.assertEquals("Got \n" + output, expectedJson, output, false);

    if (testCase.isWrapperJsonObject()) {
      assertThat(output).doesNotContain("\n");
    }

    if (file == null) {
      // no need to test again for file - and it's not working with files
      assertDoubleOutput(exporter, expectedJson, outputStream);
    }
  }

  private void assertDoubleOutput(
      Supplier<T> exporter, String expectedJson, @Nullable OutputStream outputStream)
      throws Exception {
    SYSTEM_OUT_STREAM.reset();
    skipLogs = logs.getEvents().size();
    testDataExporter.export(exporter.get());
    testDataExporter.export(exporter.get());

    String[] lines = output(outputStream, null).split("\n");
    assertThat(lines).hasSize(2);
    for (String line : lines) {
      JSONAssert.assertEquals("Got \n" + line, expectedJson, line, false);
    }
  }

  @Test
  void testShutdown() {
    T exporter = createDefaultExporter();
    assertThat(testDataExporter.shutdown(exporter).isSuccess()).isTrue();
    assertThat(testDataExporter.export(exporter).join(10, TimeUnit.SECONDS).isSuccess()).isFalse();
    assertThat(testDataExporter.flush(exporter).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    assertThat(output(null, null)).isEmpty();
    assertThat(testDataExporter.shutdown(exporter).isSuccess()).isTrue();
    logs.assertContains("Calling shutdown() multiple times.");
  }

  @Test
  void defaultToString() {
    assertThat(createDefaultExporter()).hasToString(defaultConfigString);

    assertThat(exporterFromProvider(DefaultConfigProperties.createFromMap(emptyMap())))
        .hasToString(defaultConfigString);
  }

  @Test
  void providerConfig() {
    assertThat(
            exporterFromProvider(
                DefaultConfigProperties.createFromMap(
                    singletonMap("otel.java.exporter.memory_mode", "immutable_data"))))
        .extracting("memoryMode")
        .isEqualTo(MemoryMode.IMMUTABLE_DATA);
    assertThat(
            exporterFromProvider(
                DefaultConfigProperties.createFromMap(
                    singletonMap("otel.java.exporter.memory_mode", "reusable_data"))))
        .extracting("memoryMode")
        .isEqualTo(MemoryMode.REUSABLE_DATA);
  }

  @Test
  void componentProviderConfig() {
    DeclarativeConfigProperties properties = spy(DeclarativeConfigProperties.empty());
    T exporter = exporterFromComponentProvider(properties);

    assertThat(exporter).extracting("wrapperJsonObject").isEqualTo(true);
    assertThat(exporter).extracting("memoryMode").isEqualTo(MemoryMode.IMMUTABLE_DATA);
    assertThat(exporter)
        .extracting("jsonWriter")
        .extracting(Object::toString)
        .isEqualTo("StreamJsonWriter{outputStream=stdout}");

    when(properties.getString("memory_mode")).thenReturn("IMMUTABLE_DATA");
    assertThat(exporterFromComponentProvider(properties))
        .extracting("memoryMode")
        .isEqualTo(MemoryMode.IMMUTABLE_DATA);

    when(properties.getString("memory_mode")).thenReturn("REUSABLE_DATA");
    assertThat(exporterFromComponentProvider(properties))
        .extracting("memoryMode")
        .isEqualTo(MemoryMode.REUSABLE_DATA);
  }

  @SuppressWarnings("unchecked")
  protected T exporterFromComponentProvider(DeclarativeConfigProperties properties) {
    return (T)
        StreamSupport.stream(
                properties.getComponentLoader().load(ComponentProvider.class).spliterator(), false)
            .filter(
                p -> {
                  return "otlp_file/development".equals(p.getName())
                      && p.getType().equals(componentProviderType);
                })
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No provider found"))
            .create(properties);
  }

  @SuppressWarnings("unchecked")
  protected T exporterFromProvider(ConfigProperties config) {
    Object provider = loadProvider(config);

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

  private Object loadProvider(ConfigProperties config) {
    return StreamSupport.stream(
            config.getComponentLoader().load(providerClass).spliterator(), false)
        .filter(
            p -> {
              try {
                return AbstractOtlpStdoutExporterTest.TYPE.equals(
                    p.getClass().getDeclaredMethod("getName").invoke(p));
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            })
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No provider found"));
  }
}
