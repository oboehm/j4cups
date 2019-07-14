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

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link Config}.
 */
class ConfigTest {
    
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

}
