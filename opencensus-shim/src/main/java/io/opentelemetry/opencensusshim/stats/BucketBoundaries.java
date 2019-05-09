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

package io.opentelemetry.opencensusshim.stats;

import com.google.auto.value.AutoValue;
import io.opentelemetry.opencensusshim.internal.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.Immutable;

/**
 * The bucket boundaries for a histogram.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class BucketBoundaries {

  private static final Logger logger = Logger.getLogger(BucketBoundaries.class.getName());

  /**
   * Returns a {@code BucketBoundaries} with the given buckets.
   *
   * @param bucketBoundaries the boundaries for the buckets in the underlying histogram.
   * @return a new {@code BucketBoundaries} with the specified boundaries.
   * @throws NullPointerException if {@code bucketBoundaries} is null.
   * @throws IllegalArgumentException if {@code bucketBoundaries} is not sorted.
   * @since 0.1.0
   */
  public static final BucketBoundaries create(List<Double> bucketBoundaries) {
    Utils.checkNotNull(bucketBoundaries, "bucketBoundaries");
    List<Double> bucketBoundariesCopy = new ArrayList<Double>(bucketBoundaries); // Deep copy.
    // Check if sorted.
    if (bucketBoundariesCopy.size() > 1) {
      double previous = bucketBoundariesCopy.get(0);
      for (int i = 1; i < bucketBoundariesCopy.size(); i++) {
        double next = bucketBoundariesCopy.get(i);
        Utils.checkArgument(previous < next, "Bucket boundaries not sorted.");
        previous = next;
      }
    }
    return new AutoValue_BucketBoundaries(
        Collections.unmodifiableList(dropNegativeBucketBounds(bucketBoundariesCopy)));
  }

  private static List<Double> dropNegativeBucketBounds(List<Double> bucketBoundaries) {
    // Negative values (BucketBounds) are currently not supported by any of the backends
    // that OC supports.
    int negativeBucketBounds = 0;
    int zeroBucketBounds = 0;
    for (Double value : bucketBoundaries) {
      if (value <= 0) {
        if (value == 0) {
          zeroBucketBounds++;
        } else {
          negativeBucketBounds++;
        }
      } else {
        break;
      }
    }

    if (negativeBucketBounds > 0) {
      logger.log(
          Level.WARNING,
          "Dropping "
              + negativeBucketBounds
              + " negative bucket boundaries, the values must be strictly > 0.");
    }
    return bucketBoundaries.subList(
        negativeBucketBounds + zeroBucketBounds, bucketBoundaries.size());
  }

  /**
   * Returns a list of histogram bucket boundaries.
   *
   * @return a list of histogram bucket boundaries.
   * @since 0.1.0
   */
  public abstract List<Double> getBoundaries();
}
