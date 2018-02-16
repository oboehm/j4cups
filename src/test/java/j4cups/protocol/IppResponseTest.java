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

import j4cups.protocol.attr.Attribute;
import j4cups.protocol.tags.DelimiterTags;
import org.junit.jupiter.api.Test;

import javax.xml.bind.DatatypeConverter;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.StringStartsWith.startsWith;


/**
 * Unit tests for {@link IppResponse}.
 */
public final class IppResponseTest extends AbstractIppTest {
    
    private final IppResponse RESPONSE_PRINT_JOB = new IppResponse(REQUEST_PRINT_JOB);

    @Override
    protected IppResponse getIppPackage() {
        return RESPONSE_PRINT_JOB;
    }

    @Test
    void testToByteArray() {
        IppResponse response = RESPONSE_PRINT_JOB;
        assertThat(DatatypeConverter.printHexBinary(response.toByteArray()), startsWith("020000000000000203"));
    }

    /**
     * The Printer MUST return to the Client the "attributes-charset" and 
     * "attributes-natural-language" as operation attributes. This is described in
     * <a href="https://tools.ietf.org/html/rfc8011#section-4.2.1.2">Section 4.1.4.2.</a>
     * of RFC-8011.
     */
    @Test
    void testPrintJobResponse() {
        List<Attribute> attributes = RESPONSE_PRINT_JOB.getAttributeGroups(DelimiterTags.OPERATIONS_ATTRIBUTES_TAG);
        assertThat(attributes, not(empty()));
    }

}
