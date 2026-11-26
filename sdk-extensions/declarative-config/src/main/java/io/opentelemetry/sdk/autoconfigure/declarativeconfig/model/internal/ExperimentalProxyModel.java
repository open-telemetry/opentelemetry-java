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
@JsonPropertyOrder({"url", "username", "password"})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ExperimentalProxyModel {

  @Nullable private String url;
  @Nullable private String username;
  @Nullable private String password;

  /**
   * Configure the URL of the HTTP forward proxy, e.g. http://proxy.example.com:3128.
   *
   * <p>The scheme must be http or https. Implementations may not support https (TLS between the
   * exporter and the proxy).
   *
   * <p>Must not include userinfo; set credentials with .username and .password instead.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty("url")
  @Nullable
  public String getUrl() {
    return url;
  }

  @JsonProperty("url")
  public ExperimentalProxyModel setUrl(String url) {
    this.url = url;
    return this;
  }

  /**
   * Configure the username for Basic authentication to the proxy.
   *
   * <p>If omitted or null, no proxy authentication is used.
   */
  @JsonProperty("username")
  @Nullable
  public String getUsername() {
    return username;
  }

  @JsonProperty("username")
  public ExperimentalProxyModel setUsername(String username) {
    this.username = username;
    return this;
  }

  /**
   * Configure the password for Basic authentication to the proxy.
   *
   * <p>If omitted or null, no proxy authentication is used.
   */
  @JsonProperty("password")
  @Nullable
  public String getPassword() {
    return password;
  }

  @JsonProperty("password")
  public ExperimentalProxyModel setPassword(String password) {
    this.password = password;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalProxyModel{"
        + "url="
        + url
        + ", username="
        + username
        + ", password="
        + password
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.url == null) ? 0 : this.url.hashCode();
    h *= 1000003;
    h ^= (this.username == null) ? 0 : this.username.hashCode();
    h *= 1000003;
    h ^= (this.password == null) ? 0 : this.password.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalProxyModel) {
      ExperimentalProxyModel that = (ExperimentalProxyModel) o;
      return (this.url == null ? that.url == null : this.url.equals(that.url))
          && (this.username == null ? that.username == null : this.username.equals(that.username))
          && (this.password == null ? that.password == null : this.password.equals(that.password));
    }
    return false;
  }
}
