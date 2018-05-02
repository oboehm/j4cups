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
 * (c)reated 02.05.18 by oliver (ob@oasd.de)
 */
package j4cups.server.http;

import j4cups.protocol.AbstractIppTest;
import j4cups.protocol.IppRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link IppEntity}.
 */
class IppEntityTest {

    @Test
    void testIppEntity() {
        IppRequest request = AbstractIppTest.REQUEST_GET_JOBS;
        IppEntity entity = new IppEntity(request);
        assertEquals(request.getLength(), entity.getContentLength());
    }

}
