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

package openconsensus.opencensusshim.trace;

import java.util.Map;
import javax.annotation.concurrent.Immutable;
import openconsensus.opencensusshim.internal.Utils;

/**
 * The {@code BlankSpan} is a singleton class, which is the default {@link Span} that is used when
 * no {@code Span} implementation is available. All operations are no-op.
 *
 * <p>Used also to stop tracing, see {@link Tracer#withSpan}.
 *
 * @since 0.1.0
 */
@Immutable
public final class BlankSpan extends Span {
  /**
   * Singleton instance of this class.
   *
   * @since 0.1.0
   */
  public static final BlankSpan INSTANCE = new BlankSpan();

  private BlankSpan() {
    super(SpanContext.INVALID, null);
  }

  /** No-op implementation of the {@link Span#putAttribute(String, AttributeValue)} method. */
  @Override
  public void putAttribute(String key, AttributeValue value) {
    Utils.checkNotNull(key, "key");
    Utils.checkNotNull(value, "value");
  }

  /** No-op implementation of the {@link Span#putAttributes(Map)} method. */
  @Override
  public void putAttributes(Map<String, AttributeValue> attributes) {
    Utils.checkNotNull(attributes, "attributes");
  }

  /** No-op implementation of the {@link Span#addAnnotation(String, Map)} method. */
  @Override
  public void addAnnotation(String description, Map<String, AttributeValue> attributes) {
    Utils.checkNotNull(description, "description");
    Utils.checkNotNull(attributes, "attributes");
  }

  /** No-op implementation of the {@link Span#addAnnotation(Annotation)} method. */
  @Override
  public void addAnnotation(Annotation annotation) {
    Utils.checkNotNull(annotation, "annotation");
  }

  /** No-op implementation of the {@link Span#addMessageEvent(MessageEvent)} method. */
  @Override
  public void addMessageEvent(MessageEvent messageEvent) {
    Utils.checkNotNull(messageEvent, "messageEvent");
  }

  /** No-op implementation of the {@link Span#addLink(Link)} method. */
  @Override
  public void addLink(Link link) {
    Utils.checkNotNull(link, "link");
  }

  @Override
  public void setStatus(Status status) {
    Utils.checkNotNull(status, "status");
  }

  /** No-op implementation of the {@link Span#end(EndSpanOptions)} method. */
  @Override
  public void end(EndSpanOptions options) {
    Utils.checkNotNull(options, "options");
  }

  @Override
  public String toString() {
    return "BlankSpan";
  }
}
