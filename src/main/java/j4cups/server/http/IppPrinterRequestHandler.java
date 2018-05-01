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
 * (c)reated 30.04.18 by oliver (ob@oasd.de)
 */

package j4cups.server.http;

import j4cups.protocol.IppRequest;
import j4cups.protocol.IppResponse;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The IppPrinterRequestHandler emulates an IPP printer and handles the
 * request you would normally send to an IPP printer.
 *
 * @author oliver
 * @since 0.5
 */
public final class IppPrinterRequestHandler extends AbstractIppRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger(IppPrinterRequestHandler.class);

    /**
     * Handles the request and produces a response to be sent back to
     * the client.
     *
     * @param request  the HTTP request.
     * @param response the HTTP response.
     */
    @Override
    public void handle(HttpEntityEnclosingRequest request, HttpResponse response) {
        IppRequest ippRequest = IppEntity.toIppRequest(request);
        LOG.info("{} received.", ippRequest.toShortString());
        IppResponse ippResponse = new IppResponse(ippRequest);
        IppEntity ippEntity = new IppEntity(ippResponse);
        response.setEntity(ippEntity);
        LOG.info("Response {} is filled.", response);
    }

}
