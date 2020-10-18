/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry;

import java.util.Arrays;

public class Request {
  private String url;
  private Request[] arguments;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Request[] getArguments() {
    return arguments;
  }

  public void setArguments(Request[] arguments) {
    this.arguments = arguments;
  }

  @Override
  public String toString() {
    return "Request{" + "url='" + url + '\'' + ", arguments=" + Arrays.toString(arguments) + '}';
  }
}
