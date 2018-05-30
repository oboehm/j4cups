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
package j4cups.client;

import j4cups.op.CancelJob;
import j4cups.op.CreateJob;
import j4cups.op.Operation;
import j4cups.protocol.IppRequest;
import j4cups.protocol.IppRequestException;
import j4cups.protocol.IppResponse;
import j4cups.protocol.StatusCode;
import j4cups.server.IppHandler;
import j4cups.server.http.IppEntity;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;

/**
 * This is a very basic client to access CUPS. It was introduced to simplify
 * the internal tests with a CUPS server.
 * 
 * @since 0.5
 */
public class CupsClient {

    private static final Logger LOG = LoggerFactory.getLogger(IppHandler.class);
    private final URI cupsURI;
    private int requestId = 0;

    /**
     * Generates a client for the access to a local CUPS on port 631.
     */
    public CupsClient() {
        this(URI.create("http://localhost:631"));
    }

    /**
     * Generates a client for the access to the given URI to CUPS.
     * 
     * @param cupsURI normally "http://localhost:631" on Linux and Mac
     */
    public CupsClient(URI cupsURI) {
        this.cupsURI = cupsURI;
    }

    /**
     * Sends a list of files as one job to the given printer.
     * 
     * @param printerURI where to send the files
     * @param files the files to be printed
     */
    public void printTo(URI printerURI, List<Path> files) {
        IppResponse createJobResponse = createJob(printerURI);
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * Creates a Job. This is needed if you want to print several documents
     * as one job.
     *
     * @param printerURI where to send the files after creation
     * @return response from CUPS
     */
    public IppResponse createJob(URI printerURI) {
        CreateJob op = new CreateJob();
        op.setPrinterURI(printerURI);
        op.setCupsURI(cupsURI);
        setRequestId(op);
        return send(op);
    }

    /**
     * Cancels a job.
     *
     * @param jobId the job id which should be cancelled
     * @return response from CUPS
     */
    public IppResponse cancelJob(int jobId, URI printerURI) {
        LOG.info("Job {} will be cancelled.", jobId);
        CancelJob op = new CancelJob();
        op.setJobId(jobId);
        op.setPrinterURI(printerURI);
        setRequestId(op);
        return send(op);
    }

    private void setRequestId(Operation op) {
        requestId++;
        op.setIppRequestId(requestId);
    }

    private IppResponse send(Operation op) {
        return send(op.getIppRequest());
    }

    private IppResponse send(IppRequest ippRequest) {
        LOG.info("Sending to {}: {}", cupsURI, ippRequest);
        HttpPost httpPost = new HttpPost(cupsURI);
        httpPost.setConfig(RequestConfig.custom().setSocketTimeout(10000).setConnectTimeout(10000).build());
        IppEntity entity = new IppEntity(ippRequest);
        httpPost.setEntity(entity);
        try (CloseableHttpClient client = HttpClients.custom().build()) {
            CloseableHttpResponse httpResponse = client.execute(httpPost);
            LOG.info("Received from {}: {}", cupsURI, httpResponse);
            try (InputStream istream = httpResponse.getEntity().getContent()) {
                IppResponse ippResponse = new IppResponse(IOUtils.toByteArray(istream));
                if (!ippResponse.getStatusCode().isSuccessful())  {
                    throw new IppRequestException(ippResponse);
                }
                return ippResponse;
            }
        } catch (IOException ex) {
            LOG.warn("Cannot sent {}:", ippRequest, ex);
            IppResponse ippResponse = new IppResponse(ippRequest);
            ippResponse.setStatusCode(StatusCode.SERVER_ERROR_INTERNAL_ERROR);
            ippResponse.setStatusMessage(ex.getMessage());
            throw new IppRequestException(ippResponse, ex);
        }
    }

}
