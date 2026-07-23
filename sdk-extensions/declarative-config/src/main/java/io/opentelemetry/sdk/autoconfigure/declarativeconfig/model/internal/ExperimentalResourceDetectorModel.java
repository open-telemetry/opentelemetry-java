/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"container", "host", "process", "service"})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ExperimentalResourceDetectorModel {

  @Nullable private ExperimentalContainerResourceDetectorModel container;
  @Nullable private ExperimentalHostResourceDetectorModel host;
  @Nullable private ExperimentalProcessResourceDetectorModel process;
  @Nullable private ExperimentalServiceResourceDetectorModel service;
  private Map<String, ExperimentalResourceDetectorPropertyModel> additionalProperties =
      new LinkedHashMap<String, ExperimentalResourceDetectorPropertyModel>();

  /**
   * Enable the container resource detector, which populates container.* attributes.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("container")
  @Nullable
  public ExperimentalContainerResourceDetectorModel getContainer() {
    return container;
  }

  @JsonProperty("container")
  public ExperimentalResourceDetectorModel withContainer(
      ExperimentalContainerResourceDetectorModel container) {
    this.container = container;
    return this;
  }

  /**
   * Enable the host resource detector, which populates host.* and os.* attributes.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("host")
  @Nullable
  public ExperimentalHostResourceDetectorModel getHost() {
    return host;
  }

  @JsonProperty("host")
  public ExperimentalResourceDetectorModel withHost(ExperimentalHostResourceDetectorModel host) {
    this.host = host;
    return this;
  }

  /**
   * Enable the process resource detector, which populates process.* attributes.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("process")
  @Nullable
  public ExperimentalProcessResourceDetectorModel getProcess() {
    return process;
  }

  @JsonProperty("process")
  public ExperimentalResourceDetectorModel withProcess(
      ExperimentalProcessResourceDetectorModel process) {
    this.process = process;
    return this;
  }

  /**
   * Enable the service detector, which populates service.name based on the OTEL_SERVICE_NAME
   * environment variable and service.instance.id.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("service")
  @Nullable
  public ExperimentalServiceResourceDetectorModel getService() {
    return service;
  }

  @JsonProperty("service")
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
  public ExperimentalResourceDetectorModel withAdditionalProperty(
      String name, ExperimentalResourceDetectorPropertyModel value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalResourceDetectorModel{"
        + "container="
        + container
        + ", host="
        + host
        + ", process="
        + process
        + ", service="
        + service
        + ", additionalProperties="
        + additionalProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.container == null) ? 0 : this.container.hashCode();
    h *= 1000003;
    h ^= (this.host == null) ? 0 : this.host.hashCode();
    h *= 1000003;
    h ^= (this.process == null) ? 0 : this.process.hashCode();
    h *= 1000003;
    h ^= (this.service == null) ? 0 : this.service.hashCode();
    h *= 1000003;
    h ^= (this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalResourceDetectorModel) {
      ExperimentalResourceDetectorModel that = (ExperimentalResourceDetectorModel) o;
      return (this.container == null
              ? that.container == null
              : this.container.equals(that.container))
          && (this.host == null ? that.host == null : this.host.equals(that.host))
          && (this.process == null ? that.process == null : this.process.equals(that.process))
          && (this.service == null ? that.service == null : this.service.equals(that.service))
          && (this.additionalProperties == null
              ? that.additionalProperties == null
              : this.additionalProperties.equals(that.additionalProperties));
    }
    return false;
  }
}
