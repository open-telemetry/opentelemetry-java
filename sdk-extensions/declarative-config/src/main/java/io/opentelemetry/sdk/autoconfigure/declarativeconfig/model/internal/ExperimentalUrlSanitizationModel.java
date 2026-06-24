/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"sensitive_query_parameters"})
@Generated("jsonschema2pojo")
public class ExperimentalUrlSanitizationModel {

  /**
   * List of query parameter names whose values should be redacted from URLs. Query parameter names
   * are case-sensitive. This is a full override of the default sensitive query parameter keys, it
   * is not a list of keys in addition to the defaults. Set to an empty array to disable query
   * parameter redaction. If omitted, the default sensitive query parameter list as defined by the
   * url semantic conventions
   * (https://github.com/open-telemetry/semantic-conventions/blob/main/docs/registry/attributes/url.md)
   * is used.
   */
  @JsonProperty("sensitive_query_parameters")
  @JsonPropertyDescription(
      "List of query parameter names whose values should be redacted from URLs.\nQuery parameter names are case-sensitive.\nThis is a full override of the default sensitive query parameter keys, it is not a list of keys in addition to the defaults.\nSet to an empty array to disable query parameter redaction.\nIf omitted, the default sensitive query parameter list as defined by the url semantic conventions (https://github.com/open-telemetry/semantic-conventions/blob/main/docs/registry/attributes/url.md) is used.\n")
  @Nullable
  private List<String> sensitiveQueryParameters;

  /**
   * List of query parameter names whose values should be redacted from URLs. Query parameter names
   * are case-sensitive. This is a full override of the default sensitive query parameter keys, it
   * is not a list of keys in addition to the defaults. Set to an empty array to disable query
   * parameter redaction. If omitted, the default sensitive query parameter list as defined by the
   * url semantic conventions
   * (https://github.com/open-telemetry/semantic-conventions/blob/main/docs/registry/attributes/url.md)
   * is used.
   */
  @JsonProperty("sensitive_query_parameters")
  @Nullable
  public List<String> getSensitiveQueryParameters() {
    return sensitiveQueryParameters;
  }

  public ExperimentalUrlSanitizationModel withSensitiveQueryParameters(
      List<String> sensitiveQueryParameters) {
    this.sensitiveQueryParameters = sensitiveQueryParameters;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalUrlSanitizationModel{"
        + "sensitiveQueryParameters="
        + sensitiveQueryParameters
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.sensitiveQueryParameters == null) ? 0 : this.sensitiveQueryParameters.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalUrlSanitizationModel) {
      ExperimentalUrlSanitizationModel that = (ExperimentalUrlSanitizationModel) o;
      return (this.sensitiveQueryParameters == null
          ? that.sensitiveQueryParameters == null
          : this.sensitiveQueryParameters.equals(that.sensitiveQueryParameters));
    }
    return false;
  }
}
