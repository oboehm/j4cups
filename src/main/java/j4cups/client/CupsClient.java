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
import j4cups.protocol.IppResponse;
import j4cups.protocol.StatusCode;
import j4cups.server.IppHandler;
import j4cups.server.http.IppEntity;
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
        return send(op);
    }

    /**
     * Cancels a job.
     *
     * @param jobId the job id which should be cancelled
     * @return response from CUPS
     */
    public IppResponse cancelJob(int jobId) {
        LOG.info("Cancelling job {}...", jobId);
        CancelJob op = new CancelJob();
        op.setJobId(jobId);
        return send(op);
    }

    private IppResponse send(Operation op) {
        IppRequest ippRequest = op.getIppRequest();
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

    private CloseableHttpResponse send(IppRequest ippRequest) throws IOException {
        LOG.info("Sending to {}: {}.", cupsURI, ippRequest);
        HttpPost httpPost = new HttpPost(cupsURI);
        IppEntity entity = new IppEntity(ippRequest);
        httpPost.setEntity(entity);
        CloseableHttpClient client = HttpClients.custom().build();
        try {
            CloseableHttpResponse response = client.execute(httpPost);
            LOG.info("Received from {}: {}", cupsURI, response);
            return response;
        } finally {
            client.close();
        }
    }

}
