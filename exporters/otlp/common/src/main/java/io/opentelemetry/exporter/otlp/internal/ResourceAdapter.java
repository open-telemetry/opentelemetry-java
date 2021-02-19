/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.proto.resource.v1.Resource;

final class ResourceAdapter {
  static Resource toProtoResource(io.opentelemetry.sdk.resources.Resource resource) {
    final Resource.Builder builder = Resource.newBuilder();
    resource
        .getAttributes()
        .forEach((key, value) -> builder.addAttributes(CommonAdapter.toProtoAttribute(key, value)));
    return builder.build();
  }

  private ResourceAdapter() {}
}
