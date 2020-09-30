/*
 * Copyright (c) 2018-2020 by Oliver Boehm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * (c)reated 13.12.2018 by oboehm (ob@oasd.de)
 */
package j4cups

import j4cups.util.XProperties
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.*

/**
 * Configuration stuff for J4Cups.
 *
 * @author oboehm
 * @since 0.6 (13.12.2018)
 */
class Config @JvmOverloads constructor(resource: String = "j4cups/standalone.properties") {

    private val properties: Properties

    /**
     * Fluent API for setting a property.
     *
     * @param key   the key
     * @param value the value
     * @return the config
     */
    fun withProperty(key: String, value: String): Config {
        properties.setProperty(key, value)
        return this
    }

    /**
     * Fluent API for setting the server port.
     *
     * @param port the port
     * @return the config
     */
    fun withServerPort(port: Int): Config {
        return withProperty("j4cups.server.port", Integer.toString(port))
    }

    /**
     * Gets server port.
     *
     * @return the server port
     */
    val serverPort: Int
        get() = getIntProperty("j4cups.server.port")

    /**
     * If the server acts like a proxy you set the URI to the forwardded
     * CUPS server here.
     *
     * @param uri the URI where requests are forwarded
     * @return the config
     */
    fun withServerForwardURI(uri: String): Config {
        return withProperty("j4cups.server.forwardURI", uri)
    }

    /**
     * Gets server forward URI.
     *
     * @return the server forward uri
     */
    val serverForwardURI: URI
        get() {
            val uri = properties.getProperty("j4cups.server.forwardURI")
            return if (uri == null) {
                File(properties.getProperty("j4cups.server.recordDIR")).toURI()
            } else {
                URI.create(uri)
            }
        }
    val serverInfo: String
        get() = properties.getProperty("j4cups.server.info")

    private fun getIntProperty(key: String): Int {
        return properties.getProperty(key).toInt()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(Config::class.java)
        private fun loadProperties(resource: String): Properties {
            val properties: Properties = XProperties()
            try {
                Thread.currentThread().contextClassLoader.getResourceAsStream(resource).use { input ->
                    requireNotNull(input) { "resource '$resource' is not in classpath" }
                    properties.load(input)
                    LOG.info("Properties are loaded from '{}'.", resource)
                    return properties
                }
            } catch (ex: IOException) {
                throw IllegalArgumentException("cannot read init parameters from resource $resource", ex)
            }
        }
    }

    init {
        properties = loadProperties(resource)
    }

}