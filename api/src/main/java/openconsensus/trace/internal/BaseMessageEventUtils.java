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

package openconsensus.trace.internal;

import openconsensus.common.Internal;
import openconsensus.internal.Utils;
import openconsensus.trace.BaseMessageEvent;
import openconsensus.trace.MessageEvent;
import openconsensus.trace.NetworkEvent;

/** Helper class to convert/cast between for {@link MessageEvent} and {@link NetworkEvent}. */
@Internal
@SuppressWarnings("deprecation")
public final class BaseMessageEventUtils {
  /**
   * Cast or convert a {@link BaseMessageEvent} to {@link MessageEvent}.
   *
   * <p>Warning: if the input is a {@code NetworkEvent} and contains {@code kernelTimestamp}
   * information, this information will be dropped.
   *
   * @param event the {@code BaseMessageEvent} that is being cast or converted.
   * @return a {@code MessageEvent} representation of the input.
   */
  public static MessageEvent asMessageEvent(BaseMessageEvent event) {
    Utils.checkNotNull(event, "event");
    if (event instanceof MessageEvent) {
      return (MessageEvent) event;
    }
    NetworkEvent networkEvent = (NetworkEvent) event;
    MessageEvent.Type type =
        (networkEvent.getType() == NetworkEvent.Type.RECV)
            ? MessageEvent.Type.RECEIVED
            : MessageEvent.Type.SENT;
    return MessageEvent.builder(type, networkEvent.getMessageId())
        .setUncompressedMessageSize(networkEvent.getUncompressedMessageSize())
        .setCompressedMessageSize(networkEvent.getCompressedMessageSize())
        .build();
  }

  /**
   * Cast or convert a {@link BaseMessageEvent} to {@link NetworkEvent}.
   *
   * @param event the {@code BaseMessageEvent} that is being cast or converted.
   * @return a {@code NetworkEvent} representation of the input.
   */
  public static NetworkEvent asNetworkEvent(BaseMessageEvent event) {
    Utils.checkNotNull(event, "event");
    if (event instanceof NetworkEvent) {
      return (NetworkEvent) event;
    }
    MessageEvent messageEvent = (MessageEvent) event;
    NetworkEvent.Type type =
        (messageEvent.getType() == MessageEvent.Type.RECEIVED)
            ? NetworkEvent.Type.RECV
            : NetworkEvent.Type.SENT;
    return NetworkEvent.builder(type, messageEvent.getMessageId())
        .setUncompressedMessageSize(messageEvent.getUncompressedMessageSize())
        .setCompressedMessageSize(messageEvent.getCompressedMessageSize())
        .build();
  }

  private BaseMessageEventUtils() {}
}
