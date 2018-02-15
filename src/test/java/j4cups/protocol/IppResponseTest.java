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
 * (c)reated 09.02.2018 by oboehm (ob@oasd.de)
 */
package j4cups.protocol;

import org.junit.jupiter.api.Test;

import javax.xml.bind.DatatypeConverter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringStartsWith.startsWith;


/**
 * Unit tests for {@link IppResponse}.
 */
public final class IppResponseTest extends AbstractIppTest {

    @Test
    void testToByteArray() {
        byte[] expected = { 2, 0, 0, 0, 0, 0, 0, 2, 3 };
        IppResponse response = new IppResponse(PRINT_JOB);
        assertThat(DatatypeConverter.printHexBinary(response.toByteArray()), startsWith("020000000000000203"));
    }

}
