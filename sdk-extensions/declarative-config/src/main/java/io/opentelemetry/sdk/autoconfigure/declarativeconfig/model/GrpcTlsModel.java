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
@JsonPropertyOrder({"ca_file", "key_file", "cert_file", "insecure"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class GrpcTlsModel {

  /**
   * Configure certificate used to verify a server's TLS credentials. Absolute path to certificate
   * file in PEM format. If omitted or null, system default certificate verification is used for
   * secure connections.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("ca_file")
  @JsonPropertyDescription(
      "Configure certificate used to verify a server's TLS credentials. \nAbsolute path to certificate file in PEM format.\nIf omitted or null, system default certificate verification is used for secure connections.\n")
  private String caFile;

  /**
   * Configure mTLS private client key. Absolute path to client key file in PEM format. If set,
   * .client_certificate must also be set. If omitted or null, mTLS is not used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("key_file")
  @JsonPropertyDescription(
      "Configure mTLS private client key. \nAbsolute path to client key file in PEM format. If set, .client_certificate must also be set.\nIf omitted or null, mTLS is not used.\n")
  private String keyFile;

  /**
   * Configure mTLS client certificate. Absolute path to client certificate file in PEM format. If
   * set, .client_key must also be set. If omitted or null, mTLS is not used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("cert_file")
  @JsonPropertyDescription(
      "Configure mTLS client certificate. \nAbsolute path to client certificate file in PEM format. If set, .client_key must also be set.\nIf omitted or null, mTLS is not used.\n")
  private String certFile;

  /**
   * Configure client transport security for the exporter's connection. Only applicable when
   * .endpoint is provided without http or https scheme. Implementations may choose to ignore
   * .insecure. If omitted or null, false is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("insecure")
  @JsonPropertyDescription(
      "Configure client transport security for the exporter's connection. \nOnly applicable when .endpoint is provided without http or https scheme. Implementations may choose to ignore .insecure.\nIf omitted or null, false is used.\n")
  private Boolean insecure;

  /**
   * Configure certificate used to verify a server's TLS credentials. Absolute path to certificate
   * file in PEM format. If omitted or null, system default certificate verification is used for
   * secure connections.
   */
  @JsonProperty("ca_file")
  @Nullable
  public String getCaFile() {
    return caFile;
  }

  public GrpcTlsModel withCaFile(String caFile) {
    this.caFile = caFile;
    return this;
  }

  /**
   * Configure mTLS private client key. Absolute path to client key file in PEM format. If set,
   * .client_certificate must also be set. If omitted or null, mTLS is not used.
   */
  @JsonProperty("key_file")
  @Nullable
  public String getKeyFile() {
    return keyFile;
  }

  public GrpcTlsModel withKeyFile(String keyFile) {
    this.keyFile = keyFile;
    return this;
  }

  /**
   * Configure mTLS client certificate. Absolute path to client certificate file in PEM format. If
   * set, .client_key must also be set. If omitted or null, mTLS is not used.
   */
  @JsonProperty("cert_file")
  @Nullable
  public String getCertFile() {
    return certFile;
  }

  public GrpcTlsModel withCertFile(String certFile) {
    this.certFile = certFile;
    return this;
  }

  /**
   * Configure client transport security for the exporter's connection. Only applicable when
   * .endpoint is provided without http or https scheme. Implementations may choose to ignore
   * .insecure. If omitted or null, false is used.
   */
  @JsonProperty("insecure")
  @Nullable
  public Boolean getInsecure() {
    return insecure;
  }

  public GrpcTlsModel withInsecure(Boolean insecure) {
    this.insecure = insecure;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(GrpcTlsModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("caFile");
    sb.append('=');
    sb.append(((this.caFile == null) ? "<null>" : this.caFile));
    sb.append(',');
    sb.append("keyFile");
    sb.append('=');
    sb.append(((this.keyFile == null) ? "<null>" : this.keyFile));
    sb.append(',');
    sb.append("certFile");
    sb.append('=');
    sb.append(((this.certFile == null) ? "<null>" : this.certFile));
    sb.append(',');
    sb.append("insecure");
    sb.append('=');
    sb.append(((this.insecure == null) ? "<null>" : this.insecure));
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
    result = ((result * 31) + ((this.insecure == null) ? 0 : this.insecure.hashCode()));
    result = ((result * 31) + ((this.caFile == null) ? 0 : this.caFile.hashCode()));
    result = ((result * 31) + ((this.keyFile == null) ? 0 : this.keyFile.hashCode()));
    result = ((result * 31) + ((this.certFile == null) ? 0 : this.certFile.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof GrpcTlsModel) == false) {
      return false;
    }
    GrpcTlsModel rhs = ((GrpcTlsModel) other);
    return (((((this.insecure == rhs.insecure)
                    || ((this.insecure != null) && this.insecure.equals(rhs.insecure)))
                && ((this.caFile == rhs.caFile)
                    || ((this.caFile != null) && this.caFile.equals(rhs.caFile))))
            && ((this.keyFile == rhs.keyFile)
                || ((this.keyFile != null) && this.keyFile.equals(rhs.keyFile))))
        && ((this.certFile == rhs.certFile)
            || ((this.certFile != null) && this.certFile.equals(rhs.certFile))));
  }
}
