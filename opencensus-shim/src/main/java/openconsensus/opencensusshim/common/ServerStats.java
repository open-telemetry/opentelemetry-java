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

package openconsensus.opencensusshim.common;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

/**
 * A representation of stats measured on the server side.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class ServerStats {

  ServerStats() {}

  /**
   * Returns Load Balancer latency, a latency observed at Load Balancer.
   *
   * @return Load Balancer latency in nanoseconds.
   * @since 0.1.0
   */
  public abstract long getLbLatencyNs();

  /**
   * Returns Service latency, a latency observed at Server.
   *
   * @return Service latency in nanoseconds.
   * @since 0.1.0
   */
  public abstract long getServiceLatencyNs();

  /**
   * Returns Trace options, a set of bits indicating properties of trace.
   *
   * @return Trace options a set of bits indicating properties of trace.
   * @since 0.1.0
   */
  public abstract byte getTraceOption();

  /**
   * Creates new {@link ServerStats} from specified parameters.
   *
   * @param lbLatencyNs Represents request processing latency observed on Load Balancer. It is
   *     measured in nanoseconds. Must not be less than 0. Value of 0 represents that the latency is
   *     not measured.
   * @param serviceLatencyNs Represents request processing latency observed on Server. It is
   *     measured in nanoseconds. Must not be less than 0. Value of 0 represents that the latency is
   *     not measured.
   * @param traceOption Represents set of bits to indicate properties of trace. Currently it used
   *     only the least signification bit to represent sampling of the request on the server side.
   *     Other bits are ignored.
   * @return new {@code ServerStats} with specified fields.
   * @throws IllegalArgumentException if the arguments are out of range.
   * @since 0.1.0
   */
  public static ServerStats create(long lbLatencyNs, long serviceLatencyNs, byte traceOption) {

    if (lbLatencyNs < 0) {
      throw new IllegalArgumentException("'getLbLatencyNs' is less than zero: " + lbLatencyNs);
    }

    if (serviceLatencyNs < 0) {
      throw new IllegalArgumentException(
          "'getServiceLatencyNs' is less than zero: " + serviceLatencyNs);
    }

    return new AutoValue_ServerStats(lbLatencyNs, serviceLatencyNs, traceOption);
  }
}
