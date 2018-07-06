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
import j4cups.protocol.attr.AttributeGroup;
import j4cups.protocol.attr.PrinterResolution;
import j4cups.protocol.enums.JobState;
import j4cups.protocol.enums.JobStateReasons;
import j4cups.protocol.enums.PrintQuality;
import j4cups.protocol.tags.DelimiterTags;
import j4cups.protocol.tags.ValueTags;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ValidationException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

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
     * Sets the given job attribute in the generated IPP request.
     *
     * @param attribute operation attribute
     */
    public void setJobAttribute(Attribute attribute) {
        ippRequest.setJobAttribute(attribute);
    }

    /**
     * Initialiazes printer-attributes with default values. This method can be
     * used by sub classes to do it.
     * 
     * @param group attribute group where the printer attributes belong to
     */
    protected void initPrinterAttributes(AttributeGroup group) {
        group.addAttribute(Attribute
                .of(ValueTags.MIME_MEDIA_TYPE, "document-format-supported", "application/octet-stream",
                        "application/pdf", "application/postscript", "application/vnd.cups-pdf",
                        "application/vnd.cups-pdf-banner", "application/vnd.cups-raw", "application/x-cshell",
                        "application/x-csource", "application/x-perl", "application/x-shell", "image/gif", "image/jpeg",
                        "image/png", "image/tiff", "image/x-bitmap", "image/x-photocd", "image/x-portable-anymap",
                        "image/x-portable-bitmap", "image/x-portable-graymap", "image/x-portable-pixmap",
                        "image/x-sgi-rgb", "image/x-sun-raster", "image/x-xbitmap", "image/x-xpixmap",
                        "image/x-xwindowdump", "text/css", "text/html", "text/plain"));
        group.addAttribute(Attribute.of("number-up-default", 1));
        group.addAttribute(Attribute.of(ValueTags.TEXT_WITHOUT_LANGUAGE, "printer-make-and-model", "Generic PDF Printer"));
        group.addAttribute(Attribute.of(ValueTags.KEYWORD, "media-default", "iso_a4_210x297mm"));
        group.addAttribute(Attribute
                .of(ValueTags.KEYWORD, "media-supported", "media-supported=na_letter_8.5x11in", "iso_a4_210x297mm",
                        "iso_a5_148x210mm", "iso_a6_105x148mm", "iso_b5_176x250mm", "iso_c5_162x229mm",
                        "na_number-10_4.125x9.5in", "iso_dl_110x220mm", "custom_5x13in_5x13in", "iso_c6_114x162mm",
                        "na_executive_7.25x10.5in", "jis_b5_182x257mm", "jis_b6_128x182mm", "na_legal_8.5x14in",
                        "na_monarch_3.875x7.5in", "custom_68.79x95.25mm_68.79x95.25mm", "na_invoice_5.5x8.5in"));
        group.addAttribute(Attribute
                .of(ValueTags.KEYWORD, "print-color-mode-supported", "monchrome", "color"));
        group.addAttribute(Attribute.of(ValueTags.KEYWORD, "print-color-mode-default", "color"));
        group.addAttribute(Attribute.of("printer-resolution-supported",
                PrinterResolution.of(300, 300, PrintQuality.DRAFT),
                PrinterResolution.of(600, 600, PrintQuality.DRAFT),
                PrinterResolution.of(1200, 1200, PrintQuality.DRAFT)));
        group.addAttribute(
                Attribute.of("printer-resolution-default", PrinterResolution.of(600, 600, PrintQuality.DRAFT)));
        group.addAttribute(Attribute.of(ValueTags.KEYWORD, "sides-supported", "one-sided", "two-sided-long-edge",
                "two-sided-short-edge"));
        group.addAttribute(Attribute.of(ValueTags.KEYWORD, "sides-default", "two-sided-long-edge"));
        group.addAttribute(Attribute.of("copies-supported", 1, 9999));
        group.addAttribute(Attribute.of(ValueTags.INTEGER, "number-up-supported", toByteArray(1),
                toByteArray(2), toByteArray(4), toByteArray(6), toByteArray(9), toByteArray(16)));
        group.addAttribute(Attribute
                .of(ValueTags.ENUM, "orientation-requested-supported", toByteArray(3), toByteArray(4), toByteArray(5),
                        toByteArray(6)));
        group.addAttribute(Attribute.of("page-ranges-supported", true));
    }

    private static byte[] toByteArray(int x) {
        byte[] array = new byte[4];
        ByteBuffer.wrap(array).putInt(x);
        return array;
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
        Set<DelimiterTags> tags = new HashSet<>();
        for (AttributeGroup group : request.getAttributeGroups()) {
            DelimiterTags beginTag = group.getBeginTag();
            if (tags.contains(beginTag)) {
                throw new ValidationException("multiple '" + beginTag + "' in request: " + request);
            }
            tags.add(beginTag);
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
