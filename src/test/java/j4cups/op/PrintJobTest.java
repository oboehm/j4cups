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
import org.junit.jupiter.api.Test;
import patterntesting.runtime.junit.ArrayTester;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link PrintJob}.
 */
final class PrintJobTest {

    private final PrintJob op = new PrintJob();

    /**
     * The created IPP request should contain "orientation-requested" as
     * attribute.
     */
    @Test
    public void testOrientationRequested() {
        Attribute attr = op.getAttribute("orientation-requested");
        assertNotNull(attr);
    }

    /**
     * Test method for {@link PrintJob#setData(byte[])}.
     */
    @Test
    public void testSetData() {
        byte[] data = "test".getBytes(StandardCharsets.UTF_8);
        op.setData(data);
        ArrayTester.assertEquals(data, op.getIppRequest().getData());
    }

}
