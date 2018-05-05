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
 * (c)reated 29.04.18 by oliver (ob@oasd.de)
 */

package j4cups.op;

import j4cups.protocol.IppOperations;
import j4cups.protocol.IppRequest;
import j4cups.protocol.IppResponse;
import j4cups.protocol.attr.Attribute;
import j4cups.protocol.enums.JobState;
import j4cups.protocol.tags.ValueTags;

import javax.validation.ValidationException;

/**
 * Class PrintJob represents the print-job operation.
 *
 * @author oliver
 * @since 0.5
 */
public class PrintJob extends Operation {

    /**
     * Instantiates an operation for 'print-job'.
     */
    public PrintJob() {
        this(IppOperations.PRINT_JOB);
    }

    /**
     * This constructor is foreseen for subclasses.
     *
     * @param op e.g. IppOperations.PRINT_JOB
     */
    protected PrintJob(IppOperations op) {
        super(op, createIppPrintJobRequest(op));
    }

    private static IppRequest createIppPrintJobRequest(IppOperations op) {
        IppRequest request = createIppRequest(op);
        request.setJobAttribute(Attribute.of("copies", 1));
        request.setJobAttribute(Attribute.of(ValueTags.ENUM, "orientation-requested", 3));
        request.setJobAttribute(Attribute.of(ValueTags.KEYWORD, "output-mode", "monochrome"));
        return request;
    }

    /**
     * Sets the data to be printed.
     *
     * @param data print data
     */
    public void setData(byte[] data) {
        getIppRequest().setData(data);
    }

    /**
     * Sets the job-name.
     *
     * @param name job name
     */
    public void setJobName(String name) {
        Attribute attr = Attribute.of(ValueTags.NAME_WITHOUT_LANGUAGE, "job-name", name);
        setOperationAttribute(attr);
    }

    /**
     * Sets the document-name.
     *
     * @param name document name
     */
    public void setDocumentName(String name) {
        Attribute attr = Attribute.of(ValueTags.NAME_WITHOUT_LANGUAGE, "document-name", name);
        setOperationAttribute(attr);
    }

    /**
     * Looks if the given request is valid. If not an
     * {@link ValidationException} will be thrown
     *
     * @param request IPP reqeust
     */
    @Override
    public void validateRequest(IppRequest request) {
        super.validateRequest(request);
        if (!hasTarget(request)) {
            throw new ValidationException(
                    "neither 'printer-uri' & 'job-id' nor 'job-uri' is given in " + request.toShortString());
        }
    }

    private static boolean hasTarget(IppRequest request) {
        return (request.hasAttribute("printer-uri") && request.hasAttribute("job-id"))
                || request.hasAttribute("job-uri");
    }

    /**
     * Gets the IPP response for the stored IPP request.
     *
     * @return IPP response
     */
    @Override
    public IppResponse getIppResponse() {
        IppResponse response = super.getIppResponse();
        response.setJobAttribute(Attribute.of(ValueTags.ENUM, "job-state", JobState.COMPLETED.getValue()));
        return response;
    }

}
