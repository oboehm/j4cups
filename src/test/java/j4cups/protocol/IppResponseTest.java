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
import j4cups.protocol.attr.AttributeGroup;
import j4cups.protocol.enums.JobState;
import j4cups.protocol.tags.DelimiterTags;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Unit tests for {@link IppResponse}.
 */
public final class IppResponseTest extends AbstractIppTest {

    private static final Logger LOG = LoggerFactory.getLogger(IppResponseTest.class);
    private static final IppResponse RESPONSE_PRINT_JOB = new IppResponse(REQUEST_PRINT_JOB);
    private static final IppResponse RESPONSE_GET_JOBS = new IppResponse(REQUEST_GET_JOBS);

    @Override
    protected IppResponse getIppPackage() {
        return new IppResponse(REQUEST_PRINT_JOB);
    }

    /**
     * A very basic test to check the toByteArray functionality.
     */
    @Test
    void testToByteArray() {
        IppResponse response = new IppResponse(REQUEST_PRINT_JOB);
        byte[] bytes = response.toByteArray();
        assertThat(bytes.length, greaterThan(9));
        assertThat(DatatypeConverter.printHexBinary(bytes), startsWith("02000000000000020"));
        assertEquals(0x03, bytes[bytes.length-1]);
    }

    /**
     * The Printer MUST return to the Client the "attributes-charset" and 
     * "attributes-natural-language" as operation attributes. This is described in
     * <a href="https://tools.ietf.org/html/rfc8011#section-4.2.1.2">Section 4.1.4.2.</a>
     * of RFC-8011.
     */
    @Test
    void testPrintJobResponseOperationAttributes() {
        checkOperationAttributesOf(RESPONSE_PRINT_JOB);
    }
    
    /**
     * The Printer MUST return "job-id" and other  job attributes. This is described in
     * <a href="https://tools.ietf.org/html/rfc8011#section-4.2.1.2">Section 4.1.4.2.</a>
     * of RFC-8011.
     */
    @Test
    void testPrintJobResponseJobAttributes() {
        List<Attribute> jobAttributes =
                RESPONSE_PRINT_JOB.getAttributeGroup(DelimiterTags.JOB_ATTRIBUTES_TAG).getAttributes();
        assertThat(jobAttributes, not(empty()));
        checkAttribute(RESPONSE_PRINT_JOB,"job-state", JobState.COMPLETED.getValue());
    }

    /**
     * The Printer returns to the Client the "attributes-charset" and 
     * "attributes-natural-language" as operation attributes. This is described in
     * <a href="https://tools.ietf.org/html/rfc8011#section-4.2.6.1">Section 4.2.6.1.</a>
     * of RFC-8011.
     */
    @Test
    void testGetJobsAttributes() {
        checkOperationAttributesOf(RESPONSE_GET_JOBS);
    }

    /**
     * There are no more attributes defined as MUST in RFC-8011.
     */
    @Test
    void testGetJobsOperationAttributes() {
        assertThrows(IllegalArgumentException.class, () -> {
            checkOperationAttributes(RESPONSE_GET_JOBS, "requesting-user-name");
        });
    }

    private void checkOperationAttributes(IppResponse response, String... attributeNames) {
        AttributeGroup group = response.getAttributeGroup(DelimiterTags.OPERATIONS_ATTRIBUTES_TAG);
        for (String name : attributeNames) {
            Attribute attr = group.getAttribute(name);
            LOG.info("found: {}", attr);
        }
    }

    /**
     * The Printer MUST return to the Client the "attributes-charset" and 
     * "attributes-natural-language" as operation attributes. This is described in
     * <a href="https://tools.ietf.org/html/rfc8011#section-4.2.5.2">Section 4.2.5.2.</a>
     * of RFC-8011.
     */
    @Test
    void testGetPrinterAttributes() {
        IppResponse responseGetPrinterAttributes = new IppResponse(REQUEST_GET_PRINTER_ATTRIBUTES);
        checkOperationAttributesOf(responseGetPrinterAttributes);
    }

    void checkOperationAttributesOf(IppResponse response) {
        List<Attribute> opAttributes = response.getAttributeGroup(DelimiterTags.OPERATIONS_ATTRIBUTES_TAG).getAttributes();
        assertThat(opAttributes, not(empty()));
        checkAttribute(response,"attributes-charset", "utf-8");
        checkAttribute(response, "attributes-natural-language",
                Locale.getDefault().getLanguage().toLowerCase());
    }

    private static void checkAttribute(IppResponse response, String name, String expected) {
        Attribute attribute = response.getAttribute(name);
        assertEquals(expected, attribute.getStringValue());
    }

    private static void checkAttribute(IppResponse response, String name, int expected) {
        Attribute attribute = response.getAttribute(name);
        assertEquals(expected, attribute.getIntValue());
    }

    /**
     * Testmethod for {@link IppResponse#setStatusCode(StatusCode)}.
     */
    @Test
    public void setStatusCode() {
        IppResponse response = new IppResponse(REQUEST_GET_JOBS);
        response.setStatusCode(StatusCode.CLIENT_ERROR_BAD_REQUEST);
        assertEquals(StatusCode.CLIENT_ERROR_BAD_REQUEST, response.getStatusCode());
    }

}
