/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import java.io.IOException;
import java.io.InputStream;

/**
 * UnMarshaler from protobuf wire format to SDK data type.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
abstract class UnMarshaler {

  public abstract void read(InputStream inputStream) throws IOException;
}
