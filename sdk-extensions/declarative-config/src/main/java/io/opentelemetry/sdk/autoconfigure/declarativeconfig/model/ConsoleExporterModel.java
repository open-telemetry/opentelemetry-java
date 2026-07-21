/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ConsoleExporterModel {

  @Override
  public String toString() {
    return "ConsoleExporterModel{}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ConsoleExporterModel) {
      ConsoleExporterModel that = (ConsoleExporterModel) o;
      return true;
    }
    return false;
  }
}
