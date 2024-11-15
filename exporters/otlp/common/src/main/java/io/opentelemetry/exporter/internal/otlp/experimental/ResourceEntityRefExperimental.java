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
public final class ResourceEntityRefExperimental {
  public static final ProtoFieldInfo SCHEMA_URL = ProtoFieldInfo.create(1, 10, "schemaUrl");
  public static final ProtoFieldInfo TYPE = ProtoFieldInfo.create(2, 18, "type");
  public static final ProtoFieldInfo IDENTITY_ATTRIBUTES =
      ProtoFieldInfo.create(3, 26, "idAttrKeys");
  public static final ProtoFieldInfo DESCRIPTION_ATTRIBUTES =
      ProtoFieldInfo.create(4, 34, "descrAttrKeys");

  private ResourceEntityRefExperimental() {}
}
