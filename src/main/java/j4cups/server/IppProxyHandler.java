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
 * (c)reated 01.06.2018 by oboehm (ob@oasd.de)
 */
package j4cups.server;

import j4cups.client.CupsClient;
import j4cups.op.GetJobs;
import j4cups.op.GetPrinterAttributes;
import j4cups.op.Operation;
import j4cups.protocol.IppRequest;
import j4cups.protocol.IppResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;

/**
 * The class IppProxyHandler forwards the requests to a real CUPS server and
 * like a proxy. It can be used as template for additional tasks like
 * filtering, accounting, logging or other things which are not part
 * of a normal CUPS server.
 *
 * @author oboehm
 * @since 0.5 (01.06.2018)
 */
public class IppProxyHandler extends IppHandler {

    private static final Logger LOG = LoggerFactory.getLogger(IppProxyHandler.class);
    private final CupsClient cupsClient;

    /**
     * Instantiates a new Cups client.
     *
     * @param cupsURI the CUPS URI
     */
    public IppProxyHandler(URI cupsURI) {
        super(cupsURI);
        this.cupsClient = new CupsClient(cupsURI);
    }
    /**
     * Sends a print job to the printer.
     *
     * @param printerURI printer URI
     * @param path       file to be printed
     * @return answer from CUPS
     */
    public IppResponse printJob(URI printerURI, Path path) {
        return cupsClient.print(printerURI, path);
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
        return cupsClient.createJob(printerURI);
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
        return cupsClient.sendDocument(printerURI, path, jobId, lastDocument);
    }

    /**
     * Cancels a (created) job.
     *
     * @param printerURI printer URI
     * @param jobId      the job id
     * @return answer from CUPS
     */
    public IppResponse cancelJob(URI printerURI, int jobId) {
        return cupsClient.cancelJob(jobId, printerURI);
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
        op.setPrinterURI(printerURI);
        return cupsClient.send(op);
    }
    
    public IppResponse send(IppRequest ippRequest) {
        return cupsClient.send(ippRequest);
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " with " + cupsClient;
    }

}
