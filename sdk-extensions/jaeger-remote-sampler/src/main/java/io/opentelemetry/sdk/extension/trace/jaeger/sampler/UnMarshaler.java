/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import java.io.IOException;

/**
 * UnMarshaler from protobuf wire format to SDK data type.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
abstract class UnMarshaler {

  abstract void read(byte[] payload) throws IOException;
}
