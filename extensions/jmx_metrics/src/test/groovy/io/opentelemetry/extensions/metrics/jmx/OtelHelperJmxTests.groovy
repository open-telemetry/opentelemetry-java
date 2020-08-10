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
package io.opentelemetry.extensions.metrics.jmx

import static java.lang.management.ManagementFactory.getPlatformMBeanServer
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

import javax.management.ObjectName
import javax.management.remote.JMXConnectorServer
import javax.management.remote.JMXConnectorServerFactory
import javax.management.remote.JMXServiceURL
import org.junit.After
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4)
class OtelHelperJmxTests {

    static String thingName = 'io.opentelemetry.extensions.metrics.jmx:type=OtelHelperJmxTests.Thing'

    JMXConnectorServer jmxServer

    @BeforeClass
    static void registerMBean() {
        def thing = new Thing()
        def mbeanServer = getPlatformMBeanServer()
        mbeanServer.registerMBean(thing, new ObjectName(thingName))
    }

    @After
    void teardownRMIServer() {
        jmxServer.stop()
    }

    private JMXServiceURL setupServer(Map env) {
        def serviceUrl = new JMXServiceURL('rmi', 'localhost', 0)
        jmxServer = JMXConnectorServerFactory.newJMXConnectorServer(serviceUrl, env, getPlatformMBeanServer())
        jmxServer.start()
        return jmxServer.getAddress()
    }

    private OtelHelper setupHelper(JmxConfig config) {
        return new OtelHelper(new JmxClient(config), new GroovyUtils(config))
    }

    private void verifyClient(JmxConfig config) {
        config.groovyScript = "myscript.groovy"
        config.validate()
        def otel = setupHelper(config)
        def mbeans = otel.queryJmx(thingName)

        assertEquals(1, mbeans.size())
        assertEquals('This is the attribute', mbeans[0].SomeAttribute)
    }

    @Test
    void NoAuthentication() {
        def serverAddr = setupServer([:])
        def config = new JmxConfig().tap {
            serviceUrl = serverAddr
        }
        verifyClient(config)
    }

    @Test
    void PasswordAuthentication() {
        def pwFile = ClassLoader.getSystemClassLoader().getResource('jmxremote.password').getPath()
        def serverAddr = setupServer(['jmx.remote.x.password.file':pwFile])

        def config = new JmxConfig().tap {
            serviceUrl = serverAddr
            username = 'wrongUsername'
            password = 'wrongPassword'
        }
        try {
            verifyClient(config)
            assertTrue('Authentication should have failed.', false)
        } catch (final SecurityException e) {
            // desired
        }

        config = new JmxConfig().tap {
            serviceUrl = serverAddr
            username = 'correctUsername'
            password = 'correctPassword'
        }
        verifyClient(config)
    }

    interface ThingMBean {

        String getSomeAttribute()

    }

    static class Thing implements ThingMBean {

        @Override
        String getSomeAttribute() {
            return 'This is the attribute'
        }

    }

}
