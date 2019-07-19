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

import j4cups.client.CupsClient;
import j4cups.op.GetDefault;
import j4cups.op.GetPrinters;
import j4cups.op.SendDocument;
import j4cups.protocol.IppRequest;
import j4cups.protocol.IppResponse;
import j4cups.server.HttpHandler;
import j4cups.server.HttpProxyHandler;
import j4cups.server.IppHandler;
import j4cups.server.IppProxyHandler;
import org.apache.http.*;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.HttpContext;
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
public class IppServerRequestHandler extends AbstractIppRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger(IppServerRequestHandler.class);
    private final IppHandler ippHandler;
    private final HttpHandler httpHandler;

    /**
     * The default ctor is mainly intented for testing.
     */
    public IppServerRequestHandler() {
        this(URI.create("http://localhost:631"));
    }

    /**
     * If the request handler acts as a proxy qe need to know the URI where
     * the request should be forwarded.
     *
     * @param forwardURI CUPS URI where the request should be forwarded to
     */
    public IppServerRequestHandler(URI forwardURI) {
        this(new IppProxyHandler(forwardURI), new HttpProxyHandler(forwardURI));
    }

    /**
     * If the request handler acts as a proxy we need to know the URI where
     * the request should be forwarded. And for the generated responses it
     * must known the CUPS URI.
     *
     * @param ippHandler the handler used for IPP communication
     */
    public IppServerRequestHandler(IppHandler ippHandler, HttpHandler httpHandler) {
        this.ippHandler = ippHandler;
        this.httpHandler = httpHandler;
    }

    /**
     * Handles the incomming HTTP request.
     *
     * @param request incoming request
     * @param response outgoing response
     * @param context context
     * @throws HttpException in case of HTTP problems
     * @throws IOException e.g. network problems
     */
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException {
        if (request instanceof BasicHttpRequest) {
            handle((BasicHttpRequest) request, response);
        } else {
            super.handle(request, response, context);
        }
    }

    private void handle(BasicHttpRequest request, HttpResponse response) throws IOException {
        httpHandler.handle(request, response);
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
                ippRequest.validate();
                switch (ippRequest.getOperation()) {
                    case GET_JOBS:
                        LOG.info("{} received, but jobs are not (yet) stored.", ippRequest.toShortString());
                        break;
                    case SEND_DOCUMENT:
                        new SendDocument().validateRequest(ippRequest);
                    case CREATE_JOB:
                    case PRINT_JOB:
                        sendToPrinter(ippRequest, response);
                        break;
                    case GET_DEFAULT:
                        handleGetDefault(response, ippRequest.getRequestId());
                        break;
                    case GET_PRINTERS:
                        handleGetPrinters(request, response, ippRequest.getRequestId());
                        break;
                    default:
                        send(ippRequest, response);
                        break;
                }
            } catch (ValidationException ex) {
                handleException(ippRequest, response, ex);
            }
        } catch (BufferUnderflowException ex) {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            LOG.warn("Status code is set to {} because too less bytes were received.", HttpStatus.SC_BAD_REQUEST);
            LOG.debug("Details:", ex);
        }
    }

    private void handleGetDefault(HttpResponse response, int requestId) {
        GetDefault op = new GetDefault();
        op.setIppRequestId(requestId);
        op.setPrinterName("test-printer");
        response.setEntity(new IppEntity(op.getIppResponse()));
    }

    private void handleGetPrinters(HttpEntityEnclosingRequest request, HttpResponse response, int requestId) {
        GetPrinters op = new GetPrinters();
        op.setIppRequestId(requestId);
        Header[] hosts = request.getHeaders("Host");
        URI printerSupported = URI.create("http://" + hosts[0].getValue() + "/printers/test-printer");
        op.addPrinter(printerSupported);
        response.setEntity(new IppEntity(op.getIppResponse()));
    }

    private void send(IppRequest ippRequest, HttpResponse response) {
        IppResponse cupsResponse = ippHandler.send(ippRequest);
        response.setEntity(new IppEntity(cupsResponse));
    }

    private void sendToPrinter(IppRequest ippRequest, HttpResponse response) {
        CupsClient printerClient = new CupsClient(ippRequest.getPrinterURI());
        IppResponse cupsResponse = printerClient.send(ippRequest);
        response.setEntity(new IppEntity(cupsResponse));
    }

}
