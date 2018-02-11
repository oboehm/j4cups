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
 * (c)reated 11.02.2018 by Oli B. (boehm@javatux.de)
 */
package j4cups.protocol.attr;

import j4cups.protocol.tags.ValueTags;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link Attribute}. The DATA for this tests are recorded
 * during an installation of a printer in CUPS, where the header of the IPP
 * request (the first 9 bytes) were removed.
 */
public final class AttributeTest {

    private static final byte[] DATA =
            {71, 0, 18, 97, 116, 116, 114, 105, 98, 117, 116, 101, 115, 45, 99, 104, 97, 114, 115, 101, 116, 0, 5, 117,
                    116, 102, 45, 56, 72, 0};
    private static final Attribute ATTRIBUTE = new Attribute(ByteBuffer.wrap(DATA));

    @Test
    void getValueTag() {
        assertEquals(ValueTags.CHARSET, ATTRIBUTE.getValueTag());
    }

    @Test
    void getName() {
        assertEquals("attributes-charset", ATTRIBUTE.getName());
    }

    @Test
    void getStringValue() {
        assertEquals("utf-8", ATTRIBUTE.getStringValue());
    }

}
