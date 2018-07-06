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
 * (c)reated 01.05.18 by oliver (ob@oasd.de)
 */
package j4cups.server.http;

import j4cups.op.OperationTest;
import j4cups.protocol.AbstractIppTest;
import j4cups.protocol.IppRequest;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * Unit tests for {@link IppPrinterRequestHandler}.
 */
final class IppPrinterRequestHandlerTest extends AbstractIppRequestHandlerTest {

    private static final Logger LOG = LoggerFactory.getLogger(IppPrinterRequestHandlerTest.class);
    private static final IppPrinterRequestHandler handler = new IppPrinterRequestHandler(
            Paths.get("target", "IPP", "printer"));

    @Test
    void testHandleGetPrinterAttributes() {
        checkHandle("Get-Printer-Attributes.bin");
    }

    @Test
    void testHandlePrintJob() {
        checkHandle("Print-Job.bin");
    }

    @Test
    void testHandleSendDocument() {
        checkHandle("Send-Document.ipp");
    }

    private void checkHandle(String filename) {
        HttpResponse response = handleRequest(filename, handler);
        OperationTest.checkIppResponse(IppEntity.toIppResponse(response), filename);
    }
    
    @Test
    void testRecordData() {
        IppRequest ippRequest = AbstractIppTest.readIppRequest("request", "Send-Document.ipp");
        ippRequest.setAttribute("job-name", "test/PgMz\\G".getBytes(StandardCharsets.UTF_8));
        HttpResponse response = handleRequest(ippRequest, handler);
        OperationTest.checkIppResponse(IppEntity.toIppResponse(response), "Send-Document.ipp");
    }

}
