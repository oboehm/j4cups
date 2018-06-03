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
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        HttpResponse response = handleRequest("Get-Printer-Attributes.bin", handler);
        OperationTest.checkIppResponse(IppEntity.toIppResponse(response), "Get-Printer-Attributes.bin");
    }

    @Test
    void testHandlePrintJob() {
        HttpResponse response = handleRequest("Print-Job.bin", handler);
        OperationTest.checkIppResponse(IppEntity.toIppResponse(response), "Print-Job.bin");
    }

}