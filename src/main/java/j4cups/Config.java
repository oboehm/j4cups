/*
 * Copyright (c) 2018 by Oliver Boehm
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
package j4cups;

import j4cups.util.XProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

/**
 * Configuration stuff for J4Cups
 *
 * @author oboehm
 * @since 0.6 (13.12.2018)
 */
public final class Config {
    
    private static final Logger LOG = LoggerFactory.getLogger(Config.class);

    private final Properties properties;

    /**
     * Creates a new configuration for a standalone server.
     */
    public Config() {
        this("j4cups/standalone.properties");
    }
    
    /**
     * Instantiates a new Configuration with the given resource
     *
     * @param resource the resource, e.g. "j4cups/proxy.properties"
     */
    public Config(String resource) {
        this.properties = loadProperties(resource);
    }

    private static Properties loadProperties(String resource) {
        Properties properties = new XProperties();
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalArgumentException("resource '" + resource + "' is not in classpath");
            }
            properties.load(input);
            LOG.info("Properties are loaded from '{}'.", resource);
            return properties;
        } catch (IOException ex) {
            throw new IllegalArgumentException("cannot read init parameters from resource " + resource, ex);
        }
    }

    /**
     * Fluent API for setting a property.
     *
     * @param key   the key
     * @param value the value
     * @return the config
     */
    public Config withProperty(String key, String value) {
        this.properties.setProperty(key, value);
        return this;
    }

    /**
     * Fluent API for setting the server port.
     *
     * @param port the port
     * @return the config
     */
    public Config withServerPort(int port) {
        this.setIntProperty("j4cups.server.port", port);
        return this;
    }

    /**
     * Gets server port.
     *
     * @return the server port
     */
    public int getServerPort() {
        return getIntProperty("j4cups.server.port");
    }

    /**
     * If the server acts like a proxy you set the URI to the forwardded
     * CUPS server here.
     *
     * @param uri the URI where requests are forwarded
     * @return the config
     */
    public Config withServerForwardURI(String uri) {
        this.properties.setProperty("j4cups.server.forwardURI", uri);
        return this;
    }

    /**
     * Gets server forward URI.
     *
     * @return the server forward uri
     */
    public URI getServerForwardURI() {
        String uri = this.properties.getProperty("j4cups.server.forwardURI");
        if (uri == null) {
            return new File(this.properties.getProperty("j4cups.server.recordDIR")).toURI();
        } else {
            return URI.create(uri);
        }
    }
    
    private void setIntProperty(String key, int value) {
        this.properties.setProperty(key, Integer.toString(value));
    }
    
    private int getIntProperty(String key) {
        return Integer.parseInt(this.properties.getProperty(key));
    }
    
}
