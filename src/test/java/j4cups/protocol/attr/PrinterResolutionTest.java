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
 * (c)reated 09.05.2018 by oboehm (ob@oasd.de)
 */
package j4cups.protocol.attr;

import j4cups.protocol.enums.PrintQuality;
import org.junit.jupiter.api.Test;
import patterntesting.runtime.junit.ArrayTester;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PrinterResolution}.
 */
class PrinterResolutionTest {
    
    private final PrinterResolution printerResolution = PrinterResolution.of(600, 600, PrintQuality.DRAFT);

    @Test
    void testToByteArray() {
        byte[] expected = { 0, 0, 2, 88, 0, 0, 2, 88, 3 };
        ArrayTester.assertEquals(expected, printerResolution.toByteArray());
    }

}
