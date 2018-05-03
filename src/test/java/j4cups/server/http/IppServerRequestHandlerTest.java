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
 * (c)reated 19.04.2018 by oboehm (ob@oasd.de)
 */
package j4cups.server.http;

import j4cups.op.CreateJob;
import j4cups.protocol.AbstractIppTest;
import j4cups.protocol.IppResponse;
import j4cups.protocol.StatusCode;
import j4cups.server.AbstractServerTest;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit tests for {@link IppServerRequestHandler}.
 */
class IppServerRequestHandlerTest extends AbstractIppRequestHandlerTest {

    private static final Logger LOG = LoggerFactory.getLogger(IppServerRequestHandlerTest.class);
    private final IppServerRequestHandler requestHandler = new IppServerRequestHandler(Paths.get("target").toUri());

    /**
     * For a first simple test we just call the handle method with an invalid
     * request.
     * 
     * @throws IOException in case of network problems
     * @throws HttpException HTTP exception
     */
    @Test
    void testHandle400() throws IOException, HttpException {
        HttpPost request = createInvalidHttpRequest();
        HttpResponse response = createHttpResponse();
        HttpContext context = new HttpClientContext();
        requestHandler.handle(request, response, context);
        assertEquals(400, response.getStatusLine().getStatusCode());
    }

    private static HttpPost createInvalidHttpRequest() throws UnsupportedEncodingException {
        HttpPost request = new HttpPost();
        HttpEntity entity = new StringEntity("hello");
        request.setEntity(entity);
        return request;
    }

    /**
     * This is a replay of a send-document request which is invalid because
     * the job-id is missing.
     *
     * @throws IOException in case of network problems
     */
    @Test
    void testHandleInvalidSendDocument() throws IOException {
        IppServerRequestHandler handler = new IppServerRequestHandler(URI.create("http://localhost:631"));
        sendIncompleteDocument(handler);
    }

    /**
     * Here we test the {@link IppServerRequestHandler}, if it is created with
     * a file URI.
     *
     * @throws IOException in case of network problems
     */
    @Test
    void testIppServerRequestHandlerFile() throws IOException {
        sendIncompleteDocument(requestHandler);
    }

    private static void sendIncompleteDocument(IppServerRequestHandler handler) throws IOException {
        HttpPost request = createHttpRequest(AbstractIppTest.readIppRequest("op", "send-document-request-invalid.ipp"));
        HttpResponse response = createHttpResponse();
        handler.handle(request, response);
        assertEquals(400, response.getStatusLine().getStatusCode());
        IppResponse ippResponse = IppEntity.toIppResponse(response);
        LOG.info("Received: {}", ippResponse);
        assertEquals(StatusCode.CLIENT_ERROR_BAD_REQUEST, ippResponse.getStatusCode());
        assertThat(ippResponse.getStatusMessage(), containsString("job-id"));
    }

    /**
     * A create-job request should be answered with a response containing a job-id.
     *
     * @throws IOException in case of network problems
     */
    @Test
    void testHandleCreateJob() throws IOException {
        CreateJob createJob = new CreateJob();
        URI printerURI = URI.create("http://localhost:631/printers/Brother_MFC_J5910DW_2");
        assumeTrue(AbstractServerTest.isOnline(printerURI));
        createJob.setPrinterURI(printerURI);
        createJob.setIppRequestId(4711);
        HttpResponse response = createHttpResponse();
        requestHandler.handle(createHttpRequest(createJob.getIppRequest()), response);
        IppResponse ippResponse = IppEntity.toIppResponse(response);
        assertThat(ippResponse.getJobId(), greaterThan(0));
    }

    @AfterEach
    public void closeRequestHandler() throws IOException {
        this.requestHandler.close();
    }

}