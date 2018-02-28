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
 * (c)reated 10.02.2018 by oboehm (boehm@javatux.de)
 */
package j4cups.protocol.attr;

import j4cups.protocol.tags.DelimiterTags;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import patterntesting.runtime.junit.ArrayTester;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link AttributeGroup} class. The data for this tests are
 * recorded during an installation of a printer in CUPS, where the header of
 * the IPP request (the first 8 bytes) were removed.
 */
public final class AttributeGroupTest {

    private static final Logger LOG = LoggerFactory.getLogger(AttributeGroupTest.class);

    private final byte[] data =
            {1, 71, 0, 18, 97, 116, 116, 114, 105, 98, 117, 116, 101, 115, 45, 99, 104, 97,
                    114, 115, 101, 116, 0, 5, 117, 116, 102, 45, 56, 72, 0, 27, 97, 116, 116, 114, 105, 98, 117, 116,
                    101, 115, 45, 110, 97, 116, 117, 114, 97, 108, 45, 108, 97, 110, 103, 117, 97, 103, 101, 0, 2, 100,
                    101, 69, 0, 11, 112, 114, 105, 110, 116, 101, 114, 45, 117, 114, 105, 0, 53, 104, 116, 116, 112, 58,
                    47, 47, 111, 112, 116, 99, 110, 49, 48, 49, 46, 97, 100, 46, 100, 114, 103, 117, 101, 108, 100, 101,
                    110, 101, 114, 46, 100, 101, 58, 49, 50, 48, 49, 49, 47, 112, 114, 105, 110, 116, 101, 114, 115, 47,
                    101, 99, 104, 111, 68, 0, 20, 114, 101, 113, 117, 101, 115, 116, 101, 100, 45, 97, 116, 116, 114,
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
    private final AttributeGroup attributeGroup = new AttributeGroup(ByteBuffer.wrap(data));

    @Test
    void getBeginTag() {
        assertEquals(DelimiterTags.OPERATION_ATTRIBUTES_TAG, attributeGroup.getBeginTag());
    }
    
    @Test
    void getAttributes() {
        List<Attribute> attributes = attributeGroup.getAttributes();
        assertThat(attributes, not(empty()));
        assertThat(attributes.size(), greaterThan(1));
    }
    
    @Test
    public void testToString() {
        String s = attributeGroup.toString();
        LOG.info("s = \"{}\"", s);
        assertThat("looks like default implementation", s, not(containsString("@")));
    }
    
    @Test
    public void testToLongString() {
        String longString = attributeGroup.toLongString();
        LOG.info("longString = {}", longString);
        assertThat(longString.length(), greaterThan(attributeGroup.toString().length()));
    }
    
    @Test
    public void testToByteArray() {
        byte[] bytes = attributeGroup.toByteArray();
        byte[] expected = Arrays.copyOf(data, bytes.length);
        ArrayTester.assertEquals(expected, bytes);
    }

}
