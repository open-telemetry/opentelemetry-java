/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigTestUtil.createTempFileWithContent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.ExtendedOpenTelemetrySdk;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TracerProviderModel;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateEncodingException;
import java.util.Collections;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.event.Level;

class DeclarativeConfigurationCreateTest {

  @RegisterExtension
  static final SelfSignedCertificateExtension serverTls = new SelfSignedCertificateExtension();

  @RegisterExtension
  static final SelfSignedCertificateExtension clientTls = new SelfSignedCertificateExtension();

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  @RegisterExtension
  LogCapturer logCapturer =
      LogCapturer.create().captureForLogger(DeclarativeConfiguration.class.getName(), Level.TRACE);

  /**
   * Verify each example in <a
   * href="https://github.com/open-telemetry/opentelemetry-configuration/tree/main/examples">open-telemetry/opentelemetry-configuration/examples</a>
   * can pass {@link DeclarativeConfiguration#parseAndCreate(InputStream)}.
   */
  @Test
  void parseAndCreate_Examples(@TempDir Path tempDir)
      throws IOException, CertificateEncodingException {
    // Write certificates to temp files
    String certificatePath =
        createTempFileWithContent(
            tempDir, "certificate.cert", serverTls.certificate().getEncoded());
    String clientKeyPath =
        createTempFileWithContent(tempDir, "clientKey.key", clientTls.privateKey().getEncoded());
    String clientCertificatePath =
        createTempFileWithContent(
            tempDir, "clientCertificate.cert", clientTls.certificate().getEncoded());

    File examplesDir = new File(System.getenv("CONFIG_EXAMPLE_DIR"));
    assertThat(examplesDir).isDirectory();

    for (File example : Objects.requireNonNull(examplesDir.listFiles())) {
      // Rewrite references to cert files in examples
      String exampleContent =
          new String(Files.readAllBytes(example.toPath()), StandardCharsets.UTF_8);
      String rewrittenExampleContent =
          exampleContent
              .replaceAll(
                  "certificate_file: .*\n",
                  "certificate_file: "
                      + certificatePath.replace("\\", "\\\\")
                      + System.lineSeparator())
              .replaceAll(
                  "client_key_file: .*\n",
                  "client_key_file: "
                      + clientKeyPath.replace("\\", "\\\\")
                      + System.lineSeparator())
              .replaceAll(
                  "client_certificate_file: .*\n",
                  "client_certificate_file: "
                      + clientCertificatePath.replace("\\", "\\\\")
                      + System.lineSeparator());
      InputStream is =
          new ByteArrayInputStream(rewrittenExampleContent.getBytes(StandardCharsets.UTF_8));

      // Verify that file can be parsed and interpreted without error
      assertThatCode(() -> cleanup.addCloseable(DeclarativeConfiguration.parseAndCreate(is)))
          .as("Example file: " + example.getName())
          .doesNotThrowAnyException();
    }
  }

  @Test
  void parseAndCreate_Exception_CleansUpPartials() {
    // Trigger an exception after some components have been configured by adding a valid batch
    // exporter with OTLP exporter, following by invalid batch exporter which references invalid
    // exporter "foo".
    String yaml =
        "file_format: \"1.0-rc.1\"\n"
            + "logger_provider:\n"
            + "  processors:\n"
            + "    - batch:\n"
            + "        exporter:\n"
            + "          otlp_http: {}\n"
            + "    - batch:\n"
            + "        exporter:\n"
            + "          foo: {}\n";

    assertThatThrownBy(
            () ->
                DeclarativeConfiguration.parseAndCreate(
                    new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage(
            "No component provider detected for io.opentelemetry.sdk.logs.export.LogRecordExporter with name \"foo\".");
    logCapturer.assertContains(
        "Error encountered interpreting model. Closing partially configured components.");
    logCapturer.assertContains(
        "Closing io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter");
    logCapturer.assertContains("Closing io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor");
  }

  @Test
  void parseAndCreate_EmptyComponentProviderConfig() {
    String yaml =
        "file_format: \"1.0-rc.1\"\n"
            + "logger_provider:\n"
            + "  processors:\n"
            + "    - test:\n"
            + "tracer_provider:\n"
            + "  processors:\n"
            + "    - test:\n";

    assertThatCode(
            () ->
                DeclarativeConfiguration.parseAndCreate(
                    new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))))
        .doesNotThrowAnyException();
  }

  @Test
  void create_ModelCustomizer() {
    OpenTelemetryConfigurationModel model = new OpenTelemetryConfigurationModel();
    model.withFileFormat("1.0-rc.1");
    model.withTracerProvider(
        new TracerProviderModel()
            .withProcessors(
                Collections.singletonList(
                    new SpanProcessorModel().withAdditionalProperty("test", null))));
    ExtendedOpenTelemetrySdk sdk =
        DeclarativeConfiguration.create(
            model,
            // customizer is TestDeclarativeConfigurationCustomizerProvider
            ComponentLoader.forClassLoader(
                DeclarativeConfigurationCreateTest.class.getClassLoader()));
    assertThat(sdk.toString())
        .contains(
            "resource=Resource{schemaUrl=null, attributes={"
                + "color=\"blue\", "
                + "foo=\"bar\", "
                + "service.name=\"unknown_service:java\", "
                + "telemetry.sdk.language=\"java\", "
                + "telemetry.sdk.name=\"opentelemetry\", "
                + "telemetry.sdk.version=\"");
  }

  @Test
  void callAutoConfigureListeners_exceptionIsCaught() {
    SpiHelper spiHelper = mock(SpiHelper.class);
    when(spiHelper.getListeners())
        .thenReturn(
            Collections.singleton(
                sdk -> {
                  throw new RuntimeException("Test exception from AutoConfigureListener");
                }));

    assertThatCode(
            () ->
                DeclarativeConfiguration.callAutoConfigureListeners(
                    spiHelper, OpenTelemetrySdk.builder().build()))
        .doesNotThrowAnyException();
  }
}
