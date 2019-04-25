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

import java.util.Map;
import javax.annotation.concurrent.Immutable;
import openconsensus.internal.Utils;

/**
 * The {@code BlankSpan} is a singleton class, which is the default {@link Span} that is used when
 * no {@code Span} implementation is available. All operations are no-op.
 *
 * <p>Used also to stop tracing, see {@link Tracer#withSpan}.
 *
 * @since 0.1.0
 */
@Immutable
public final class BlankSpan implements Span {
  /**
   * Singleton instance of this class.
   *
   * @since 0.1.0
   */
  public static final Span INSTANCE = new BlankSpan();

  private BlankSpan() {}

  @Override
  public void setAttribute(String key, String value) {
    Utils.checkNotNull(key, "key");
    Utils.checkNotNull(value, "value");
  }

  @Override
  public void setAttribute(String key, long value) {
    Utils.checkNotNull(key, "key");
  }

  @Override
  public void setAttribute(String key, double value) {
    Utils.checkNotNull(key, "key");
  }

  @Override
  public void setAttribute(String key, boolean value) {
    Utils.checkNotNull(key, "key");
  }

  @Override
  public void setAttribute(String key, AttributeValue value) {
    Utils.checkNotNull(key, "key");
    Utils.checkNotNull(value, "value");
  }

  @Override
  public void addEvent(String name) {}

  @Override
  public void addEvent(String name, Map<String, AttributeValue> attributes) {
    Utils.checkNotNull(name, "name");
    Utils.checkNotNull(attributes, "attributes");
  }

  @Override
  public void addEvent(Event event) {
    Utils.checkNotNull(event, "event");
  }

  @Override
  public void addLink(Link link) {
    Utils.checkNotNull(link, "link");
  }

  @Override
  public void setStatus(Status status) {
    Utils.checkNotNull(status, "status");
  }

  @Override
  public void updateName(String name) {
    Utils.checkNotNull(name, "name");
  }

  @Override
  public void end() {}

  @Override
  public SpanContext getContext() {
    return SpanContext.BLANK;
  }

  @Override
  public boolean isRecordingEvents() {
    return false;
  }

  @Override
  public String toString() {
    return "BlankSpan";
  }
}
