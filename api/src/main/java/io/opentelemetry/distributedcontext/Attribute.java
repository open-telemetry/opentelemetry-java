/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.distributedcontext;

import com.google.auto.value.AutoValue;
import io.opentelemetry.distributedcontext.AttributeMetadata.AttributeTtl;
import javax.annotation.concurrent.Immutable;

/**
 * {@link AttributeKey} paired with a {@link AttributeValue}.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class Attribute {

  /** Default propagation metadata - unlimited propagation. */
  public static final AttributeMetadata METADATA_UNLIMITED_PROPAGATION =
      AttributeMetadata.create(AttributeTtl.UNLIMITED_PROPAGATION);

  Attribute() {}

  /**
   * Creates an {@code Attribute} from the given key, value and metadata.
   *
   * @param key the attribute key.
   * @param value the attribute value.
   * @param attrMetadata the attribute metadata.
   * @return a {@code Attribute}.
   * @since 0.1.0
   */
  public static Attribute create(
      AttributeKey key, AttributeValue value, AttributeMetadata attrMetadata) {
    return new AutoValue_Attribute(key, value, attrMetadata);
  }

  /**
   * Returns the attribute's key.
   *
   * @return the attribute's key.
   * @since 0.1.0
   */
  public abstract AttributeKey getKey();

  /**
   * Returns the attribute's value.
   *
   * @return the attribute's value.
   * @since 0.1.0
   */
  public abstract AttributeValue getValue();

  /**
   * Returns the {@link AttributeMetadata} associated with this {@link Attribute}.
   *
   * @return the {@code AttributeMetadata}.
   * @since 0.1.0
   */
  public abstract AttributeMetadata getAttributeMetadata();
}
