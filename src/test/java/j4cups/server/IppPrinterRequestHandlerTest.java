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
package j4cups.server;

import j4cups.protocol.AbstractIppTest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link IppPrinterRequestHandler}.
 */
final class IppPrinterRequestHandlerTest extends AbstractIppRequestHandlerTest {

    private static final Logger LOG = LoggerFactory.getLogger(IppPrinterRequestHandlerTest.class);
    private static final IppPrinterRequestHandler handler = new IppPrinterRequestHandler();

    @Test
    void testHandle() {
        HttpPost httpRequest = createHttpRequest(
                AbstractIppTest.readIppRequest("request", "Get-Printer-Attributes.bin"));
        HttpResponse httpResponse = createHttpResponse();
        handler.handle(httpRequest, httpResponse, new HttpClientContext());
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        LOG.info("httpResponse = {}", httpResponse);
    }

}