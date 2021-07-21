/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage.propagation;

import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.api.baggage.BaggageEntryMetadata;

/**
 * Implements single-pass Baggage parsing in accordance with https://w3c.github.io/baggage/ Key /
 * value are restricted in accordance with https://www.ietf.org/rfc/rfc2616.txt
 *
 * <p>Note: following aspects are not specified in RFC: - some invalid elements (key or value) -
 * parser will include valid ones, disregard invalid - empty "value" is regarded as invalid - meta -
 * anything besides element terminator (comma)
 */
class Parser {

  private enum State {
    KEY,
    VALUE,
    META
  }

  private final String baggageHeader;

  private final Element key = Element.createKeyElement();
  private final Element value = Element.createValueElement();
  private String meta;

  private State state;
  private int metaStart;

  private boolean skipToNext;

  public Parser(String baggageHeader) {
    this.baggageHeader = baggageHeader;
    reset(0);
  }

  void parseInto(BaggageBuilder baggageBuilder) {
    for (int i = 0, n = baggageHeader.length(); i < n; i++) {
      char current = baggageHeader.charAt(i);

      if (skipToNext) {
        if (current == ',') {
          reset(i + 1);
        }
        continue;
      }

      switch (current) {
        case '=':
          {
            if (state == State.KEY) {
              if (key.tryTerminating(i, baggageHeader)) {
                setState(State.VALUE, i + 1);
              } else {
                skipToNext = true;
              }
            }
            break;
          }
        case ';':
          {
            if (state == State.VALUE) {
              skipToNext = !value.tryTerminating(i, baggageHeader);
              setState(State.META, i + 1);
            }
            break;
          }
        case ',':
          {
            switch (state) {
              case VALUE:
                value.tryTerminating(i, baggageHeader);
                break;
              case META:
                meta = baggageHeader.substring(metaStart, i).trim();
                break;
              case KEY: // none
            }
            baggageBuilder.put(key.getValue(), value.getValue(), BaggageEntryMetadata.create(meta));
            reset(i + 1);
            break;
          }
        default:
          {
            switch (state) {
              case KEY:
                skipToNext = !key.tryNextChar(current, i);
                break;
              case VALUE:
                skipToNext = !value.tryNextChar(current, i);
                break;
              case META: // none
            }
          }
      }
    }
    // need to finish parsing if there was no list element termination comma
    switch (state) {
      case KEY:
        break;
      case META:
        {
          String rest = baggageHeader.substring(metaStart).trim();
          baggageBuilder.put(key.getValue(), value.getValue(), BaggageEntryMetadata.create(rest));
          break;
        }
      case VALUE:
        {
          if (!skipToNext) {
            value.tryTerminating(baggageHeader.length(), baggageHeader);
            baggageBuilder.put(key.getValue(), value.getValue());
            break;
          }
        }
    }
  }

  /**
   * Resets parsing state, preparing to start a new list element (see spec).
   *
   * @param index index where parser should start new element scan
   */
  private void reset(int index) {
    this.skipToNext = false;
    this.state = State.KEY;
    this.key.reset(index);
    this.value.reset(index);
    this.meta = "";
    this.metaStart = 0;
  }

  /** Switches parser state (element of a list member). */
  private void setState(State state, int start) {
    this.state = state;
    this.metaStart = start;
  }
}
