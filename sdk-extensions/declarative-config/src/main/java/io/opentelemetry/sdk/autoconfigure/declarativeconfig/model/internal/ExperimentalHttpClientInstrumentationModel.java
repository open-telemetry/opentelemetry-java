/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"request_captured_headers", "response_captured_headers", "known_methods"})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ExperimentalHttpClientInstrumentationModel {

  @Nullable private List<String> requestCapturedHeaders;
  @Nullable private List<String> responseCapturedHeaders;
  @Nullable private List<String> knownMethods;

  /**
   * Configure headers to capture for outbound http requests.
   *
   * <p>If omitted, no outbound request headers are captured.
   */
  @JsonProperty("request_captured_headers")
  @Nullable
  public List<String> getRequestCapturedHeaders() {
    return requestCapturedHeaders;
  }

  @JsonProperty("request_captured_headers")
  public ExperimentalHttpClientInstrumentationModel withRequestCapturedHeaders(
      List<String> requestCapturedHeaders) {
    this.requestCapturedHeaders = requestCapturedHeaders;
    return this;
  }

  /**
   * Configure headers to capture for inbound http responses.
   *
   * <p>If omitted, no inbound response headers are captured.
   */
  @JsonProperty("response_captured_headers")
  @Nullable
  public List<String> getResponseCapturedHeaders() {
    return responseCapturedHeaders;
  }

  @JsonProperty("response_captured_headers")
  public ExperimentalHttpClientInstrumentationModel withResponseCapturedHeaders(
      List<String> responseCapturedHeaders) {
    this.responseCapturedHeaders = responseCapturedHeaders;
    return this;
  }

  /**
   * Override the default list of known HTTP methods.
   *
   * <p>Known methods are case-sensitive.
   *
   * <p>This is a full override of the default known methods, not a list of known methods in
   * addition to the defaults.
   *
   * <p>If omitted, HTTP methods GET, HEAD, POST, PUT, DELETE, CONNECT, OPTIONS, TRACE, PATCH are
   * known.
   */
  @JsonProperty("known_methods")
  @Nullable
  public List<String> getKnownMethods() {
    return knownMethods;
  }

  @JsonProperty("known_methods")
  public ExperimentalHttpClientInstrumentationModel withKnownMethods(List<String> knownMethods) {
    this.knownMethods = knownMethods;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalHttpClientInstrumentationModel{"
        + "requestCapturedHeaders="
        + requestCapturedHeaders
        + ", responseCapturedHeaders="
        + responseCapturedHeaders
        + ", knownMethods="
        + knownMethods
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.requestCapturedHeaders == null) ? 0 : this.requestCapturedHeaders.hashCode();
    h *= 1000003;
    h ^= (this.responseCapturedHeaders == null) ? 0 : this.responseCapturedHeaders.hashCode();
    h *= 1000003;
    h ^= (this.knownMethods == null) ? 0 : this.knownMethods.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalHttpClientInstrumentationModel) {
      ExperimentalHttpClientInstrumentationModel that =
          (ExperimentalHttpClientInstrumentationModel) o;
      return (this.requestCapturedHeaders == null
              ? that.requestCapturedHeaders == null
              : this.requestCapturedHeaders.equals(that.requestCapturedHeaders))
          && (this.responseCapturedHeaders == null
              ? that.responseCapturedHeaders == null
              : this.responseCapturedHeaders.equals(that.responseCapturedHeaders))
          && (this.knownMethods == null
              ? that.knownMethods == null
              : this.knownMethods.equals(that.knownMethods));
    }
    return false;
  }
}
