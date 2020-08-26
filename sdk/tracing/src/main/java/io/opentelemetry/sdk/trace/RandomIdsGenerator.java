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

package io.opentelemetry.sdk.trace;

import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The default {@link IdsGenerator} which generates IDs as random numbers using {@link
 * ThreadLocalRandom}.
 */
public final class RandomIdsGenerator implements IdsGenerator {

  private static final long INVALID_ID = 0;

  @Override
  public CharSequence generateSpanId() {
    long id;
    ThreadLocalRandom random = ThreadLocalRandom.current();
    do {
      id = random.nextLong();
    } while (id == INVALID_ID);
    return new SpanIdWrapper(id);
  }

  @Override
  public CharSequence generateTraceId() {
    long idHi;
    long idLo;
    ThreadLocalRandom random = ThreadLocalRandom.current();
    do {
      idHi = random.nextLong();
      idLo = random.nextLong();
    } while (idHi == INVALID_ID && idLo == INVALID_ID);
    return new TraceIdWrapper(idHi, idLo);
  }

  static final class TraceIdWrapper implements CharSequence {
    private final long idHi;
    private final long idLo;
    private volatile CharSequence chars;

    private TraceIdWrapper(long idHi, long idLo) {
      this.idHi = idHi;
      this.idLo = idLo;
    }

    @Override
    public int length() {
      return 32;
    }

    @Override
    public char charAt(int index) {
      return getCharSequence().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
      return getCharSequence().subSequence(start, end);
    }

    public long getIdHi() {
      return idHi;
    }

    public long getIdLo() {
      return idLo;
    }

    @Override
    public String toString() {
      return getCharSequence().toString();
    }

    private CharSequence getCharSequence() {
      if (chars == null) {
        chars = TraceId.fromLongs(idHi, idLo);
      }
      return chars;
    }
  }

  static final class SpanIdWrapper implements CharSequence {
    private final long id;
    private volatile CharSequence chars;

    private SpanIdWrapper(long id) {
      this.id = id;
    }

    @Override
    public int length() {
      return 16;
    }

    @Override
    public char charAt(int index) {
      return getCharSequence().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
      return getCharSequence().subSequence(start, end);
    }

    public long getId() {
      return id;
    }

    @Override
    public String toString() {
      return getCharSequence().toString();
    }

    private CharSequence getCharSequence() {
      if (chars == null) {
        chars = SpanId.fromLong(id);
      }
      return chars;
    }
  }
}
