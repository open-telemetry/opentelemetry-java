/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.export.HttpResponse;

@AutoValue
abstract class ImmutableHttpResponse implements HttpResponse {

  static ImmutableHttpResponse create(int statusCode, String statusMessage, byte[] responseBody) {
    return new AutoValue_ImmutableHttpResponse(statusCode, statusMessage, responseBody);
  }

  @SuppressWarnings("mutable")
  @Override
  public abstract byte[] getResponseBody();
}
