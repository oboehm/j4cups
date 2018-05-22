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
 * (c)reated 26.03.2018 by oboehm (ob@oasd.de)
 */
package j4cups.op;

import j4cups.protocol.IppOperations;
import j4cups.protocol.IppRequest;
import j4cups.protocol.IppResponse;
import j4cups.protocol.attr.Attribute;
import j4cups.protocol.enums.JobState;
import j4cups.protocol.enums.JobStateReasons;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ValidationException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * This is the common super class of all IPP operations.
 *
 * @author oboehm
 * @since 0.5 (26.03.2018)
 */
public class Operation {

    private static final Logger LOG = LoggerFactory.getLogger(Operation.class);
    private final IppOperations id;
    private final IppRequest ippRequest;
    private final IppResponse ippResponse;
    private URI cupsURI = URI.create("http://localhost:631");

    /**
     * Instantiates an operation with the given id.
     * 
     * @param id a positiv number between 0x0000 and 0x0031
     */
    public Operation(IppOperations id) {
        this(id, createIppRequest(id));
    }

    /**
     * Instantiates an operation with the given id and the given IPP request.
     *
     * @param id         a positiv number between 0x0000 and 0x0031
     * @param ippRequest the ipp request
     */
    protected Operation(IppOperations id, IppRequest ippRequest) {
        this.id = id;
        this.ippRequest = ippRequest;
        this.ippResponse = new IppResponse(ippRequest);
    }

    protected static IppRequest createIppRequest(IppOperations ippOp) {
        IppRequest request = new IppRequest();
        request.setOpCode(ippOp.getCode());
        request.setAttributesCharset(StandardCharsets.UTF_8);
        request.setAttributesNaturalLanguage(Locale.US);
        request.setRequestingUserName(SystemUtils.USER_NAME);
        return request;
    }

    /**
     * Gets the IPP request which belongs to this operation.
     *
     * @return IPP request
     */
    public IppRequest getIppRequest() {
        return ippRequest;
    }

    /**
     * Gets the IPP response for the stored IPP request.
     * 
     * @return IPP response
     */
    public IppResponse getIppResponse() {
        return ippResponse;
    }

    /**
     * Sets the ID of the IPP request.
     *
     * @param id the new ID
     */
    public void setIppRequestId(int id) {
        ippRequest.setRequestId(id);
    }

    /**
     * Returns the attribute of the give name.
     *
     * @param name attribute name
     * @return the attribute
     */
    public Attribute getAttribute(String name) {
        return ippRequest.getAttribute(name);
    }
    
    /**
     * Sets the printer-uri.
     *
     * @param printerURI the printer uri
     */
    public void setPrinterURI(URI printerURI) {
        ippRequest.setPrinterURI(printerURI);
    }

    /**
     * Gets printer uri.
     *
     * @return printer uri
     */
    public URI getPrinterURI() {
        return ippRequest.getPrinterURI();
    }

    /**
     * The printer name is the last part of the printer-uri.
     *
     * @return the printer name
     */
    public String getPrinterName() {
        return StringUtils.substringAfterLast(getPrinterURI().getPath(), "/");
    }

    /**
     * Sets the CUPS URI. This is needed e.g. to set the job-uri.
     *
     * @param cupsURI the printer uri
     */
    public void setCupsURI(URI cupsURI) {
        this.cupsURI = cupsURI;
    }

    /**
     * Sets job id.
     *
     * @param jobId the job id
     */
    public void setJobId(int jobId) {
        ippRequest.setJobId(jobId);
        ippResponse.setJobId(jobId);
        try {
            URI ippURI = new URI("ipp", cupsURI.getUserInfo(), cupsURI.getHost(), cupsURI.getPort(),
                    cupsURI.getPath() + "/jobs/" + jobId, cupsURI.getQuery(), cupsURI.getFragment());
            ippResponse.setJobURI(ippURI);
        } catch (URISyntaxException ex) {
            LOG.warn("Cannot set job-uri from {}:", cupsURI, ex);
            ippResponse.setJobURI(URI.create("ipp://localhost:631/jobs/" + jobId));
        }
    }

    /**
     * Sets job state.
     *
     * @param state the state
     */
    public void setJobState(JobState state) {
        ippResponse.setJobState(state);
    }

    /**
     * Sets job state reason.
     *
     * @param reason the job-state-reason
     */
    public void setJobStateReasons(JobStateReasons reason) {
        ippResponse.setJobStateReasons(reason);
        ippResponse.setJobStateMessage(reason.toString());
    }

    /**
     * Sets the given operation attribute in the generated IPP request.
     *
     * @param attribute operation attribute
     */
    public void setOperationAttribute(Attribute attribute) {
        ippRequest.setOperationAttribute(attribute);
    }

    /**
     * Looks if the given bytes represents a valid request. If not an
     * {@link ValidationException} will be thrown
     * 
     * @param bytes which represents an request
     */
    public void validateRequest(byte[] bytes) {
        validateRequest(new IppRequest(bytes));
    }

    /**
     * Looks if the given request is valid. If not an
     * {@link ValidationException} will be thrown
     *
     * @param request IPP reqeust
     */
    public void validateRequest(IppRequest request) {
        if (request.getOperation() != id) {
            throw new ValidationException("not a " + id + " request:" + request);
        }
    }

    /**
     * Looks if the stored request is valid. If not an
     * {@link ValidationException} will be thrown
     * 
     * @since 0.5
     */
    public void validateRequest() {
        validateRequest(getIppRequest());
    }


    /**
     * Converts the given URI into an URI beginning with "ipp://...".
     *
     * @param uri the uri
     * @return the uri
     */
    protected static URI toIPP(URI uri) {
        try {
            return new URI("ipp", uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(),
                    uri.getFragment());
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("not a real URI: " + uri, ex);
        }
    }

}
