/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.experimental;

import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class ResourceExperimental {
  public static final ProtoFieldInfo ENTITY_REFS = ProtoFieldInfo.create(3, 26, "entityRefs");

  private ResourceExperimental() {}
}
