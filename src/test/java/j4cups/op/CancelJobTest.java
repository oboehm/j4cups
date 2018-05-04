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
 * (c)reated 29.04.18 by oliver (ob@oasd.de)
 */
package j4cups.op;

import j4cups.protocol.attr.Attribute;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CancelJob}.
 */
final class CancelJobTest {

    private static final Logger LOG = LoggerFactory.getLogger(CancelJobTest.class);
    private final CancelJob op = new CancelJob();

    /**
     * The requesting-user-name should be part of the generated IPP request.
     */
    @Test
    public void testRequestingUserName() {
        String expected = SystemUtils.USER_NAME;
        Attribute userAttribute = op.getAttribute("requesting-user-name");
        assertNotNull(userAttribute);
        LOG.info("Requesting user name is {}.", userAttribute);
        assertEquals(expected, userAttribute.getStringValue());
    }

}
