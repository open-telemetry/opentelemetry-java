/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.grpc.GrpcExporter;
import io.opentelemetry.exporter.internal.grpc.GrpcExporterBuilder;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.util.Collection;
import java.util.StringJoiner;
import javax.annotation.concurrent.ThreadSafe;

/** Exports profiles using OTLP via gRPC, using OpenTelemetry's protobuf model. */
@ThreadSafe
public class OtlpGrpcProfileExporter implements ProfileExporter {

  private final GrpcExporterBuilder builder;
  private final GrpcExporter delegate;

  /**
   * Returns a new {@link OtlpGrpcProfileExporter} using the default values.
   *
   * <p>To load configuration values from environment variables and system properties, use <a
   * href="https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure">opentelemetry-sdk-extension-autoconfigure</a>.
   *
   * @return a new {@link OtlpGrpcProfileExporter} instance.
   */
  public static OtlpGrpcProfileExporter getDefault() {
    return builder().build();
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static OtlpGrpcProfilesExporterBuilder builder() {
    return new OtlpGrpcProfilesExporterBuilder();
  }

  OtlpGrpcProfileExporter(GrpcExporterBuilder builder, GrpcExporter delegate) {
    this.builder = builder;
    this.delegate = delegate;
  }

  /**
   * Returns a builder with configuration values equal to those for this exporter.
   *
   * <p>IMPORTANT: Be sure to {@link #shutdown()} this instance if it will no longer be used.
   */
  public OtlpGrpcProfilesExporterBuilder toBuilder() {
    return new OtlpGrpcProfilesExporterBuilder(builder.copy());
  }

  /**
   * Submits all the given profiles in a single batch to the OpenTelemetry collector.
   *
   * @param profiles the list of sampled profiles to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<ProfileData> profiles) {
    ProfilesRequestMarshaler request = ProfilesRequestMarshaler.create(profiles);
    return delegate.export(request, profiles.size());
  }

  /**
   * The OTLP exporter does not batch items, so this method will immediately return with success.
   *
   * @return always Success
   */
  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  /**
   * Initiates an orderly shutdown in which preexisting calls continue but new calls are immediately
   * cancelled.
   */
  @Override
  public CompletableResultCode shutdown() {
    return delegate.shutdown();
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ", "OtlpGrpcProfilesExporter{", "}");
    joiner.add(builder.toString(false));
    return joiner.toString();
  }
}
