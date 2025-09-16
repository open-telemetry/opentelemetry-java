/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * A Stack represents a list of locations. The first location is the leaf frame.
 *
 * @see "profiles.proto::Stack"
 */
@Immutable
public interface StackData {

  List<Integer> getLocationIndices();
}
