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

import j4cups.protocol.IppResponse;
import j4cups.protocol.attr.Attribute;
import j4cups.protocol.tags.DelimiterTags;
import org.junit.jupiter.api.Test;
import patterntesting.runtime.junit.ArrayTester;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void testOrientationRequested() {
        Attribute attr = op.getAttribute("orientation-requested");
        assertNotNull(attr);
    }

    /**
     * Test method for {@link PrintJob#setData(byte[])}.
     */
    @Test
    void testSetData() {
        byte[] data = "test".getBytes(StandardCharsets.UTF_8);
        op.setData(data);
        ArrayTester.assertEquals(data, op.getIppRequest().getData());
    }
    
    /**
     * The Printer MUST return "job-id" and other  job attributes. This is described in
     * <a href="https://tools.ietf.org/html/rfc8011#section-4.2.1.2">Section 4.1.4.2.</a>
     * of RFC-8011.
     */
    @Test
    void testPrintJobResponseJobAttributes() {
        IppResponse responsePrintJob = op.getIppResponse();
        List<Attribute> jobAttributes =
                responsePrintJob.getAttributeGroup(DelimiterTags.JOB_ATTRIBUTES_TAG).getAttributes();
        assertThat(jobAttributes, not(empty()));
        assertTrue(responsePrintJob.hasAttribute("job-state"));
    }

}
