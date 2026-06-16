/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"request_captured_headers", "response_captured_headers", "known_methods"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalHttpClientInstrumentationModel {

  /**
   * Configure headers to capture for outbound http requests. If omitted, no outbound request
   * headers are captured.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("request_captured_headers")
  @JsonPropertyDescription(
      "Configure headers to capture for outbound http requests.\nIf omitted, no outbound request headers are captured.\n")
  private List<String> requestCapturedHeaders;

  /**
   * Configure headers to capture for inbound http responses. If omitted, no inbound response
   * headers are captured.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("response_captured_headers")
  @JsonPropertyDescription(
      "Configure headers to capture for inbound http responses.\nIf omitted, no inbound response headers are captured.\n")
  private List<String> responseCapturedHeaders;

  /**
   * Override the default list of known HTTP methods. Known methods are case-sensitive. This is a
   * full override of the default known methods, not a list of known methods in addition to the
   * defaults. If omitted, HTTP methods GET, HEAD, POST, PUT, DELETE, CONNECT, OPTIONS, TRACE, PATCH
   * are known.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("known_methods")
  @JsonPropertyDescription(
      "Override the default list of known HTTP methods.\nKnown methods are case-sensitive.\nThis is a full override of the default known methods, not a list of known methods in addition to the defaults.\nIf omitted, HTTP methods GET, HEAD, POST, PUT, DELETE, CONNECT, OPTIONS, TRACE, PATCH are known.\n")
  private List<String> knownMethods;

  /**
   * Configure headers to capture for outbound http requests. If omitted, no outbound request
   * headers are captured.
   */
  @JsonProperty("request_captured_headers")
  @Nullable
  public List<String> getRequestCapturedHeaders() {
    return requestCapturedHeaders;
  }

  public ExperimentalHttpClientInstrumentationModel withRequestCapturedHeaders(
      List<String> requestCapturedHeaders) {
    this.requestCapturedHeaders = requestCapturedHeaders;
    return this;
  }

  /**
   * Configure headers to capture for inbound http responses. If omitted, no inbound response
   * headers are captured.
   */
  @JsonProperty("response_captured_headers")
  @Nullable
  public List<String> getResponseCapturedHeaders() {
    return responseCapturedHeaders;
  }

  public ExperimentalHttpClientInstrumentationModel withResponseCapturedHeaders(
      List<String> responseCapturedHeaders) {
    this.responseCapturedHeaders = responseCapturedHeaders;
    return this;
  }

  /**
   * Override the default list of known HTTP methods. Known methods are case-sensitive. This is a
   * full override of the default known methods, not a list of known methods in addition to the
   * defaults. If omitted, HTTP methods GET, HEAD, POST, PUT, DELETE, CONNECT, OPTIONS, TRACE, PATCH
   * are known.
   */
  @JsonProperty("known_methods")
  @Nullable
  public List<String> getKnownMethods() {
    return knownMethods;
  }

  public ExperimentalHttpClientInstrumentationModel withKnownMethods(List<String> knownMethods) {
    this.knownMethods = knownMethods;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalHttpClientInstrumentationModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("requestCapturedHeaders");
    sb.append('=');
    sb.append(((this.requestCapturedHeaders == null) ? "<null>" : this.requestCapturedHeaders));
    sb.append(',');
    sb.append("responseCapturedHeaders");
    sb.append('=');
    sb.append(((this.responseCapturedHeaders == null) ? "<null>" : this.responseCapturedHeaders));
    sb.append(',');
    sb.append("knownMethods");
    sb.append('=');
    sb.append(((this.knownMethods == null) ? "<null>" : this.knownMethods));
    sb.append(',');
    if (sb.charAt((sb.length() - 1)) == ',') {
      sb.setCharAt((sb.length() - 1), ']');
    } else {
      sb.append(']');
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int result = 1;
    result =
        ((result * 31)
            + ((this.requestCapturedHeaders == null) ? 0 : this.requestCapturedHeaders.hashCode()));
    result =
        ((result * 31)
            + ((this.responseCapturedHeaders == null)
                ? 0
                : this.responseCapturedHeaders.hashCode()));
    result = ((result * 31) + ((this.knownMethods == null) ? 0 : this.knownMethods.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalHttpClientInstrumentationModel) == false) {
      return false;
    }
    ExperimentalHttpClientInstrumentationModel rhs =
        ((ExperimentalHttpClientInstrumentationModel) other);
    return ((((this.requestCapturedHeaders == rhs.requestCapturedHeaders)
                || ((this.requestCapturedHeaders != null)
                    && this.requestCapturedHeaders.equals(rhs.requestCapturedHeaders)))
            && ((this.responseCapturedHeaders == rhs.responseCapturedHeaders)
                || ((this.responseCapturedHeaders != null)
                    && this.responseCapturedHeaders.equals(rhs.responseCapturedHeaders))))
        && ((this.knownMethods == rhs.knownMethods)
            || ((this.knownMethods != null) && this.knownMethods.equals(rhs.knownMethods))));
  }
}
