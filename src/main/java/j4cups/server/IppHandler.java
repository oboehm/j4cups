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
 * (c)reated 26.04.2018 by oboehm (ob@oasd.de)
 */
package j4cups.server;

import j4cups.op.*;
import j4cups.protocol.IppRequest;
import j4cups.protocol.IppResponse;
import j4cups.protocol.StatusCode;
import j4cups.protocol.enums.JobState;
import j4cups.protocol.enums.JobStateReasons;
import j4cups.server.http.IppEntity;
import j4cups.server.http.LogRequestInterceptor;
import j4cups.server.http.LogResponseInterceptor;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The IppHandler is reponsible for handling IPP requests. He can do it by
 * forwarding the request to a real CUPS server. Or it can handle the IPP
 * requests itself and acts like a (more ore less) real CUPS server. This
 * may be useful if you want to do some tests with a CUPS server but do not
 * want to waste paper.
 *
 * @author oboehm
 * @since 0.5 (26.04.2018)
 */
public final class IppHandler implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(IppHandler.class);
    private final URI forwardURI;
    private final URI cupsURI;
    private final CloseableHttpClient client = createHttpClient();
    private int requestId = 1;
    private int jobId = 0;

    /**
     * Instantiates a new Cups client.
     *
     * @param cupsURI the cups uri
     */
    public IppHandler(URI cupsURI) {
        this(cupsURI, cupsURI);
    }

    /**
     * Instantiates a new Cups client.
     *
     * @param forwardURI the forward uri
     * @param cupsURI    the cups uri
     */
    public IppHandler(URI forwardURI, URI cupsURI) {
        this.forwardURI = forwardURI;
        this.cupsURI = cupsURI;
    }

    /**
     * Sends a print job to the printer.
     *
     * @param printerURI printer URI
     * @param path       file to be printed
     * @return answer from CUPS
     */
    public IppResponse printJob(URI printerURI, Path path) {
        PrintJob op = new PrintJob();
        setPrintJob(op, path);
        return send(op, printerURI);
    }

    /**
     * Gets all jobs of a printer.
     *
     * @param printerURI printer URI
     * @return answer from CUPS
     */
    public IppResponse getJobs(URI printerURI) {
        GetJobs op = new GetJobs();
        return send(op, printerURI);
    }

    /**
     * Creates a new job. This is needed if you want to send multiple
     * documents.
     *
     * @param printerURI printer URI
     * @return answer from CUPS
     */
    public IppResponse createJob(URI printerURI) {
        CreateJob op = new CreateJob();
        return send(op, printerURI);
    }

    /**
     * Sends one print job of a multiple document to the printer
     *
     * @param printerURI   printer URI
     * @param path         file to be printed
     * @param jobId        the job id
     * @param lastDocument if it is the last document
     * @return answer from CUPS
     */
    public IppResponse sendDocument(URI printerURI, Path path, int jobId, boolean lastDocument) {
        SendDocument op = new SendDocument();
        op.setJobId(jobId);
        op.setLastDocument(lastDocument);
        setPrintJob(op, path);
        return send(op, printerURI);
    }

    private void setPrintJob(PrintJob op, Path path) {
        try {
            byte[] data = Files.readAllBytes(path);
            op.setData(data);
            op.setJobName(path.getFileName() + "-" + requestId);
            op.setDocumentName(path.toString());
        } catch (IOException ex) {
            throw new IllegalArgumentException("cannot read " + path, ex);
        }
    }

    /**
     * Cancels a (created) job.
     *
     * @param printerURI printer URI
     * @param jobId      the job id
     * @return answer from CUPS
     */
    public IppResponse cancelJob(URI printerURI, int jobId) {
        CancelJob op = new CancelJob();
        op.setJobId(jobId);
        return send(op, printerURI);
    }

    /**
     * Sends a get-printer-attributes to CUPS.
     *
     * @param printerURI printer URI
     * @return answer from CUPS
     */
    public IppResponse getPrinterAttributes(URI printerURI) {
        Operation op = new GetPrinterAttributes();
        return send(op, printerURI);
    }

    private IppResponse send(Operation op, URI printerURI) {
        op.setCupsURI(cupsURI);
        op.setPrinterURI(printerURI);
        return send(op);
    }

    private IppResponse send(Operation op) {
        op.setIppRequestId(requestId);
        requestId++;
        if ("file".equals(forwardURI.getScheme())) {
            return handle(op);
        } else {
            return send(op, op.getIppRequest());
        }
    }

    private IppResponse handle(Operation op) {
        IppRequest ippRequest = op.getIppRequest();
        switch (ippRequest.getOperation()) {
            case PRINT_JOB:
                setJobIdFor(op);
                op.setJobStateReasons(JobStateReasons.NONE);
                break;
            case CREATE_JOB:
                op.setCupsURI(cupsURI);
                op.setJobState(JobState.PENDING_HELD);
                op.setJobStateReasons(JobStateReasons.JOB_INCOMING);
                setJobIdFor(op);
                break;
            case SEND_DOCUMENT:
                op.setCupsURI(cupsURI);
                setJobIdFor(op);
                break;
            default:
                op.validateRequest();
                break;
        }
        return op.getIppResponse();
    }

    private void setJobIdFor(Operation op) {
        jobId++;
        op.setJobId(jobId);
    }

    private IppResponse send(Operation op, IppRequest ippRequest) {
        try {
            CloseableHttpResponse httpResponse = send(ippRequest);
            try (InputStream istream = httpResponse.getEntity().getContent()) {
                return new IppResponse(IOUtils.toByteArray(istream));
            }
        } catch (IOException ex) {
            LOG.warn("Cannot sent {}:", op, ex);
            IppResponse ippResponse = new IppResponse(ippRequest);
            ippResponse.setStatusCode(StatusCode.SERVER_ERROR_INTERNAL_ERROR);
            ippResponse.setStatusMessage(ex.getMessage());
            return ippResponse;
        }
    }

    /**
     * Sends an IPP request to a CUPS server. Normally the resonse from CUPS is
     * an {@link j4cups.protocol.IppResponse}. But in some case (like the
     * get-printers operation the answer is returned as HTML page.
     *
     * @param ippRequest the ipp request
     * @return response from CUPS
     * @throws IOException the io exception
     */
    public CloseableHttpResponse send(IppRequest ippRequest) throws IOException {
        if ("file".equals(forwardURI.getScheme())) {
            return record(ippRequest, Paths.get(forwardURI));
        } else {
            return sendTo(forwardURI, ippRequest);
        }
    }

    private CloseableHttpResponse record(IppRequest ippRequest, Path dir) throws IOException {
        ippRequest.recordTo(dir);
        URI printerURI = getPrinterURI(ippRequest);
        CloseableHttpResponse response = sendTo(printerURI, ippRequest);
        IppResponse ippResponse = IppEntity.toIppResponse(response);
        ippResponse.recordTo(dir);
        return response;
    }

    private CloseableHttpResponse sendTo(URI targetURI, IppRequest ippRequest) throws IOException {
        LOG.info("Sending to {}: {}.", targetURI, ippRequest);
        HttpPost httpPost = new HttpPost(targetURI);
        IppEntity entity = new IppEntity(ippRequest);
        httpPost.setEntity(entity);
        CloseableHttpResponse response = client.execute(httpPost);
        LOG.info("Received from {}: {}", targetURI, response);
        return response;
    }

    private static CloseableHttpClient createHttpClient() {
        return HttpClients.custom().addInterceptorFirst(new LogRequestInterceptor("C"))
                          .addInterceptorLast(new LogResponseInterceptor("C")).build();
    }

    private URI getPrinterURI(IppRequest ippRequest) {
        URI printerURI = ippRequest.getPrinterURI();
        if (printerURI.getPort() < 0) {
            LOG.debug("Port is missing in {} and will be replaced with port 631.", printerURI);
            try {
                return new URI(printerURI.getScheme(), printerURI.getUserInfo(), printerURI.getHost(),
                        631, printerURI.getPath(), printerURI.getQuery(), printerURI.getFragment());
            } catch (URISyntaxException ex) {
                LOG.warn("Cannot create printer-uri '{}' with port 631:", printerURI, ex);
            }
        }
        return printerURI;
    }

    /**
     * Closes the HttpClient which used to connect the CUPS server or
     * printer.
     *
     * @throws IOException if this resource cannot be closed
     */
    @Override
    public void close() throws IOException {
        client.close();
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " to " + forwardURI;
    }

}
