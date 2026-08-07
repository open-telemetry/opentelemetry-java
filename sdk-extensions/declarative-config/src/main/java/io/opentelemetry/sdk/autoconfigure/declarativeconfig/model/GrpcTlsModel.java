/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.GrpcTlsModel.CA_FILE;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.GrpcTlsModel.CERT_FILE;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.GrpcTlsModel.INSECURE;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.GrpcTlsModel.KEY_FILE;

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
@JsonPropertyOrder({CA_FILE, KEY_FILE, CERT_FILE, INSECURE})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class GrpcTlsModel {

  static final String CA_FILE = "ca_file";
  static final String KEY_FILE = "key_file";
  static final String CERT_FILE = "cert_file";
  static final String INSECURE = "insecure";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(CA_FILE, String.class);
    STABLE_PROPERTIES.put(KEY_FILE, String.class);
    STABLE_PROPERTIES.put(CERT_FILE, String.class);
    STABLE_PROPERTIES.put(INSECURE, Boolean.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private String caFile;
  @Nullable private String keyFile;
  @Nullable private String certFile;
  @Nullable private Boolean insecure;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure certificate used to verify a server's TLS credentials.
   *
   * <p>Absolute path to certificate file in PEM format.
   *
   * <p>If omitted or null, system default certificate verification is used for secure connections.
   */
  @JsonProperty(CA_FILE)
  @Nullable
  public String getCaFile() {
    if (caFile == null) {
      return ExtensionPropertyUtil.getGraduated(CA_FILE, extensionProperties, String.class);
    }
    return caFile;
  }

  @JsonProperty(CA_FILE)
  public GrpcTlsModel withCaFile(String caFile) {
    this.caFile = caFile;
    return this;
  }

  /**
   * Configure mTLS private client key.
   *
   * <p>Absolute path to client key file in PEM format. If set, .client_certificate must also be
   * set.
   *
   * <p>If omitted or null, mTLS is not used.
   */
  @JsonProperty(KEY_FILE)
  @Nullable
  public String getKeyFile() {
    if (keyFile == null) {
      return ExtensionPropertyUtil.getGraduated(KEY_FILE, extensionProperties, String.class);
    }
    return keyFile;
  }

  @JsonProperty(KEY_FILE)
  public GrpcTlsModel withKeyFile(String keyFile) {
    this.keyFile = keyFile;
    return this;
  }

  /**
   * Configure mTLS client certificate.
   *
   * <p>Absolute path to client certificate file in PEM format. If set, .client_key must also be
   * set.
   *
   * <p>If omitted or null, mTLS is not used.
   */
  @JsonProperty(CERT_FILE)
  @Nullable
  public String getCertFile() {
    if (certFile == null) {
      return ExtensionPropertyUtil.getGraduated(CERT_FILE, extensionProperties, String.class);
    }
    return certFile;
  }

  @JsonProperty(CERT_FILE)
  public GrpcTlsModel withCertFile(String certFile) {
    this.certFile = certFile;
    return this;
  }

  /**
   * Configure client transport security for the exporter's connection.
   *
   * <p>Only applicable when .endpoint is provided without http or https scheme. Implementations may
   * choose to ignore .insecure.
   *
   * <p>If omitted or null, false is used.
   */
  @JsonProperty(INSECURE)
  @Nullable
  public Boolean getInsecure() {
    if (insecure == null) {
      return ExtensionPropertyUtil.getGraduated(INSECURE, extensionProperties, Boolean.class);
    }
    return insecure;
  }

  @JsonProperty(INSECURE)
  public GrpcTlsModel withInsecure(Boolean insecure) {
    this.insecure = insecure;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public GrpcTlsModel withExtensionProperty(String name, @Nullable Object value) {
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
    return "GrpcTlsModel{"
        + "caFile="
        + caFile
        + ", keyFile="
        + keyFile
        + ", certFile="
        + certFile
        + ", insecure="
        + insecure
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.caFile == null) ? 0 : this.caFile.hashCode();
    h *= 1000003;
    h ^= (this.keyFile == null) ? 0 : this.keyFile.hashCode();
    h *= 1000003;
    h ^= (this.certFile == null) ? 0 : this.certFile.hashCode();
    h *= 1000003;
    h ^= (this.insecure == null) ? 0 : this.insecure.hashCode();
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof GrpcTlsModel) {
      GrpcTlsModel that = (GrpcTlsModel) o;
      return (this.caFile == null ? that.caFile == null : this.caFile.equals(that.caFile))
          && (this.keyFile == null ? that.keyFile == null : this.keyFile.equals(that.keyFile))
          && (this.certFile == null ? that.certFile == null : this.certFile.equals(that.certFile))
          && (this.insecure == null ? that.insecure == null : this.insecure.equals(that.insecure))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
