/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import static io.opentelemetry.common.AttributeKey.longKey;

import io.opentelemetry.common.Attributes;

public class TestResourceProvider extends ResourceProvider {

  @Override
  protected Attributes getAttributes() {
    return Attributes.of(longKey("providerAttribute"), 42L);
  }
}
