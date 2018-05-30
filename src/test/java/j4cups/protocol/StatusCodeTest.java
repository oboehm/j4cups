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
 * (c)reated 14.02.2018 by oboehm (ob@oasd.de)
 */
package j4cups.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link StatusCode}.
 */
class StatusCodeTest {

    @Test
    void testOf() {
        assertEquals(StatusCode.SUCCESSFUL_OK, StatusCode.of(0x0000));
    }
    
    @Test
    void testToString() {
        assertEquals("client-error-forbidden", StatusCode.CLIENT_ERROR_FORBIDDEN.toString());
    }

    @Test
    void testIsSuccessful() {
        assertTrue(StatusCode.SUCCESSFUL_OK_CONFLICTING_ATTRIBUTES.isSuccessful());
    }

    @Test
    void testIsNotSuccessful() {
        assertFalse(StatusCode.CLIENT_ERROR_BAD_REQUEST.isSuccessful());
    }

}
