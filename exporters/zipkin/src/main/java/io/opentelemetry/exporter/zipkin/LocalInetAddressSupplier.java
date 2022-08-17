/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

class LocalInetAddressSupplier implements Supplier<InetAddress> {

  private static final Logger logger = Logger.getLogger(LocalInetAddressSupplier.class.getName());
  private static final LocalInetAddressSupplier INSTANCE =
      new LocalInetAddressSupplier(findLocalIp());
  @Nullable private final InetAddress inetAddress;

  private LocalInetAddressSupplier(@Nullable InetAddress inetAddress) {
    this.inetAddress = inetAddress;
  }

  @Nullable
  @Override
  public InetAddress get() {
    return inetAddress;
  }

  /** Logic borrowed from brave.internal.Platform.produceLocalEndpoint */
  @Nullable
  private static InetAddress findLocalIp() {
    try {
      Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
      while (nics.hasMoreElements()) {
        NetworkInterface nic = nics.nextElement();
        Enumeration<InetAddress> addresses = nic.getInetAddresses();
        while (addresses.hasMoreElements()) {
          InetAddress address = addresses.nextElement();
          if (address.isSiteLocalAddress()) {
            return address;
          }
        }
      }
    } catch (Exception e) {
      // don't crash the caller if there was a problem reading nics.
      logger.log(Level.FINE, "error reading nics", e);
    }
    return null;
  }

  static LocalInetAddressSupplier getInstance() {
    return INSTANCE;
  }
}
