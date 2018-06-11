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

import j4cups.op.*;
import j4cups.protocol.AbstractIpp;
import j4cups.protocol.IppRequest;
import j4cups.protocol.IppResponse;
import j4cups.protocol.attr.Attribute;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The IppPrinterRequestHandler emulates an IPP printer and handles the
 * request you would normally send to an IPP printer.
 *
 * @author oliver
 * @since 0.5
 */
public final class IppPrinterRequestHandler extends AbstractIppRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger(IppPrinterRequestHandler.class);
    private final Path recordDir;
    private int jobId;

    /**
     * Instantiates the request handler for the printer emulation. The requests
     * will be recorded to "/tmp/IPP/printer" (on Unix).
     */
    public IppPrinterRequestHandler() {
        this(Paths.get(SystemUtils.getJavaIoTmpDir().toString(), "IPP", "printer"));
    }

    /**
     * Instantiates the request handler for the printer emulation.
     *
     * @param recordDir directory, where the requests are logged
     */
    public IppPrinterRequestHandler(Path recordDir) {
        this.recordDir = recordDir;
    }

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
        ippRequest.recordTo(recordDir);
        IppResponse ippResponse = new IppResponse(ippRequest);
        switch (ippRequest.getOperation()) {
            case GET_PRINTER_ATTRIBUTES:
                ippResponse = handleGetPrinterAttributes(ippRequest);
                break;
            case CREATE_JOB:
                ippResponse = handleCreateJob(ippRequest);
                break;
            case PRINT_JOB:
                ippResponse = handlePrintJob(ippRequest);
                break;
            case SEND_DOCUMENT:
                ippResponse = handleSendDocument(ippRequest);
                break;
        }
        ippResponse.setRequestId(ippRequest.getRequestId());
        IppEntity ippEntity = new IppEntity(ippResponse);
        response.setEntity(ippEntity);
        ippResponse.recordTo(recordDir);
        LOG.info("Response {} is filled.", response);
    }

    private IppResponse handleGetPrinterAttributes(IppRequest ippRequest) {
        Operation op = new GetPrinterAttributes(ippRequest);
        return op.getIppResponse();
    }

    private IppResponse handleCreateJob(IppRequest ippRequest) {
        CreateJob op = new CreateJob(ippRequest);
        setJobId(op);
        return op.getIppResponse();
    }

    private IppResponse handlePrintJob(IppRequest ippRequest) {
        return handle(new PrintJob(), ippRequest);
    }

    private IppResponse handleSendDocument(IppRequest ippRequest) {
        return handle(new SendDocument(), ippRequest);
    }

    private IppResponse handle(Operation op, IppRequest ippRequest) {
        recordData(ippRequest);
        setJobId(op);
        URI printerURI = ippRequest.getPrinterURI();
        op.setPrinterURI(printerURI);
        URI jobURI = getJobUriFrom(printerURI);
        op.setJobAttribute(Attribute.of("job-uri", jobURI));
        return op.getIppResponse();
    }

    private URI getJobUriFrom(URI printerURI) {
        try {
            return new URI("ipp", null, printerURI.getHost(), printerURI.getPort(),  "/jobs/" + jobId, null, null);
        } catch (URISyntaxException ex) {
            LOG.warn("Cannot build job-URI from {}:", printerURI, ex);
            return printerURI;
        }
    }

    private void setJobId(Operation op) {
        jobId++;
        op.setJobId(jobId);
    }

    private void recordData(IppRequest ippRequest) {
        URI printerURI = ippRequest.getPrinterURI();
        Path dataDir = Paths.get(recordDir.toString(), "data", StringUtils.substringAfterLast(printerURI.getPath(), "/"));
        String filename = FilenameUtils.normalize(ippRequest.getAttribute("job-name").getStringValue() + ".data", true);
        filename = StringUtils.removeAll(filename, "/");
        AbstractIpp.recordTo(dataDir, ippRequest.getData(), filename);
    }

}
