/*
 * Copyright (c) 2019 by Oliver Boehm
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
 * (c)reated 2019-07-19 by oliver (ob@oasd.de)
 */

package j4cups.server;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link IppProxyHandler}.
 */
final class IppProxyHandlerTest {

    @Test
    void getForwardURI() {
        IppProxyHandler handler = new IppProxyHandler(URI.create("http://localhost:631"));
        URI forwardURI = handler.getForwardURI();
        assertEquals("ipp", forwardURI.getScheme());
    }

}