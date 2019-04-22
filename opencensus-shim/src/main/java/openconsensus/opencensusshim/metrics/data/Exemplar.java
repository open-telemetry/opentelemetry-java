/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.opencensusshim.metrics.data;

import static openconsensus.opencensusshim.internal.Utils.checkNotNull;

import com.google.auto.value.AutoValue;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.concurrent.Immutable;
import openconsensus.opencensusshim.common.Timestamp;

/**
 * An example point that may be used to annotate aggregated distribution values, associated with a
 * histogram bucket.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class Exemplar {

  Exemplar() {}

  /**
   * Returns value of the {@link Exemplar} point.
   *
   * @return value of the {@code Exemplar} point.
   * @since 0.1.0
   */
  public abstract double getValue();

  /**
   * Returns the time that this {@link Exemplar}'s value was recorded.
   *
   * @return the time that this {@code Exemplar}'s value was recorded.
   * @since 0.1.0
   */
  public abstract Timestamp getTimestamp();

  /**
   * Returns the contextual information about the example value.
   *
   * @return the contextual information about the example value.
   * @since 0.1.0
   */
  public abstract Map<String, AttachmentValue> getAttachments();

  /**
   * Creates an {@link Exemplar}.
   *
   * @param value value of the {@link Exemplar} point.
   * @param timestamp the time that this {@code Exemplar}'s value was recorded.
   * @param attachments the contextual information about the example value.
   * @return an {@code Exemplar}.
   * @since 0.1.0
   */
  public static Exemplar create(
      double value, Timestamp timestamp, Map<String, AttachmentValue> attachments) {
    checkNotNull(attachments, "attachments");
    Map<String, AttachmentValue> attachmentsCopy =
        Collections.unmodifiableMap(new HashMap<String, AttachmentValue>(attachments));
    for (Entry<String, AttachmentValue> entry : attachmentsCopy.entrySet()) {
      checkNotNull(entry.getKey(), "key of attachments");
      checkNotNull(entry.getValue(), "value of attachments");
    }
    return new AutoValue_Exemplar(value, timestamp, attachmentsCopy);
  }
}
