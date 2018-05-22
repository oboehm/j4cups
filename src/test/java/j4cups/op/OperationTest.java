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
 * (c)reated 04.05.2018 by oboehm (ob@oasd.de)
 */
package j4cups.op;

import j4cups.protocol.AbstractIppTest;
import j4cups.protocol.IppOperations;
import j4cups.protocol.IppRequest;
import j4cups.protocol.IppResponse;
import j4cups.protocol.attr.Attribute;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static patterntesting.runtime.junit.ObjectTester.assertEquals;

/**
 * Unit tests for {@link Operation}.
 */
public class OperationTest {
    
    private final Operation operation = new Operation(IppOperations.GET_JOBS);

    @Test
    void testSetJobId() {
        operation.setCupsURI(URI.create("http://localhost:4711"));
        operation.setJobId(42);
        assertEquals(URI.create("ipp://localhost:4711/jobs/42"), operation.getIppResponse().getJobURI());
    }

    /**
     * This method compares the attributes of the given {@link IppRequest}
     * with an {@link IppRequest} reference recorded from a test with CUPS.
     *
     * @param ippRequest the IPP request which should be checked
     * @param refResource name of a resource with the recorded reference
     */
    public static void checkIppRequest(IppRequest ippRequest, String refResource) {
        IppRequest reference = AbstractIppTest.readIppRequest("request", refResource);
        for (Attribute attr : reference.getAttributes()) {
            assertThat("missing attribute: " + attr, ippRequest.hasAttribute(attr.getName()), is(true));
        }
    }

    /**
     * This method compares the attributes of the given {@link IppResponse}
     * with an {@link IppResponse} reference recorded from a test with CUPS.
     * 
     * @param ippResponse the IPP response which should be checked
     * @param refResource name of a resource with the recorded reference
     */
    public static void checkIppResponse(IppResponse ippResponse, String refResource) {
        IppResponse reference = AbstractIppTest.readIppResponse("response", refResource);
        for (Attribute attr : reference.getAttributes()) {
            assertThat("missing attribute: " + attr, ippResponse.hasAttribute(attr.getName()), is(true));
        }
    }
    
}
