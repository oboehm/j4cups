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
package j4cups.server.http;

import j4cups.op.SendDocument;
import j4cups.protocol.IppRequest;
import j4cups.protocol.IppResponse;
import j4cups.protocol.StatusCode;
import j4cups.server.IppHandler;
import j4cups.server.IppProxyHandler;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ValidationException;
import java.net.URI;
import java.nio.BufferUnderflowException;

/**
 * The class IppServerRequestHandler handles the IPP requests.
 *
 * @author <a href="ob@aosd.de">oliver</a>
 * @since (15.04.18)
 */
public class IppServerRequestHandler extends AbstractIppRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger(IppServerRequestHandler.class);
    private final IppHandler ippHandler;

    /**
     * The default ctor is mainly intented for testing.
     */
    public IppServerRequestHandler() {
        this(URI.create("http://localhost:631"));
    }

    /**
     * If the request handler acts as a proxy he need to know ther URI where
     * the request should be forwarded.
     *
     * @param forwardURI CUPS URI where the request should be forwarded to
     */
    public IppServerRequestHandler(URI forwardURI) {
        this(new IppProxyHandler(forwardURI));
    }

    /**
     * If the request handler acts as a proxy he need to know the URI where
     * the request should be forwarded. And for the generated responses it
     * must known the CUPS URI.
     *
     * @param ippHandler the handler used for IPP communication
     */
    public IppServerRequestHandler(IppHandler ippHandler) {
        this.ippHandler = ippHandler;
    }

    /**
     * Handles the incomming IPP request and sends it to the real CUPS server
     * (given by the forwardURI or the inserted printer-uri).
     *
     * @param request incoming request
     * @param response outgoing response
     */
    protected void handle(HttpEntityEnclosingRequest request, HttpResponse response) {
        try {
            IppRequest ippRequest = IppEntity.toIppRequest(request);
            LOG.info("Received: {}", ippRequest);
            response.setStatusCode(HttpStatus.SC_OK);
            try {
                switch (ippRequest.getOperation()) {
                    case GET_JOBS:
                        LOG.info("{} received, but jobs are not (yet) stored.", ippRequest.toShortString());
                        return;
                    case SEND_DOCUMENT:
                        new SendDocument().validateRequest(ippRequest);
                        break;
                }
                send(ippRequest, response);
            } catch (ValidationException ex) {
                handleException(ippRequest, response, ex);
            }
        } catch (BufferUnderflowException ex) {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            LOG.warn("Status code is set to {} because too less bytes were received.", HttpStatus.SC_BAD_REQUEST);
            LOG.debug("Details:", ex);
        }
    }

    private void send(IppRequest ippRequest, HttpResponse response) {
        IppResponse cupsResponse = ippHandler.send(ippRequest);
        response.setEntity(new IppEntity(cupsResponse));
    }

    private static void handleException(IppRequest ippRequest, HttpResponse response, ValidationException ex) {
        IppResponse ippResponse = new IppResponse(ippRequest);
        LOG.info("{} is not valid ({}).", ippRequest, ex.getMessage());
        LOG.debug("Details:", ex);
        response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
        ippResponse.setStatusCode(StatusCode.CLIENT_ERROR_BAD_REQUEST);
        ippResponse.setStatusMessage(ex.getMessage());
        response.setEntity(new IppEntity(ippResponse));
    }

}
