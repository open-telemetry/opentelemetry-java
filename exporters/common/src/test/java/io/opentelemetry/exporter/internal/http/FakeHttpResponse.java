/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.http;

import java.io.IOException;

class FakeHttpResponse implements HttpSender.Response {

  final int statusCode;
  final String statusMessage;

  FakeHttpResponse(int statusCode, String statusMessage) {
    this.statusCode = statusCode;
    this.statusMessage = statusMessage;
  }

  @Override
  public int statusCode() {
    return statusCode;
  }

  @Override
  public String statusMessage() {
    return statusMessage;
  }

  @Override
  public byte[] responseBody() throws IOException {
    return new byte[0];
  }
}
