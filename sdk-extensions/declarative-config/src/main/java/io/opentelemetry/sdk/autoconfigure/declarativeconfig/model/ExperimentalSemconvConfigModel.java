/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"version", "experimental", "dual_emit"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalSemconvConfigModel {

  /**
   * The target semantic convention version for this domain (e.g., 1). If omitted or null, the
   * latest stable version is used, or if no stable version is available and .experimental is true
   * then the latest experimental version is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("version")
  @JsonPropertyDescription(
      "The target semantic convention version for this domain (e.g., 1).\nIf omitted or null, the latest stable version is used, or if no stable version is available and .experimental is true then the latest experimental version is used.\n")
  private Integer version;

  /**
   * Use latest experimental semantic conventions (before stable is available or to enable
   * experimental features on top of stable conventions). If omitted or null, false is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("experimental")
  @JsonPropertyDescription(
      "Use latest experimental semantic conventions (before stable is available or to enable experimental features on top of stable conventions).\nIf omitted or null, false is used.\n")
  private Boolean experimental;

  /**
   * When true, also emit the previous major version alongside the target version. For version=1,
   * the previous version refers to the pre-stable conventions that the instrumentation emitted
   * before the first stable semantic convention version was defined. For version=2 and above, the
   * previous version is the prior stable major version (e.g., version=2, dual_emit=true emits both
   * v2 and v1). Enables dual-emit for phased migration between versions. If omitted or null, false
   * is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("dual_emit")
  @JsonPropertyDescription(
      "When true, also emit the previous major version alongside the target version.\nFor version=1, the previous version refers to the pre-stable conventions that the instrumentation emitted before the first stable semantic convention version was defined.\nFor version=2 and above, the previous version is the prior stable major version (e.g., version=2, dual_emit=true emits both v2 and v1).\nEnables dual-emit for phased migration between versions.\nIf omitted or null, false is used.\n")
  private Boolean dualEmit;

  /**
   * The target semantic convention version for this domain (e.g., 1). If omitted or null, the
   * latest stable version is used, or if no stable version is available and .experimental is true
   * then the latest experimental version is used.
   */
  @JsonProperty("version")
  @Nullable
  public Integer getVersion() {
    return version;
  }

  public ExperimentalSemconvConfigModel withVersion(Integer version) {
    this.version = version;
    return this;
  }

  /**
   * Use latest experimental semantic conventions (before stable is available or to enable
   * experimental features on top of stable conventions). If omitted or null, false is used.
   */
  @JsonProperty("experimental")
  @Nullable
  public Boolean getExperimental() {
    return experimental;
  }

  public ExperimentalSemconvConfigModel withExperimental(Boolean experimental) {
    this.experimental = experimental;
    return this;
  }

  /**
   * When true, also emit the previous major version alongside the target version. For version=1,
   * the previous version refers to the pre-stable conventions that the instrumentation emitted
   * before the first stable semantic convention version was defined. For version=2 and above, the
   * previous version is the prior stable major version (e.g., version=2, dual_emit=true emits both
   * v2 and v1). Enables dual-emit for phased migration between versions. If omitted or null, false
   * is used.
   */
  @JsonProperty("dual_emit")
  @Nullable
  public Boolean getDualEmit() {
    return dualEmit;
  }

  public ExperimentalSemconvConfigModel withDualEmit(Boolean dualEmit) {
    this.dualEmit = dualEmit;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalSemconvConfigModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("version");
    sb.append('=');
    sb.append(((this.version == null) ? "<null>" : this.version));
    sb.append(',');
    sb.append("experimental");
    sb.append('=');
    sb.append(((this.experimental == null) ? "<null>" : this.experimental));
    sb.append(',');
    sb.append("dualEmit");
    sb.append('=');
    sb.append(((this.dualEmit == null) ? "<null>" : this.dualEmit));
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
    result = ((result * 31) + ((this.dualEmit == null) ? 0 : this.dualEmit.hashCode()));
    result = ((result * 31) + ((this.version == null) ? 0 : this.version.hashCode()));
    result = ((result * 31) + ((this.experimental == null) ? 0 : this.experimental.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalSemconvConfigModel) == false) {
      return false;
    }
    ExperimentalSemconvConfigModel rhs = ((ExperimentalSemconvConfigModel) other);
    return ((((this.dualEmit == rhs.dualEmit)
                || ((this.dualEmit != null) && this.dualEmit.equals(rhs.dualEmit)))
            && ((this.version == rhs.version)
                || ((this.version != null) && this.version.equals(rhs.version))))
        && ((this.experimental == rhs.experimental)
            || ((this.experimental != null) && this.experimental.equals(rhs.experimental))));
  }
}
