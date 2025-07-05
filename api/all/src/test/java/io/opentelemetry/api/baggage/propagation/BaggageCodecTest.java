/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage.propagation;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BaggageCodecTest {

  @ParameterizedTest
  @CsvSource(
      quoteCharacter = ';', // default is a quote character "'", which collides with %27 character.
      value = {
        "%21,!", "%23,#", "%24,$", "%25,%", "%26,&", "%27,'", "%28,(", "%29,)", "%2A,*", "%2B,+",
        "%2D,-", "%2E,.", "%2F,/", "%30,0", "%31,1", "%32,2", "%33,3", "%34,4", "%35,5", "%36,6",
        "%37,7", "%38,8", "%39,9", "%3A,:", "%3C,<", "%3D,=", "%3E,>", "%3F,?", "%40,@", "%41,A",
        "%42,B", "%43,C", "%44,D", "%45,E", "%46,F", "%47,G", "%48,H", "%49,I", "%4A,J", "%4B,K",
        "%4C,L", "%4D,M", "%4E,N", "%4F,O", "%50,P", "%51,Q", "%52,R", "%53,S", "%54,T", "%55,U",
        "%56,V", "%57,W", "%58,X", "%59,Y", "%5A,Z", "%5B,[", "%5D,]", "%5E,^", "%5F,_", "%60,`",
        "%61,a", "%62,b", "%63,c", "%64,d", "%65,e", "%66,f", "%67,g", "%68,h", "%69,i", "%6A,j",
        "%6B,k", "%6C,l", "%6D,m", "%6E,n", "%6F,o", "%70,p", "%71,q", "%72,r", "%73,s", "%74,t",
        "%75,u", "%76,v", "%77,w", "%78,x", "%79,y", "%7A,z", "%7B,{", "%7C,|", "%7D,}", "%7E,~",
      })
  void shouldDecodePercentEncodedValues(String percentEncoded, String expectedDecoded) {
    assertThat(BaggageCodec.decode(percentEncoded, StandardCharsets.UTF_8))
        .isEqualTo(expectedDecoded);
  }

  @Test
  void shouldIgnoreIfMalformedData() {
    assertThat(BaggageCodec.decode("%", StandardCharsets.UTF_8)).isEqualTo("");
    assertThat(BaggageCodec.decode("%1", StandardCharsets.UTF_8)).isEqualTo("");
  }
}
