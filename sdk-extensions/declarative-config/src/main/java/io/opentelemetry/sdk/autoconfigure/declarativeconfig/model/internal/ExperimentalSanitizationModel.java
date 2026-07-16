/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"url"})
@Generated("jsonschema2pojo")
public class ExperimentalSanitizationModel {

  @Nullable private ExperimentalUrlSanitizationModel url;

  /**
   * Configure URL sanitization options.
   *
   * <p>If omitted, defaults as described in ExperimentalUrlSanitization are used.
   */
  @JsonProperty("url")
  @Nullable
  public ExperimentalUrlSanitizationModel getUrl() {
    return url;
  }

  @JsonProperty("url")
  public ExperimentalSanitizationModel withUrl(ExperimentalUrlSanitizationModel url) {
    this.url = url;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalSanitizationModel{" + "url=" + url + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.url == null) ? 0 : this.url.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalSanitizationModel) {
      ExperimentalSanitizationModel that = (ExperimentalSanitizationModel) o;
      return (this.url == null ? that.url == null : this.url.equals(that.url));
    }
    return false;
  }
}
