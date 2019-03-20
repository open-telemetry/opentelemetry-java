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

package openconsensus.trace;

/**
 * Superclass for {@link MessageEvent} and {@link NetworkEvent} to resolve API backward
 * compatibility issue.
 *
 * <p>{@code SpanData.create} can't be overloaded with parameter types that differ only in the type
 * of the TimedEvent, because the signatures are the same after generic type erasure. {@code
 * BaseMessageEvent} allows the same method to accept both {@code TimedEvents<NetworkEvent>} and
 * {@code TimedEvents<MessageEvent>}.
 *
 * <p>This class should only be extended by {@code NetworkEvent} and {@code MessageEvent}.
 *
 * @deprecated This class is for internal use only.
 * @since 0.1.0
 */
@Deprecated
public abstract class BaseMessageEvent {
  // package protected to avoid users to extend it.
  BaseMessageEvent() {}
}
