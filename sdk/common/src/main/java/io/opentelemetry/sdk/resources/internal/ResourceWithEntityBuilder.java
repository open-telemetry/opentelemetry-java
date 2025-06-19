/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import io.opentelemetry.api.common.Attributes;
import java.util.Collection;

/**
 * A builder of {@link ResourceWithEntity} that allows to add key-value pairs and copy attributes
 * from other {@link Attributes} or {@link ResourceWithEntity}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface ResourceWithEntityBuilder {
  // TODO - Raw Attributes.
  /** Appends a new entity on to the end of the list of entities. */
  ResourceWithEntityBuilder add(Entity e);

  /** Appends a new collection of entities on to the end of the list of entities. */
  ResourceWithEntityBuilder addAll(Collection<Entity> entities);

  /** Create the {@link ResourceWithEntity} from this. */
  ResourceWithEntity build();
}
