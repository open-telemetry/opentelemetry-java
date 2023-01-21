/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry;

import java.util.List;
import javax.annotation.Nullable;

public final class Request {
  @Nullable private String url;
  @Nullable private List<Request> arguments;

  @Nullable
  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @Nullable
  public List<Request> getArguments() {
    return arguments;
  }

  public void setArguments(List<Request> arguments) {
    this.arguments = arguments;
  }

  @Override
  public String toString() {
    return "Request{" + "url='" + url + '\'' + ", arguments=" + arguments + '}';
  }
}
