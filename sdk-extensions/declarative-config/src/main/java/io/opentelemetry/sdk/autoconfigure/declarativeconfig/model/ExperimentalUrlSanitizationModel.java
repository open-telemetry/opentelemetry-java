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
@JsonPropertyOrder({"sensitive_query_parameters"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalUrlSanitizationModel {

  /**
   * List of query parameter names whose values should be redacted from URLs. Query parameter names
   * are case-sensitive. This is a full override of the default sensitive query parameter keys, it
   * is not a list of keys in addition to the defaults. Set to an empty array to disable query
   * parameter redaction. If omitted, the default sensitive query parameter list as defined by the
   * url semantic conventions
   * (https://github.com/open-telemetry/semantic-conventions/blob/main/docs/registry/attributes/url.md)
   * is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("sensitive_query_parameters")
  @JsonPropertyDescription(
      "List of query parameter names whose values should be redacted from URLs.\nQuery parameter names are case-sensitive.\nThis is a full override of the default sensitive query parameter keys, it is not a list of keys in addition to the defaults.\nSet to an empty array to disable query parameter redaction.\nIf omitted, the default sensitive query parameter list as defined by the url semantic conventions (https://github.com/open-telemetry/semantic-conventions/blob/main/docs/registry/attributes/url.md) is used.\n")
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
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalUrlSanitizationModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("sensitiveQueryParameters");
    sb.append('=');
    sb.append(((this.sensitiveQueryParameters == null) ? "<null>" : this.sensitiveQueryParameters));
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
            + ((this.sensitiveQueryParameters == null)
                ? 0
                : this.sensitiveQueryParameters.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalUrlSanitizationModel) == false) {
      return false;
    }
    ExperimentalUrlSanitizationModel rhs = ((ExperimentalUrlSanitizationModel) other);
    return ((this.sensitiveQueryParameters == rhs.sensitiveQueryParameters)
        || ((this.sensitiveQueryParameters != null)
            && this.sensitiveQueryParameters.equals(rhs.sensitiveQueryParameters)));
  }
}
