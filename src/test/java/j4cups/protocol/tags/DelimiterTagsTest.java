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
 * (c)reated 10.02.2018 by oboehm (ob@oasd.de)
 */
package j4cups.protocol.tags;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link DelimiterTags}.
 */
class DelimiterTagsTest {

    @Test
    void testOf() {
        assertEquals(DelimiterTags.JOB_ATTRIBUTES_TAG, DelimiterTags.of(0x02));
    }
    
    @Test
    public void testToString() {
        assertEquals("end-of-attributes-tag", DelimiterTags.END_OF_ATTRIBUTES_TAG.toString());
    }

}
