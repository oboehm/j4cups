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
import j4cups.protocol.StatusCode;
import org.apache.http.*;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Class AbstractIppRequestHandlerTest.
 *
 * @author oliver
 * @since 01.05.18
 */
public abstract class AbstractIppRequestHandlerTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractIppRequestHandlerTest.class);
    
    /**
     * This method should be overridden by subclass to get the request handler
     * for testing.
     * 
     * @return request handler for testing
     */
    protected abstract AbstractIppRequestHandler getRequestHandler();

    /**
     * This is a replay of a send-document request with a duplicate
     * operation-attribute-groups section.
     */
    @Test
    void testHandleSendDocumentWithDuplicateGroup() {
        IppResponse ippResponse = checkHandleRequest400("Send-Document-401.ipp");
        assertThat(ippResponse.getStatusMessage(), containsString("operation-attributes-tag"));
    }

    protected IppResponse checkHandleRequest400(String requestName) {
        HttpResponse response = handleRequest(requestName, getRequestHandler());
        assertEquals(400, response.getStatusLine().getStatusCode());
        IppResponse ippResponse = IppEntity.toIppResponse(response);
        LOG.info("Received: {}", ippResponse);
        assertEquals(StatusCode.CLIENT_ERROR_BAD_REQUEST, ippResponse.getStatusCode());
        return ippResponse;
    }

    /**
     * Creates an POST request for testing.
     *
     * @param ippRequest IPP request which is wrapped as HttpPost
     * @return POST request with IPP data inside
     */
    protected static HttpPost createHttpRequest(IppRequest ippRequest) {
        HttpPost request = new HttpTestRequest();
        request.setHeader("Host", "localhost:4711");
        request.setEntity(new IppEntity(ippRequest));
        return request;
    }

    /**
     * Use this method to create an empty {@link HttpResponse}.
     * 
     * @return an empty {@link HttpResponse}
     */
    protected static HttpResponse createHttpResponse() {
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 0);
        StatusLine statusLine = new BasicStatusLine(protocolVersion, 200, "OK");
        return new BasicHttpResponse(statusLine);
    }


    /**
     * Loads a recorded request and sends it to the given handler.
     *
     * @param requestName name of the recorded request
     * @param handler request handler
     * @return response from the handler
     */
    protected HttpResponse handleRequest(String requestName, AbstractIppRequestHandler handler) {
        IppRequest ippRequest = AbstractIppTest.readIppRequest("request", requestName);
        return handleRequest(ippRequest, handler);
    }

    /**
     * Sends a prepared request to the given handler.
     *
     * @param ippRequest prepared request
     * @param handler request handler
     * @return response from the handler
     */
    protected HttpResponse handleRequest(IppRequest ippRequest, AbstractIppRequestHandler handler) {
        HttpPost request = createHttpRequest(ippRequest);
        HttpResponse response = createHttpResponse();
        try {
            handler.handle(request, response);
            return response;
        } catch (IOException ex) {
            throw new IllegalArgumentException("cannot handle request " + ippRequest, ex);
        }
    }

    static class HttpTestRequest extends HttpPost {
        public HttpTestRequest() {
            super();
        }
        @Override
        public RequestLine getRequestLine() {
            return new RequestLine() {
                @Override
                public String getMethod() {
                    return "POST";
                }
                @Override
                public ProtocolVersion getProtocolVersion() {
                    return new HttpVersion(1, 1);
                }
                @Override
                public String getUri() {
                    return "/printers/test-printer";
                }
            };
        }
    }

}
