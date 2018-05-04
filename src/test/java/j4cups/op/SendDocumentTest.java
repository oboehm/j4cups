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
 * (c)reated 26.03.2018 by oboehm (ob@oasd.de)
 */
package j4cups.op;

import j4cups.protocol.attr.Attribute;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.validation.ValidationException;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link SendDocument} class.
 *
 * @author oboehm
 */
class SendDocumentTest {
    
    private final SendDocument operation = new SendDocument();

    /**
     * If {@link SendDocument} is created the generated IPP request should
     * contain the last-document attribute.
     */
    @Test
    void testLastDocument() {
        Attribute attr = operation.getAttribute("last-document");
        assertNotNull(attr);
    }

    /**
     * Test method for {@link SendDocument#setLastDocument(boolean)}.
     */
    @Test
    void testSetLastDocument() {
        operation.setLastDocument(false);
        assertFalse(operation.isLastDocument());
    }

    /**
     * The recorded request which is used for this test results in an 0x0400
     * status code. So it is not a valid send-document request.
     * 
     * @throws IOException may happen
     */
    @Test
    public void validateRequest() throws IOException {
        byte[] ippRequest = FileUtils
                .readFileToByteArray(new File("src/test/resources/j4cups/op/send-document-request-invalid.ipp"));
        Assertions.assertThrows(ValidationException.class, () -> operation.validateRequest(ippRequest));
    }

}
