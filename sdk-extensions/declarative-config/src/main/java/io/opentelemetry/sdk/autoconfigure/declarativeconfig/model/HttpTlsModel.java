/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"ca_file", "key_file", "cert_file"})
@Generated("jsonschema2pojo")
public class HttpTlsModel {

  @JsonProperty("ca_file")
  @Nullable
  private String caFile;

  @JsonProperty("key_file")
  @Nullable
  private String keyFile;

  @JsonProperty("cert_file")
  @Nullable
  private String certFile;

  /**
   * Configure certificate used to verify a server's TLS credentials.
   *
   * <p>Absolute path to certificate file in PEM format.
   *
   * <p>If omitted or null, system default certificate verification is used for secure connections.
   */
  @JsonProperty("ca_file")
  @Nullable
  public String getCaFile() {
    return caFile;
  }

  public HttpTlsModel withCaFile(String caFile) {
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
  @JsonProperty("key_file")
  @Nullable
  public String getKeyFile() {
    return keyFile;
  }

  public HttpTlsModel withKeyFile(String keyFile) {
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
  @JsonProperty("cert_file")
  @Nullable
  public String getCertFile() {
    return certFile;
  }

  public HttpTlsModel withCertFile(String certFile) {
    this.certFile = certFile;
    return this;
  }

  @Override
  public String toString() {
    return "HttpTlsModel{"
        + "caFile="
        + caFile
        + ", keyFile="
        + keyFile
        + ", certFile="
        + certFile
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
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof HttpTlsModel) {
      HttpTlsModel that = (HttpTlsModel) o;
      return (this.caFile == null ? that.caFile == null : this.caFile.equals(that.caFile))
          && (this.keyFile == null ? that.keyFile == null : this.keyFile.equals(that.keyFile))
          && (this.certFile == null ? that.certFile == null : this.certFile.equals(that.certFile));
    }
    return false;
  }
}
