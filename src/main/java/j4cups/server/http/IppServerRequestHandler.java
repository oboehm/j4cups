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
import j4cups.server.IppSender;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ValidationException;
import java.io.IOException;
import java.net.URI;
import java.nio.BufferUnderflowException;

/**
 * The class IppServerRequestHandler handles the IPP requests.
 *
 * @author <a href="ob@aosd.de">oliver</a>
 * @since (15.04.18)
 */
public class IppServerRequestHandler extends AbstractIppRequestHandler implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(IppServerRequestHandler.class);
    private final IppSender ippSender;

    /**
     * The default ctor is mainly intented for testing.
     */
    public IppServerRequestHandler() {
        this(URI.create("http://localhost:631"));
    }

    /**
     * For shutdown request we must know the server. The server is given
     * with this constructor.
     *
     * @param forwardURI URI where the request should be forwarded to
     */
    public IppServerRequestHandler(URI forwardURI) {
        this.ippSender = new IppSender(forwardURI);
    }

    /**
     * Handles the incomming IPP request and sends it to the real CUPS server
     * (given by the forwardURI or the inserted printer-uri).
     *
     * @param request incoming request
     * @param response outgoing response
     * @throws IOException e.g. network problems
     */
    protected void handle(HttpEntityEnclosingRequest request, HttpResponse response) throws IOException {
        try {
            IppRequest ippRequest = IppEntity.toIppRequest(request);
            LOG.info("Received: {}", ippRequest);
            response.setStatusCode(HttpStatus.SC_OK);
            try {
                switch (ippRequest.getOperation()) {
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

    private void send(IppRequest ippRequest, HttpResponse response) throws IOException {
        CloseableHttpResponse cupsResponse = ippSender.send(ippRequest);
        response.setEntity(cupsResponse.getEntity());
        response.setStatusCode(cupsResponse.getStatusLine().getStatusCode());
        response.setStatusLine(cupsResponse.getStatusLine());
        response.setLocale(cupsResponse.getLocale());
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

    /**
     * Closes the IppSender which used to connect the CUPS server or
     * printer.
     *
     * @throws IOException if this resource cannot be closed
     */
    @Override
    public void close() throws IOException {
        ippSender.close();
    }

}
