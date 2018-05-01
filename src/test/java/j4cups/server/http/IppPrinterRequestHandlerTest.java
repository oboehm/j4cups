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

import j4cups.protocol.AbstractIppTest;
import j4cups.protocol.IppRequest;
import j4cups.protocol.IppResponse;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link IppPrinterRequestHandler}.
 */
final class IppPrinterRequestHandlerTest extends AbstractIppRequestHandlerTest {

    private static final Logger LOG = LoggerFactory.getLogger(IppPrinterRequestHandlerTest.class);
    private static final IppPrinterRequestHandler handler = new IppPrinterRequestHandler();

    /**
     * A simple test to see if an IPP request is handled.
     *
     * @throws IOException   the io exception
     * @throws HttpException the http exception
     */
    @Test
    void testHandle() throws IOException, HttpException {
        IppRequest ippRequest = AbstractIppTest.readIppRequest("request", "Get-Printer-Attributes.bin");
        HttpPost httpRequest = createHttpRequest(
                ippRequest);
        HttpResponse httpResponse = createHttpResponse();
        handler.handle(httpRequest, httpResponse, new HttpClientContext());
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        LOG.info("httpResponse = {}", httpResponse);
        IppResponse ippResponse = IppEntity.toIppResponse(httpResponse);
        assertEquals(ippRequest.getRequestId(), ippResponse.getRequestId());
    }

}