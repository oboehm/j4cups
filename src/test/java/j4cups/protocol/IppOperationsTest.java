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
 * (c)reated 08.02.2018 by oboehm (boehm@javatux.de)
 */
package j4cups.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit-Tests for {@link IppOperations}.
 *
 * @author Oli B.
 */
public class IppOperationsTest {

    @Test
    public void testOf() {
        assertEquals(IppOperations.GET_JOBS, IppOperations.of(0x000a));
    }

    @Test
    public void testToString() {
        assertEquals("Print-Job", IppOperations.PRINT_JOB.toString());
    }

    /**
     * This is the test for the operation ids between 0x0013-0x3fff.
     */
    @Test
    public void testOfAdditionalRegisteredOperations() {
        assertEquals(IppOperations.ADDITIONAL_REGISTERED_OPERATIONS, IppOperations.of(0x3eee));
    }

    /**
     * This is the test for the operation ids between 0x4000-0x7fff.
     */
    @Test
    public void testOfReservedForVendorExtensions() {
        assertEquals(IppOperations.RESERVED_FOR_VENDOR_EXTENSIONS, IppOperations.of(0x4444));
    }

}
