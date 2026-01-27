/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import io.opentelemetry.sdk.common.export.GrpcResponse;
import io.opentelemetry.sdk.common.export.HttpResponse;
import javax.annotation.Nullable;

/**
 * Represents the failure of a gRPC or HTTP exporter.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public abstract class FailedExportException extends Exception {

  private static final long serialVersionUID = 6988924855140178789L;

  private FailedExportException(@Nullable Throwable cause) {
    super(cause);
  }

  /** Indicates an HTTP export failed after receiving a response from the server. */
  public static HttpExportException httpFailedWithResponse(HttpResponse response) {
    return new HttpExportException(response, null);
  }

  /** Indicates an HTTP export failed exceptionally without receiving a response from the server. */
  public static HttpExportException httpFailedExceptionally(Throwable cause) {
    return new HttpExportException(null, cause);
  }

  /** Indicates a gRPC export failed after receiving a response from the server. */
  public static GrpcExportException grpcFailedWithResponse(GrpcResponse response) {
    return new GrpcExportException(response, null);
  }

  /** Indicates a gRPC export failed exceptionally without receiving a response from the server. */
  public static GrpcExportException grpcFailedExceptionally(Throwable cause) {
    return new GrpcExportException(null, cause);
  }

  /** Returns true if the export failed with a response from the server. */
  public abstract boolean failedWithResponse();

  /**
   * Represents the failure of an HTTP exporter.
   *
   * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
   * at any time.
   */
  public static final class HttpExportException extends FailedExportException {

    private static final long serialVersionUID = -6787390183017184775L;

    @Nullable private final HttpResponse response;
    @Nullable private final Throwable cause;

    private HttpExportException(@Nullable HttpResponse response, @Nullable Throwable cause) {
      super(cause);
      this.response = response;
      this.cause = cause;
    }

    @Override
    public boolean failedWithResponse() {
      return response != null;
    }

    /**
     * Returns the response if the export failed with a response from the server, or null if the
     * export failed exceptionally with no response.
     */
    @Nullable
    public HttpResponse getResponse() {
      return response;
    }

    /**
     * Returns the exceptional cause of failure, or null if the export failed with a response from
     * the server.
     */
    @Nullable
    @Override
    public Throwable getCause() {
      return cause;
    }
  }

  /**
   * Represents the failure of a gRPC exporter.
   *
   * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
   * at any time.
   */
  public static final class GrpcExportException extends FailedExportException {

    private static final long serialVersionUID = -9157548250286695364L;

    @Nullable private final GrpcResponse response;
    @Nullable private final Throwable cause;

    private GrpcExportException(@Nullable GrpcResponse response, @Nullable Throwable cause) {
      super(cause);
      this.response = response;
      this.cause = cause;
    }

    @Override
    public boolean failedWithResponse() {
      return response != null;
    }

    /**
     * Returns the response if the export failed with a response from the server, or null if the
     * export failed exceptionally with no response.
     */
    @Nullable
    public GrpcResponse getResponse() {
      return response;
    }

    /**
     * Returns the exceptional cause of failure, or null if the export failed with a response from
     * the server.
     */
    @Nullable
    @Override
    public Throwable getCause() {
      return cause;
    }
  }
}
