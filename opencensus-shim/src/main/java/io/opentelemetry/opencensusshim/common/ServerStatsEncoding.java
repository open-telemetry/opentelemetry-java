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

package io.opentelemetry.opencensusshim.common;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A service class to encode/decode {@link ServerStats} as defined by the spec.
 *
 * <p>See <a
 * href="https://github.com/census-instrumentation/opencensus-specs/blob/master/encodings/CensusServerStatsEncoding.md">opencensus-server-stats-specs</a>
 * for encoding {@code ServerStats}
 *
 * <p>Use {@code ServerStatsEncoding.toBytes(ServerStats stats)} to encode.
 *
 * <p>Use {@code ServerStatsEncoding.parseBytes(byte[] serialized)} to decode.
 *
 * @since 0.1.0
 */
public final class ServerStatsEncoding {

  private ServerStatsEncoding() {}

  /**
   * The current encoding version. The value is {@value #CURRENT_VERSION}
   *
   * @since 0.1.0
   */
  public static final byte CURRENT_VERSION = (byte) 0;

  /**
   * Encodes the {@link ServerStats} as per the Opencensus Summary Span specification.
   *
   * @param stats {@code ServerStats} to encode.
   * @return encoded byte array.
   * @since 0.1.0
   */
  public static byte[] toBytes(ServerStats stats) {
    // Should this be optimized to not include invalid values?

    ByteBuffer bb = ByteBuffer.allocate(ServerStatsFieldEnums.getTotalSize() + 1);
    bb.order(ByteOrder.LITTLE_ENDIAN);

    // put version
    bb.put(CURRENT_VERSION);

    bb.put((byte) ServerStatsFieldEnums.Id.SERVER_STATS_LB_LATENCY_ID.value());
    bb.putLong(stats.getLbLatencyNs());

    bb.put((byte) ServerStatsFieldEnums.Id.SERVER_STATS_SERVICE_LATENCY_ID.value());
    bb.putLong(stats.getServiceLatencyNs());

    bb.put((byte) ServerStatsFieldEnums.Id.SERVER_STATS_TRACE_OPTION_ID.value());
    bb.put(stats.getTraceOption());
    return bb.array();
  }

  /**
   * Decodes serialized byte array to create {@link ServerStats} as per Opencensus Summary Span
   * specification.
   *
   * @param serialized encoded {@code ServerStats} in byte array.
   * @return decoded {@code ServerStats}. null if decoding fails.
   * @since 0.1.0
   */
  public static ServerStats parseBytes(byte[] serialized)
      throws ServerStatsDeserializationException {
    final ByteBuffer bb = ByteBuffer.wrap(serialized);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    long serviceLatencyNs = 0L;
    long lbLatencyNs = 0L;
    byte traceOption = (byte) 0;

    // Check the version first.
    if (!bb.hasRemaining()) {
      throw new ServerStatsDeserializationException("Serialized ServerStats buffer is empty");
    }
    byte version = bb.get();

    if (version > CURRENT_VERSION || version < 0) {
      throw new ServerStatsDeserializationException("Invalid ServerStats version: " + version);
    }

    while (bb.hasRemaining()) {
      ServerStatsFieldEnums.Id id = ServerStatsFieldEnums.Id.valueOf((int) bb.get() & 0xFF);
      if (id == null) {
        // Skip remaining;
        bb.position(bb.limit());
      } else {
        switch (id) {
          case SERVER_STATS_LB_LATENCY_ID:
            lbLatencyNs = bb.getLong();
            break;
          case SERVER_STATS_SERVICE_LATENCY_ID:
            serviceLatencyNs = bb.getLong();
            break;
          case SERVER_STATS_TRACE_OPTION_ID:
            traceOption = bb.get();
            break;
        }
      }
    }
    try {
      return ServerStats.create(lbLatencyNs, serviceLatencyNs, traceOption);
    } catch (IllegalArgumentException e) {
      throw new ServerStatsDeserializationException(
          "Serialized ServiceStats contains invalid values: " + e.getMessage());
    }
  }
}
