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

package io.opentelemetry.distributedcontext;

import io.opentelemetry.context.NoopScope;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.BinaryFormat;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.distributedcontext.unsafe.ContextUtils;
import io.opentelemetry.internal.Utils;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * No-op implementations of {@link DistributedContextManager}.
 *
 * @since 0.1.0
 */
@ThreadSafe
public final class DefaultDistributedContextManager implements DistributedContextManager {
  private static final DefaultDistributedContextManager INSTANCE =
      new DefaultDistributedContextManager();
  private static final BinaryFormat<DistributedContext> BINARY_FORMAT = new NoopBinaryFormat();
  private static final HttpTextFormat<DistributedContext> HTTP_TEXT_FORMAT =
      new NoopHttpTextFormat();

  /**
   * Returns a {@code DistributedContextManager} singleton that is the default implementation for
   * {@link DistributedContextManager}.
   *
   * @return a {@code DistributedContextManager} singleton that is the default implementation for
   *     {@link DistributedContextManager}.
   * @since 0.1.0
   */
  public static DistributedContextManager getInstance() {
    return INSTANCE;
  }

  @Override
  public DistributedContext getCurrentContext() {
    return ContextUtils.getValue();
  }

  @Override
  public DistributedContext.Builder contextBuilder() {
    return new NoopDistributedContextBuilder();
  }

  @Override
  public Scope withContext(DistributedContext distContext) {
    return ContextUtils.withDistributedContext(distContext);
  }

  @Override
  public BinaryFormat<DistributedContext> getBinaryFormat() {
    return BINARY_FORMAT;
  }

  @Override
  public HttpTextFormat<DistributedContext> getHttpTextFormat() {
    return HTTP_TEXT_FORMAT;
  }

  @Immutable
  private static final class NoopDistributedContextBuilder implements DistributedContext.Builder {
    @Override
    public DistributedContext.Builder setParent(DistributedContext parent) {
      Utils.checkNotNull(parent, "parent");
      return this;
    }

    @Override
    public DistributedContext.Builder setNoParent() {
      return this;
    }

    @Override
    public DistributedContext.Builder put(
        AttributeKey key, AttributeValue value, AttributeMetadata attrMetadata) {
      Utils.checkNotNull(key, "key");
      Utils.checkNotNull(value, "value");
      Utils.checkNotNull(attrMetadata, "attributeMetadata");
      return this;
    }

    @Override
    public DistributedContext.Builder remove(AttributeKey key) {
      Utils.checkNotNull(key, "key");
      return this;
    }

    @Override
    public DistributedContext build() {
      return EmptyDistributedContext.INSTANCE;
    }

    @Override
    public Scope buildScoped() {
      return NoopScope.INSTANCE;
    }
  }

  @Immutable
  private static final class NoopBinaryFormat implements BinaryFormat<DistributedContext> {
    static final byte[] EMPTY_BYTE_ARRAY = {};

    @Override
    public byte[] toByteArray(DistributedContext distContext) {
      Utils.checkNotNull(distContext, "distContext");
      return EMPTY_BYTE_ARRAY;
    }

    @Override
    public DistributedContext fromByteArray(byte[] bytes) {
      Utils.checkNotNull(bytes, "bytes");
      return EmptyDistributedContext.INSTANCE;
    }
  }

  @Immutable
  private static final class NoopHttpTextFormat implements HttpTextFormat<DistributedContext> {
    @Override
    public List<String> fields() {
      return Collections.emptyList();
    }

    @Override
    public <C> void inject(DistributedContext distContext, C carrier, Setter<C> setter) {
      Utils.checkNotNull(distContext, "distContext");
      Utils.checkNotNull(carrier, "carrier");
      Utils.checkNotNull(setter, "setter");
    }

    @Override
    public <C> DistributedContext extract(C carrier, Getter<C> getter) {
      Utils.checkNotNull(carrier, "carrier");
      Utils.checkNotNull(getter, "getter");
      return EmptyDistributedContext.INSTANCE;
    }
  }
}
