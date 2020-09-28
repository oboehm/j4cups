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

import org.hamcrest.text.IsEmptyString;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link Config}.
 */
class ConfigTest {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigTest.class);
    private Config config = new Config();

    @Test
    void getServerPort() {
        assertEquals(631, config.getServerPort());
    }
    
    @Test
    void getServerForwardURI() {
        assertThat(config.getServerForwardURI().toString(), startsWith("file:/"));
    }

    @Test
    void withServerForwardURI() {
        URI uri = URI.create("ipp://test:123");
        config = config.withServerForwardURI(uri.toString());
        assertEquals(uri, config.getServerForwardURI());
    }

    @Test
    void withServerPort() {
        config = config.withServerPort(42);
        assertEquals(42, config.getServerPort());
    }

    @Test
    void getServerInfo() {
        String info = config.getServerInfo();
        assertThat(info, not(IsEmptyString.emptyString()));
        assertThat(info, not(containsString("$")));
        LOG.info("info={}", info);
    }

    @Test
    void createInvalidConfig() {
        assertThrows(IllegalArgumentException.class, () -> new Config("nirwana"));
    }

}
