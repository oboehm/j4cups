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
 * (c)reated 29.05.2018 by oboehm (ob@oasd.de)
 */
package j4cups.server.http;

import j4cups.op.CreateJob;
import j4cups.protocol.AbstractIppTest;
import j4cups.protocol.IppRequest;
import j4cups.protocol.IppResponse;
import j4cups.protocol.StatusCode;
import j4cups.server.AbstractServerTest;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for {@link IppServerRequestHandler}. For testing you need a
 * CUPS server in your network. If it is not a local CUPS server on port 631
 * you can use
 * 
 *      -DcupsURI=http://localhost:631
 * 
 * to specify your CUPS server.
 */
class IppServerRequestHandlerIT extends AbstractIppRequestHandlerTest {

    private static final Logger LOG = LoggerFactory.getLogger(IppServerRequestHandlerTest.class);
    private final IppServerRequestHandler requestHandler = new IppServerRequestHandler(getCupsURI());

    private static URI getCupsURI() {
        return URI.create(System.getProperty("forwardURI", "http://localhost:631"));
    }

    /**
     * A create-job request should be answered with a response containing a job-id.
     */
    @Test
    void testHandleCreateJob() {
        CreateJob createJob = new CreateJob();
        URI printerURI = URI.create("http://localhost:631/printers/Brother_MFC_J5910DW_2");
        assumeTrue(AbstractServerTest.isOnline(printerURI));
        createJob.setPrinterURI(printerURI);
        createJob.setIppRequestId(4711);
        IppResponse ippResponse = getIppResponseFor(createJob.getIppRequest());
        if (ippResponse.getStatusCode() == StatusCode.CLIENT_ERROR_NOT_FOUND) {
            LOG.warn("Cannot create job for printer {}: {}", printerURI, ippResponse.getStatusMessage());
        } else {
            assertThat(ippResponse.getJobId(), greaterThan(0));
        }
    }

    /**
     * On a get-printers request the handler should return a response with at
     * least one printer.
     */
    @Test
    void testHandleGetPrinters() {
        URI printersURI = getPrintersURI();
        assumeTrue(AbstractServerTest.isOnline(printersURI));
        IppRequest getPrintersRequest = AbstractIppTest.readIppRequest("request", "Get-Printers.bin");
        getPrintersRequest.setPrinterURI(printersURI);
        IppResponse ippResponse = getIppResponseFor(getPrintersRequest);
        LOG.info("ippResponse = {}", ippResponse);
        assertThat(ippResponse.hasAttribute("printer-uri-supported"), is(Boolean.TRUE));
    }

    private static URI getPrintersURI() {
        return URI.create(getCupsURI() + "/printers");
    }

    private IppResponse getIppResponseFor(IppRequest ippRequest) {
        HttpResponse response = createHttpResponse();
        requestHandler.handle(createHttpRequest(ippRequest), response);
        return IppEntity.toIppResponse(response);
    }

}
