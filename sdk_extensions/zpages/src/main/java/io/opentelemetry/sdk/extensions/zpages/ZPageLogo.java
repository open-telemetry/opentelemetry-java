/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.sdk.extensions.zpages;

import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import java.io.InputStream;
import java.util.logging.Logger;

final class ZPageLogo {
  private ZPageLogo() {}

  /**
   * Get OpenTelemetry logo in base64 encoding.
   *
   * @return OpenTelemetry logo in base64 encoding.
   */
  public static String getLogoBase64() {
    try {
      InputStream in = ZPageLogo.class.getClassLoader().getResourceAsStream("logo.png");
      byte[] bytes = ByteStreams.toByteArray(in);
      return BaseEncoding.base64().encode(bytes);
    } catch (Throwable t) {
      Logger.getLogger(ZPageLogo.class.getName())
          .warning("Error while getting OpenTelemetry Logo: " + t.toString());
      return "";
    }
  }

  /**
   * Get OpenTelemetry favicon in base64 encoding.
   *
   * @return OpenTelemetry favicon in base64 encoding.
   */
  public static String getFaviconBase64() {
    try {

      InputStream in = ZPageLogo.class.getClassLoader().getResourceAsStream("favicon.png");
      byte[] bytes = ByteStreams.toByteArray(in);
      return BaseEncoding.base64().encode(bytes);
    } catch (Throwable t) {
      Logger.getLogger(ZPageLogo.class.getName())
          .warning("Error while getting OpenTelemetry Logo: " + t.toString());
      return "";
    }
  }
}
