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

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import javax.validation.ValidationException;
import java.io.File;
import java.io.IOException;

/**
 * Unit tests for {@link SendDocument} class.
 *
 * @author oboehm
 */
public class SendDocumentTest {
    
    private final SendDocument operation = new SendDocument();

    /**
     * The recorded request which is used for this test results in an 0x0400
     * status code. So it is not a valid send-document request.
     * 
     * @throws IOException may happen
     */
    @Test(expected = ValidationException.class)
    public void validateRequest() throws IOException {
        byte[] ippRequest = FileUtils
                .readFileToByteArray(new File("src/test/resources/j4cups/op/send-document-request-invalid.ipp"));
        operation.validateRequest(ippRequest);
    }

}