/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.jfr;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.profiles.OtlpGrpcProfileExporter;
import io.opentelemetry.exporter.otlp.profiles.OtlpGrpcProfilesExporterBuilder;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.profiles.ProfileExporter;
import io.opentelemetry.sdk.profiles.data.ProfileData;
import io.opentelemetry.sdk.profiles.internal.data.ImmutableProfileData;
import io.opentelemetry.sdk.profiles.internal.data.ImmutableValueTypeData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import jdk.jfr.consumer.RecordingFile;

/**
 * Simple example of how to wire up the profiles signal OTLP exporter to convert and send the
 * content of a JFR recording file. This is not a supported CLI and is not intended to be
 * configurable by e.g. command line flags.
 */
public class JfrExportExample {

  private JfrExportExample() {}

  @SuppressWarnings("SystemOut")
  public static void main(String[] args) throws IOException {

    Path jfrFilePath = Path.of("/tmp/demo.jfr"); // TODO set the JFR file location here
    ProfileData profileData = convertJfrFile(jfrFilePath);

    // for test purposes https://github.com/elastic/devfiler/ provides a handy standalone backend.
    // by default devfiler listens on port 11000
    String destination = "127.0.0.1:11000"; // TODO set the location of the backend receiver here

    OtlpGrpcProfilesExporterBuilder exporterBuilder = OtlpGrpcProfileExporter.builder();
    exporterBuilder.setEndpoint("http://" + destination);
    ProfileExporter exporter = exporterBuilder.build();

    CompletableResultCode completableResultCode = exporter.export(List.of(profileData));
    completableResultCode.join(1, TimeUnit.MINUTES);
    System.out.println(completableResultCode.isSuccess() ? "success" : "failure");
  }

  /**
   * Read the content of the JFR recording file and convert it to a ProfileData object in
   * preparation for OTLP export.
   *
   * @param jfrFilePath the data source.
   * @return a ProfileData object constructed from the JFR recording.
   * @throws IOException if the conversion fails.
   */
  public static ProfileData convertJfrFile(Path jfrFilePath) throws IOException {

    JfrExecutionSampleEventConverter converter = new JfrExecutionSampleEventConverter();

    RecordingFile recordingFile = new RecordingFile(jfrFilePath);
    while (recordingFile.hasMoreEvents()) {
      converter.accept(recordingFile.readEvent());
    }
    recordingFile.close();

    String profileId = "0123456789abcdef0123456789abcdef";
    InstrumentationScopeInfo scopeInfo =
        InstrumentationScopeInfo.builder("testLib")
            .setVersion("1.0")
            .setSchemaUrl("http://url")
            .build();

    return ImmutableProfileData.create(
        Resource.create(Attributes.empty()),
        scopeInfo,
        converter.getProfilesDictionaryCompositor().getProfileDictionaryData(),
        ImmutableValueTypeData.create(0, 0),
        converter.getSamples(),
        0,
        0,
        ImmutableValueTypeData.create(0, 0),
        0,
        profileId,
        0,
        "format",
        ByteBuffer.wrap(Files.readAllBytes(jfrFilePath)),
        Collections.emptyList());
  }
}
