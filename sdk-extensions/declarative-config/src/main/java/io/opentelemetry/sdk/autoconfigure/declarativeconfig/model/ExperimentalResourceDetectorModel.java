/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"container", "host", "process", "service"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalResourceDetectorModel {

  /** (Can be null) */
  @Nullable
  @JsonProperty("container")
  private ExperimentalContainerResourceDetectorModel container;

  /** (Can be null) */
  @Nullable
  @JsonProperty("host")
  private ExperimentalHostResourceDetectorModel host;

  /** (Can be null) */
  @Nullable
  @JsonProperty("process")
  private ExperimentalProcessResourceDetectorModel process;

  /** (Can be null) */
  @Nullable
  @JsonProperty("service")
  private ExperimentalServiceResourceDetectorModel service;

  @JsonIgnore
  private Map<String, ExperimentalResourceDetectorPropertyModel> additionalProperties =
      new LinkedHashMap<String, ExperimentalResourceDetectorPropertyModel>();

  @JsonProperty("container")
  @Nullable
  public ExperimentalContainerResourceDetectorModel getContainer() {
    return container;
  }

  public ExperimentalResourceDetectorModel withContainer(
      ExperimentalContainerResourceDetectorModel container) {
    this.container = container;
    return this;
  }

  @JsonProperty("host")
  @Nullable
  public ExperimentalHostResourceDetectorModel getHost() {
    return host;
  }

  public ExperimentalResourceDetectorModel withHost(ExperimentalHostResourceDetectorModel host) {
    this.host = host;
    return this;
  }

  @JsonProperty("process")
  @Nullable
  public ExperimentalProcessResourceDetectorModel getProcess() {
    return process;
  }

  public ExperimentalResourceDetectorModel withProcess(
      ExperimentalProcessResourceDetectorModel process) {
    this.process = process;
    return this;
  }

  @JsonProperty("service")
  @Nullable
  public ExperimentalServiceResourceDetectorModel getService() {
    return service;
  }

  public ExperimentalResourceDetectorModel withService(
      ExperimentalServiceResourceDetectorModel service) {
    this.service = service;
    return this;
  }

  @JsonAnyGetter
  public Map<String, ExperimentalResourceDetectorPropertyModel> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, ExperimentalResourceDetectorPropertyModel value) {
    this.additionalProperties.put(name, value);
  }

  public ExperimentalResourceDetectorModel withAdditionalProperty(
      String name, ExperimentalResourceDetectorPropertyModel value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalResourceDetectorModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("container");
    sb.append('=');
    sb.append(((this.container == null) ? "<null>" : this.container));
    sb.append(',');
    sb.append("host");
    sb.append('=');
    sb.append(((this.host == null) ? "<null>" : this.host));
    sb.append(',');
    sb.append("process");
    sb.append('=');
    sb.append(((this.process == null) ? "<null>" : this.process));
    sb.append(',');
    sb.append("service");
    sb.append('=');
    sb.append(((this.service == null) ? "<null>" : this.service));
    sb.append(',');
    sb.append("additionalProperties");
    sb.append('=');
    sb.append(((this.additionalProperties == null) ? "<null>" : this.additionalProperties));
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
    result = ((result * 31) + ((this.container == null) ? 0 : this.container.hashCode()));
    result = ((result * 31) + ((this.host == null) ? 0 : this.host.hashCode()));
    result = ((result * 31) + ((this.process == null) ? 0 : this.process.hashCode()));
    result =
        ((result * 31)
            + ((this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode()));
    result = ((result * 31) + ((this.service == null) ? 0 : this.service.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalResourceDetectorModel) == false) {
      return false;
    }
    ExperimentalResourceDetectorModel rhs = ((ExperimentalResourceDetectorModel) other);
    return ((((((this.container == rhs.container)
                        || ((this.container != null) && this.container.equals(rhs.container)))
                    && ((this.host == rhs.host)
                        || ((this.host != null) && this.host.equals(rhs.host))))
                && ((this.process == rhs.process)
                    || ((this.process != null) && this.process.equals(rhs.process))))
            && ((this.additionalProperties == rhs.additionalProperties)
                || ((this.additionalProperties != null)
                    && this.additionalProperties.equals(rhs.additionalProperties))))
        && ((this.service == rhs.service)
            || ((this.service != null) && this.service.equals(rhs.service))));
  }
}
