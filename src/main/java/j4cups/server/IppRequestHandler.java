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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express orimplied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * (c)reated 15.04.18 by oliver (ob@oasd.de)
 */
package j4cups.server;

import j4cups.protocol.IppRequest;
import org.apache.http.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * The class IppRequestHandler handles the IPP requests.
 *
 * @author <a href="ob@aosd.de">oliver</a>
 * @since (15.04.18)
 */
public class IppRequestHandler implements HttpRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger(IppRequestHandler.class);

    /**
     * Handles the incomming IPP request.
     *
     * @param request incoming request
     * @param response outgoing response
     * @param context context
     * @throws HttpException in case of HTTP problems
     * @throws IOException e.g. network problems
     */
    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException {
        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ROOT);
        if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
            throw new MethodNotSupportedException(method + " method not supported");
        }
        if (request instanceof HttpEntityEnclosingRequest) {
            handle((HttpEntityEnclosingRequest) request, response);
        } else {
            response.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
            StringEntity entity = new StringEntity(
                    "<html><body><h1>" + request +
                            " not supported</h1></body></html>",
                    ContentType.create("text/html", "UTF-8"));
            response.setEntity(entity);
            LOG.warn("{} is not supported.", request);
        }
    }

    private void handle(HttpEntityEnclosingRequest request, HttpResponse response) throws IOException {
        HttpEntity entity = request.getEntity();
        byte[] entityContent = EntityUtils.toByteArray(entity);
        try {
            IppRequest ippRequest = new IppRequest(entityContent);
            LOG.info("Received: {}", ippRequest);
            response.setStatusCode(HttpStatus.SC_OK);
        } catch (BufferUnderflowException ex) {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            LOG.warn("Status code is set to {} because only {} bytes (\"{}\") were received.", HttpStatus.SC_BAD_REQUEST,
                    entityContent.length, new String(entityContent, StandardCharsets.UTF_8));
            LOG.debug("Details:", ex);
        }
    }

}
