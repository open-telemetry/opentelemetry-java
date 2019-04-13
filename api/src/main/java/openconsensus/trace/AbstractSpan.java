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
import openconsensus.trace.data.AttributeValue;
import openconsensus.trace.data.Event;
import openconsensus.trace.data.Link;
import openconsensus.trace.data.MessageEvent;
import openconsensus.trace.data.Status;

/**
 * An abstract class that implements {@code Span}.
 *
 * <p>Users are encouraged to extend this class for convenience.
 *
 * @since 0.1.0
 */
public abstract class AbstractSpan implements Span {
  @Override
  public abstract void setAttribute(String key, String value);

  @Override
  public abstract void setAttribute(String key, long value);

  @Override
  public abstract void setAttribute(String key, double value);

  @Override
  public abstract void setAttribute(String key, boolean value);

  @Override
  public abstract void setAttribute(String key, AttributeValue value);

  @Override
  public abstract void addEvent(String name);

  @Override
  public abstract void addEvent(String name, Map<String, AttributeValue> attributes);

  @Override
  public abstract void addEvent(Event event);

  @Override
  public abstract void addMessageEvent(MessageEvent messageEvent);

  @Override
  public abstract void addLink(Link link);

  @Override
  public abstract void setStatus(Status status);

  @Override
  public abstract void end();

  @Override
  public abstract SpanContext getContext();

  @Override
  public abstract boolean isRecordingEvents();

  protected AbstractSpan() {}
}
