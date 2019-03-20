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

package openconsensus.common;

import java.util.TreeMap;
import javax.annotation.Nullable;

/**
 * A Enum representation for Ids and Size for attributes of {@code ServerStats}.
 *
 * <p>See <a
 * href="https://github.com/census-instrumentation/opencensus-specs/blob/master/encodings/CensusServerStatsEncoding.md">opencensus-server-stats-specs</a>
 * for the field ids and their length defined for Server Stats
 *
 * @since 0.1.0
 */
public final class ServerStatsFieldEnums {

  /**
   * Available Ids for {@code ServerStats} attributes.
   *
   * @since 0.1.0
   */
  public enum Id {
    /**
     * Id for Latency observed at Load Balancer.
     *
     * @since 0.1.0
     */
    SERVER_STATS_LB_LATENCY_ID(0),
    /**
     * Id for Latency observed at Server.
     *
     * @since 0.1.0
     */
    SERVER_STATS_SERVICE_LATENCY_ID(1),
    /**
     * Id for Trace options.
     *
     * @since 0.1.0
     */
    SERVER_STATS_TRACE_OPTION_ID(2);

    private final int value;

    private Id(int value) {
      this.value = value;
    }

    /**
     * Returns the numerical value of the {@link Id}.
     *
     * @return the numerical value of the {@code Id}.
     * @since 0.1.0
     */
    public int value() {
      return value;
    }

    private static final TreeMap<Integer, Id> map = new TreeMap<Integer, Id>();

    static {
      for (Id id : Id.values()) {
        map.put(id.value, id);
      }
    }

    /**
     * Returns the {@link Id} representing the value value of the id.
     *
     * @param value integer value for which {@code Id} is being requested.
     * @return the numerical value of the id. null if the id is not valid
     * @since 0.1.0
     */
    @Nullable
    public static Id valueOf(int value) {
      return map.get(value);
    }
  }

  /**
   * Size for each attributes in {@code ServerStats}.
   *
   * @since 0.1.0
   */
  public enum Size {
    /**
     * Number of bytes used to represent latency observed at Load Balancer.
     *
     * @since 0.1.0
     */
    SERVER_STATS_LB_LATENCY_SIZE(8),
    /**
     * Number of bytes used to represent latency observed at Server.
     *
     * @since 0.1.0
     */
    SERVER_STATS_SERVICE_LATENCY_SIZE(8),
    /**
     * Number of bytes used to represent Trace option.
     *
     * @since 0.1.0
     */
    SERVER_STATS_TRACE_OPTION_SIZE(1);

    private final int value;

    private Size(int value) {
      this.value = value;
    }

    /**
     * Returns the numerical value of the {@link Size}.
     *
     * @return the numerical value of the {@code Size}.
     * @since 0.1.0
     */
    public int value() {
      return value;
    }
  }

  private static final int TOTALSIZE = computeTotalSize();

  private ServerStatsFieldEnums() {}

  private static int computeTotalSize() {
    int sum = 0;
    for (Size sizeValue : Size.values()) {
      sum += sizeValue.value();
      sum += 1; // For Id
    }
    return sum;
  }

  /**
   * Returns the total size required to encode the {@code ServerStats}.
   *
   * @return the total size required to encode all fields in {@code ServerStats}.
   * @since 0.1.0
   */
  public static int getTotalSize() {
    return TOTALSIZE;
  }
}
