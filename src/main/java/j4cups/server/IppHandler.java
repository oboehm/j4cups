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

import j4cups.client.CupsClient;
import j4cups.op.CreateJob;
import j4cups.op.GetJobs;
import j4cups.op.GetPrinterAttributes;
import j4cups.op.Operation;
import j4cups.protocol.IppRequest;
import j4cups.protocol.IppResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
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
public class IppHandler {

    private static final Logger LOG = LoggerFactory.getLogger(IppHandler.class);
    private final Path recordDir;

    /**
     * Instantiates a new IPP handler.
     *
     * @param forwardURI the forward uri where to store the requests/responses
     */
    public IppHandler(URI forwardURI) {
        this(Paths.get(forwardURI));
    }

    /**
     * Instantiates a new IPP handler.
     * 
     * @param recordDir directory where requests and responses are recorded
     */
    public IppHandler(Path recordDir) {
        this.recordDir = recordDir;
    }

    /**
     * Sends a print job to the printer.
     *
     * @param printerURI printer URI
     * @param path       file to be printed
     * @return answer from CUPS
     */
    public IppResponse printJob(URI printerURI, Path path) {
        CupsClient printerClient = new CupsClient(printerURI);
        return printerClient.print(printerURI, path);
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
        CupsClient printerClient = new CupsClient(printerURI);
        return printerClient.sendDocument(printerURI, path, jobId, lastDocument);
    }

    /**
     * Cancels a (created) job.
     *
     * @param printerURI printer URI
     * @param jobId      the job id
     * @return answer from CUPS
     */
    public IppResponse cancelJob(URI printerURI, int jobId) {
        CupsClient printerClient = new CupsClient(printerURI);
        return printerClient.cancelJob(jobId, printerURI);
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
        CupsClient printerClient = new CupsClient(printerURI);
        return printerClient.send(op);
    }

    private IppResponse handle(Operation op) {
        IppRequest ippRequest = op.getIppRequest();
        op.validateRequest();
        return op.getIppResponse();
    }

    /**
     * Sends an IPP request to a CUPS server. Normally the resonse from CUPS is
     * an {@link j4cups.protocol.IppResponse}. But in some case (like the
     * get-printers operation the answer is returned as HTML page.
     *
     * @param ippRequest the ipp request
     * @return response from CUPS
     */
    public IppResponse send(IppRequest ippRequest) {
        ippRequest.recordTo(recordDir);
        IppResponse ippResponse = new IppResponse(ippRequest);
        ippResponse.recordTo(recordDir);
        return ippResponse;
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " to " + recordDir;
    }

}
