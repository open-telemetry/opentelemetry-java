/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import io.opentelemetry.api.common.Attributes;
import javax.annotation.Nullable;

/**
 * A builder for {@link InstrumentationScopeInfo}.
 *
 * @since 1.18.0
 */
public final class InstrumentationScopeInfoBuilder {

  private final String name;
  @Nullable private String version;
  @Nullable private String schemaUrl;
  @Nullable private Attributes attributes;

  InstrumentationScopeInfoBuilder(String name) {
    this.name = name;
  }

  /** Set the version. */
  public InstrumentationScopeInfoBuilder setVersion(String version) {
    this.version = version;
    return this;
  }

  /** Set the schema URL. */
  public InstrumentationScopeInfoBuilder setSchemaUrl(String schemaUrl) {
    this.schemaUrl = schemaUrl;
    return this;
  }

  /** Set the attributes. */
  public InstrumentationScopeInfoBuilder setAttributes(Attributes attributes) {
    this.attributes = attributes;
    return this;
  }

  /** Return a {@link InstrumentationScopeInfo} with the configuration of this builder. */
  public InstrumentationScopeInfo build() {
    return InstrumentationScopeInfo.create(
        name, version, schemaUrl, attributes == null ? Attributes.empty() : attributes);
  }
}
