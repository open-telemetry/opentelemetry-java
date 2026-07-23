/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ParentBasedSamplerModel.LOCAL_PARENT_NOT_SAMPLED;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ParentBasedSamplerModel.LOCAL_PARENT_SAMPLED;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ParentBasedSamplerModel.REMOTE_PARENT_NOT_SAMPLED;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ParentBasedSamplerModel.REMOTE_PARENT_SAMPLED;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ParentBasedSamplerModel.ROOT;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExtensionPropertyUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  ROOT,
  REMOTE_PARENT_SAMPLED,
  REMOTE_PARENT_NOT_SAMPLED,
  LOCAL_PARENT_SAMPLED,
  LOCAL_PARENT_NOT_SAMPLED
})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ParentBasedSamplerModel {

  static final String ROOT = "root";
  static final String REMOTE_PARENT_SAMPLED = "remote_parent_sampled";
  static final String REMOTE_PARENT_NOT_SAMPLED = "remote_parent_not_sampled";
  static final String LOCAL_PARENT_SAMPLED = "local_parent_sampled";
  static final String LOCAL_PARENT_NOT_SAMPLED = "local_parent_not_sampled";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(ROOT, SamplerModel.class);
    STABLE_PROPERTIES.put(REMOTE_PARENT_SAMPLED, SamplerModel.class);
    STABLE_PROPERTIES.put(REMOTE_PARENT_NOT_SAMPLED, SamplerModel.class);
    STABLE_PROPERTIES.put(LOCAL_PARENT_SAMPLED, SamplerModel.class);
    STABLE_PROPERTIES.put(LOCAL_PARENT_NOT_SAMPLED, SamplerModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private SamplerModel root;
  @Nullable private SamplerModel remoteParentSampled;
  @Nullable private SamplerModel remoteParentNotSampled;
  @Nullable private SamplerModel localParentSampled;
  @Nullable private SamplerModel localParentNotSampled;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure root sampler.
   *
   * <p>If omitted, always_on is used.
   */
  @JsonProperty(ROOT)
  @Nullable
  public SamplerModel getRoot() {
    if (root == null) {
      return ExtensionPropertyUtil.getGraduated(ROOT, extensionProperties, SamplerModel.class);
    }
    return root;
  }

  @JsonProperty(ROOT)
  public ParentBasedSamplerModel withRoot(SamplerModel root) {
    this.root = root;
    return this;
  }

  /**
   * Configure remote_parent_sampled sampler.
   *
   * <p>If omitted, always_on is used.
   */
  @JsonProperty(REMOTE_PARENT_SAMPLED)
  @Nullable
  public SamplerModel getRemoteParentSampled() {
    if (remoteParentSampled == null) {
      return ExtensionPropertyUtil.getGraduated(
          REMOTE_PARENT_SAMPLED, extensionProperties, SamplerModel.class);
    }
    return remoteParentSampled;
  }

  @JsonProperty(REMOTE_PARENT_SAMPLED)
  public ParentBasedSamplerModel withRemoteParentSampled(SamplerModel remoteParentSampled) {
    this.remoteParentSampled = remoteParentSampled;
    return this;
  }

  /**
   * Configure remote_parent_not_sampled sampler.
   *
   * <p>If omitted, always_off is used.
   */
  @JsonProperty(REMOTE_PARENT_NOT_SAMPLED)
  @Nullable
  public SamplerModel getRemoteParentNotSampled() {
    if (remoteParentNotSampled == null) {
      return ExtensionPropertyUtil.getGraduated(
          REMOTE_PARENT_NOT_SAMPLED, extensionProperties, SamplerModel.class);
    }
    return remoteParentNotSampled;
  }

  @JsonProperty(REMOTE_PARENT_NOT_SAMPLED)
  public ParentBasedSamplerModel withRemoteParentNotSampled(SamplerModel remoteParentNotSampled) {
    this.remoteParentNotSampled = remoteParentNotSampled;
    return this;
  }

  /**
   * Configure local_parent_sampled sampler.
   *
   * <p>If omitted, always_on is used.
   */
  @JsonProperty(LOCAL_PARENT_SAMPLED)
  @Nullable
  public SamplerModel getLocalParentSampled() {
    if (localParentSampled == null) {
      return ExtensionPropertyUtil.getGraduated(
          LOCAL_PARENT_SAMPLED, extensionProperties, SamplerModel.class);
    }
    return localParentSampled;
  }

  @JsonProperty(LOCAL_PARENT_SAMPLED)
  public ParentBasedSamplerModel withLocalParentSampled(SamplerModel localParentSampled) {
    this.localParentSampled = localParentSampled;
    return this;
  }

  /**
   * Configure local_parent_not_sampled sampler.
   *
   * <p>If omitted, always_off is used.
   */
  @JsonProperty(LOCAL_PARENT_NOT_SAMPLED)
  @Nullable
  public SamplerModel getLocalParentNotSampled() {
    if (localParentNotSampled == null) {
      return ExtensionPropertyUtil.getGraduated(
          LOCAL_PARENT_NOT_SAMPLED, extensionProperties, SamplerModel.class);
    }
    return localParentNotSampled;
  }

  @JsonProperty(LOCAL_PARENT_NOT_SAMPLED)
  public ParentBasedSamplerModel withLocalParentNotSampled(SamplerModel localParentNotSampled) {
    this.localParentNotSampled = localParentNotSampled;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public ParentBasedSamplerModel withExtensionProperty(String name, @Nullable Object value) {
    ExtensionPropertyUtil.handleAnySetter(
        name,
        value,
        extensionProperties,
        Collections.emptyMap(),
        STABLE_PROPERTIES,
        ALLOWS_ADDITIONAL_PROPERTIES);
    return this;
  }

  @Override
  public String toString() {
    return "ParentBasedSamplerModel{"
        + "root="
        + root
        + ", remoteParentSampled="
        + remoteParentSampled
        + ", remoteParentNotSampled="
        + remoteParentNotSampled
        + ", localParentSampled="
        + localParentSampled
        + ", localParentNotSampled="
        + localParentNotSampled
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.root == null) ? 0 : this.root.hashCode();
    h *= 1000003;
    h ^= (this.remoteParentSampled == null) ? 0 : this.remoteParentSampled.hashCode();
    h *= 1000003;
    h ^= (this.remoteParentNotSampled == null) ? 0 : this.remoteParentNotSampled.hashCode();
    h *= 1000003;
    h ^= (this.localParentSampled == null) ? 0 : this.localParentSampled.hashCode();
    h *= 1000003;
    h ^= (this.localParentNotSampled == null) ? 0 : this.localParentNotSampled.hashCode();
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ParentBasedSamplerModel) {
      ParentBasedSamplerModel that = (ParentBasedSamplerModel) o;
      return (this.root == null ? that.root == null : this.root.equals(that.root))
          && (this.remoteParentSampled == null
              ? that.remoteParentSampled == null
              : this.remoteParentSampled.equals(that.remoteParentSampled))
          && (this.remoteParentNotSampled == null
              ? that.remoteParentNotSampled == null
              : this.remoteParentNotSampled.equals(that.remoteParentNotSampled))
          && (this.localParentSampled == null
              ? that.localParentSampled == null
              : this.localParentSampled.equals(that.localParentSampled))
          && (this.localParentNotSampled == null
              ? that.localParentNotSampled == null
              : this.localParentNotSampled.equals(that.localParentNotSampled))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
