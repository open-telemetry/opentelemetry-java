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
@JsonPropertyOrder({"version", "experimental", "dual_emit"})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ExperimentalSemconvConfigModel {

  @Nullable private Integer version;
  @Nullable private Boolean experimental;
  @Nullable private Boolean dualEmit;

  /**
   * The target semantic convention version for this domain (e.g., 1).
   *
   * <p>If omitted or null, the latest stable version is used, or if no stable version is available
   * and .experimental is true then the latest experimental version is used.
   */
  @JsonProperty("version")
  @Nullable
  public Integer getVersion() {
    return version;
  }

  @JsonProperty("version")
  public ExperimentalSemconvConfigModel withVersion(Integer version) {
    this.version = version;
    return this;
  }

  /**
   * Use latest experimental semantic conventions (before stable is available or to enable
   * experimental features on top of stable conventions).
   *
   * <p>If omitted or null, false is used.
   */
  @JsonProperty("experimental")
  @Nullable
  public Boolean getExperimental() {
    return experimental;
  }

  @JsonProperty("experimental")
  public ExperimentalSemconvConfigModel withExperimental(Boolean experimental) {
    this.experimental = experimental;
    return this;
  }

  /**
   * When true, also emit the previous major version alongside the target version.
   *
   * <p>For version=1, the previous version refers to the pre-stable conventions that the
   * instrumentation emitted before the first stable semantic convention version was defined.
   *
   * <p>For version=2 and above, the previous version is the prior stable major version (e.g.,
   * version=2, dual_emit=true emits both v2 and v1).
   *
   * <p>Enables dual-emit for phased migration between versions.
   *
   * <p>If omitted or null, false is used.
   */
  @JsonProperty("dual_emit")
  @Nullable
  public Boolean getDualEmit() {
    return dualEmit;
  }

  @JsonProperty("dual_emit")
  public ExperimentalSemconvConfigModel withDualEmit(Boolean dualEmit) {
    this.dualEmit = dualEmit;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalSemconvConfigModel{"
        + "version="
        + version
        + ", experimental="
        + experimental
        + ", dualEmit="
        + dualEmit
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.version == null) ? 0 : this.version.hashCode();
    h *= 1000003;
    h ^= (this.experimental == null) ? 0 : this.experimental.hashCode();
    h *= 1000003;
    h ^= (this.dualEmit == null) ? 0 : this.dualEmit.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalSemconvConfigModel) {
      ExperimentalSemconvConfigModel that = (ExperimentalSemconvConfigModel) o;
      return (this.version == null ? that.version == null : this.version.equals(that.version))
          && (this.experimental == null
              ? that.experimental == null
              : this.experimental.equals(that.experimental))
          && (this.dualEmit == null ? that.dualEmit == null : this.dualEmit.equals(that.dualEmit));
    }
    return false;
  }
}
