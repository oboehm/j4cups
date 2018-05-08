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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import patterntesting.runtime.junit.ArrayTester;

import java.nio.ByteBuffer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link Attribute}. The DATA for this tests are recorded
 * during an installation of a printer in CUPS, where the header of the IPP
 * request (the first 9 bytes) were removed.
 */
final class AttributeTest {

    private static final Logger LOG = LoggerFactory.getLogger(AttributeTest.class);
    private static final byte[] DATA =
            {71, 0, 18, 97, 116, 116, 114, 105, 98, 117, 116, 101, 115, 45, 99, 104, 97, 114, 115, 101, 116, 0, 5, 117,
                    116, 102, 45, 56};
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

    @Test
    void testToString() {
        LOG.info("ATTRIBUTE = {}", ATTRIBUTE);
        assertThat(ATTRIBUTE.toString(), containsString(ATTRIBUTE.getName()));
    }

    @Test
    void testToLongString() {
        String s = ATTRIBUTE.toLongString();
        LOG.info("ATTRIBUTE = {}", s);
        assertThat(ATTRIBUTE.toString(), not(containsString("...")));
    }

    @Test
    public void testToStringNumberAttribute() {
        Attribute numberAttribute = Attribute.of(ValueTags.INTEGER, "number", 4711);
        LOG.info("numberAttribute = {}", numberAttribute);
        assertThat(numberAttribute.toString(), containsString("4711"));
    }
    
    @Test
    public void testIsMultiValue() {
        assertThat(ATTRIBUTE.isMultiValue(), is(false));
    }
    
    @Test
    void testMultiValue() {
        byte[] multiData = {68, 0, 20, 114, 101, 113, 117, 101, 115, 116, 101, 100, 45, 97, 116, 116, 114,
                        105, 98, 117, 116, 101, 115, 0, 16, 99, 111, 112, 105, 101, 115, 45, 115, 117, 112, 112, 111, 114,
                        116, 101, 100, 68, 0, 0, 0, 12, 99, 117, 112, 115, 45, 118, 101, 114, 115, 105, 111, 110, 68, 0, 0,
                        0, 25, 100, 111, 99, 117, 109, 101, 110, 116, 45, 102, 111, 114, 109, 97, 116, 45, 115, 117, 112,
                        112, 111, 114, 116, 101, 100, 68, 0, 0, 0, 13, 109, 97, 114, 107, 101, 114, 45, 99, 111, 108, 111,
                        114, 115, 68, 0, 0, 0, 18, 109, 97, 114, 107, 101, 114, 45, 104, 105, 103, 104, 45, 108, 101, 118,
                        101, 108, 115, 68, 0, 0, 0, 13, 109, 97, 114, 107, 101, 114, 45, 108, 101, 118, 101, 108, 115, 68,
                        0, 0, 0, 17, 109, 97, 114, 107, 101, 114, 45, 108, 111, 119, 45, 108, 101, 118, 101, 108, 115, 68,
                        0, 0, 0, 14, 109, 97, 114, 107, 101, 114, 45, 109, 101, 115, 115, 97, 103, 101, 68, 0, 0, 0, 12,
                        109, 97, 114, 107, 101, 114, 45, 110, 97, 109, 101, 115, 68, 0, 0, 0, 12, 109, 97, 114, 107, 101,
                        114, 45, 116, 121, 112, 101, 115, 68, 0, 0, 0, 19, 109, 101, 100, 105, 97, 45, 99, 111, 108, 45,
                        115, 117, 112, 112, 111, 114, 116, 101, 100, 68, 0, 0, 0, 36, 109, 117, 108, 116, 105, 112, 108,
                        101, 45, 100, 111, 99, 117, 109, 101, 110, 116, 45, 104, 97, 110, 100, 108, 105, 110, 103, 45, 115,
                        117, 112, 112, 111, 114, 116, 101, 100, 68, 0, 0, 0, 20, 111, 112, 101, 114, 97, 116, 105, 111, 110,
                        115, 45, 115, 117, 112, 112, 111, 114, 116, 101, 100, 68, 0, 0, 0, 13, 112, 114, 105, 110, 116, 101,
                        114, 45, 97, 108, 101, 114, 116, 68, 0, 0, 0, 25, 112, 114, 105, 110, 116, 101, 114, 45, 97, 108,
                        101, 114, 116, 45, 100, 101, 115, 99, 114, 105, 112, 116, 105, 111, 110, 68, 0, 0, 0, 25, 112, 114,
                        105, 110, 116, 101, 114, 45, 105, 115, 45, 97, 99, 99, 101, 112, 116, 105, 110, 103, 45, 106, 111,
                        98, 115, 68, 0, 0, 0, 13, 112, 114, 105, 110, 116, 101, 114, 45, 115, 116, 97, 116, 101, 68, 0, 0,
                        0, 21, 112, 114, 105, 110, 116, 101, 114, 45, 115, 116, 97, 116, 101, 45, 109, 101, 115, 115, 97,
                        103, 101, 68, 0, 0, 0, 21, 112, 114, 105, 110, 116, 101, 114, 45, 115, 116, 97, 116, 101, 45, 114,
                        101, 97, 115, 111, 110, 115, 3};
        Attribute requestedAttributes = new Attribute(ByteBuffer.wrap(multiData));
        LOG.info("toString = {}", requestedAttributes);
        LOG.info("toLongString = {}", requestedAttributes.toLongString());
        assertEquals("requested-attributes", requestedAttributes.getName());
        assertThat(requestedAttributes.isMultiValue(), is(true));
        assertThat(requestedAttributes.toLongString().length(), greaterThan(requestedAttributes.toString().length()));
    }

    @Test
    void testOfKeywords() {
        Attribute attr =
                Attribute.of(ValueTags.KEYWORD, "requested-attributes", "copies-supported", "page-ranges-supported");
        assertTrue(attr.isMultiValue());
        assertEquals("requested-attributes", attr.getName());
    }

    @Test
    void testOfName() {
        Attribute attr =
                Attribute.of(ValueTags.NAME_WITHOUT_LANGUAGE, "requesting-user-name", "nobody");
        assertFalse(attr.isMultiValue());
        assertEquals("requesting-user-name", attr.getName());
        assertEquals("nobody", attr.getStringValue());
    }

    @Test
    void testAdd() {
        Attribute multiValue = Attribute.of("answer", 42);
        multiValue.add(Attribute.of("global", true));
        assertThat(multiValue.isMultiValue(), is(true));
        assertEquals(2, multiValue.getAdditionalValues().size());
    }
    
    @Test
    void testToByteArray() {
        byte[] bytes = ATTRIBUTE.toByteArray();
        ArrayTester.assertEquals(DATA, bytes);
    }

}
