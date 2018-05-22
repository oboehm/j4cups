package j4cups.op;/*
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
 * (c)reated 07.05.2018 by oboehm (ob@oasd.de)
 */

import j4cups.protocol.IppOperations;
import j4cups.protocol.IppRequest;
import j4cups.protocol.IppResponse;
import j4cups.protocol.attr.Attribute;
import j4cups.protocol.attr.PrinterResolution;
import j4cups.protocol.enums.PrintQuality;
import j4cups.protocol.tags.ValueTags;

import java.nio.ByteBuffer;

public class GetPrinterAttributes extends Operation {
    
    /**
     * Instantiates an operation with the get-printer-attributes id.
     */
    public GetPrinterAttributes() {
        super(IppOperations.GET_PRINTER_ATTRIBUTES);
    }

    /**
     * Instantiates an operation with the get-printer-attributes id and the
     * given IPP request.
     *
     * @param ippRequest the ipp request
     */
    public GetPrinterAttributes(IppRequest ippRequest) {
        super(IppOperations.GET_PRINTER_ATTRIBUTES, ippRequest);
    }

    /**
     * Gets the IPP request which belongs to this operation.
     *
     * @return IPP request
     */
    @Override
    public IppRequest getIppRequest() {
        IppRequest request = super.getIppRequest();
        Attribute attr = Attribute
                .of(ValueTags.KEYWORD, "requested-attributes", "copies-supported", "page-ranges-supported",
                        "printer-name", "printer-info", "printer-location", "printer-make-and-model",
                        "printer-uri-supported", "media-supported", "media-default", "sides-supported", "sides-default",
                        "orientation-requested-supported", "printer-resolution-supported", "printer",
                        "printer-resolution-default", "number-up-default", "number-up-supported",
                        "document-format-supported", "print-color-mode-supported", "print-color-mode-default");
        request.setOperationAttribute(attr);
        return request;
    }

    /**
     * Gets the IPP response for the stored IPP request.
     *
     * @return IPP response
     */
    public IppResponse getIppResponse() {
        IppResponse response = super.getIppResponse();
        response.setJobAttribute(Attribute.of("printer-uri-supported", toIPP(getPrinterURI())));
        response.setJobAttribute(Attribute.of(ValueTags.TEXT_WITHOUT_LANGUAGE, "printer-name", getPrinterName()));
        response.setJobAttribute(Attribute.of(ValueTags.TEXT_WITHOUT_LANGUAGE, "printer-location", "unknown"));
        response.setJobAttribute(Attribute.of(ValueTags.TEXT_WITHOUT_LANGUAGE, "printer-info", "provided by " + getClass()));
        response.setJobAttribute(Attribute
                .of(ValueTags.MIME_MEDIA_TYPE, "document-format-supported", "application/octet-stream",
                        "application/pdf", "application/postscript", "application/vnd.cups-pdf",
                        "application/vnd.cups-pdf-banner", "application/vnd.cups-raw", "application/x-cshell",
                        "application/x-csource", "application/x-perl", "application/x-shell", "image/gif", "image/jpeg",
                        "image/png", "image/tiff", "image/x-bitmap", "image/x-photocd", "image/x-portable-anymap",
                        "image/x-portable-bitmap", "image/x-portable-graymap", "image/x-portable-pixmap",
                        "image/x-sgi-rgb", "image/x-sun-raster", "image/x-xbitmap", "image/x-xpixmap",
                        "image/x-xwindowdump", "text/css", "text/html", "text/plain"));
        response.setJobAttribute(Attribute.of("number-up-default", 1));
        response.setJobAttribute(Attribute.of(ValueTags.TEXT_WITHOUT_LANGUAGE, "printer-make-and-model", "Generic PDF Printer"));
        response.setJobAttribute(Attribute.of(ValueTags.KEYWORD, "media-default", "iso_a4_210x297mm"));
        response.setJobAttribute(Attribute
                .of(ValueTags.KEYWORD, "media-supported", "media-supported=na_letter_8.5x11in", "iso_a4_210x297mm",
                        "iso_a5_148x210mm", "iso_a6_105x148mm", "iso_b5_176x250mm", "iso_c5_162x229mm",
                        "na_number-10_4.125x9.5in", "iso_dl_110x220mm", "custom_5x13in_5x13in", "iso_c6_114x162mm",
                        "na_executive_7.25x10.5in", "jis_b5_182x257mm", "jis_b6_128x182mm", "na_legal_8.5x14in",
                        "na_monarch_3.875x7.5in", "custom_68.79x95.25mm_68.79x95.25mm", "na_invoice_5.5x8.5in"));
        response.setJobAttribute(Attribute
                .of(ValueTags.KEYWORD, "print-color-mode-supported", "monchrome", "color"));
        response.setJobAttribute(Attribute.of(ValueTags.KEYWORD, "print-color-mode-default", "color"));
        response.setJobAttribute(Attribute.of("printer-resolution-supported", 
                PrinterResolution.of(300, 300, PrintQuality.DRAFT),
                PrinterResolution.of(600, 600, PrintQuality.DRAFT),
                PrinterResolution.of(1200, 1200, PrintQuality.DRAFT)));
        response.setJobAttribute(
                Attribute.of("printer-resolution-default", PrinterResolution.of(600, 600, PrintQuality.DRAFT)));
        response.setJobAttribute(Attribute.of(ValueTags.KEYWORD, "sides-supported", "one-sided", "two-sided-long-edge",
                "two-sided-short-edge"));
        response.setJobAttribute(Attribute.of(ValueTags.KEYWORD, "sides-default", "two-sided-long-edge"));
        response.setJobAttribute(Attribute.of("copies-supported", 1, 9999));
        response.setJobAttribute(Attribute.of(ValueTags.INTEGER, "number-up-supported", toByteArray(1),
                toByteArray(2), toByteArray(4), toByteArray(6), toByteArray(9), toByteArray(16)));
        response.setJobAttribute(Attribute
                .of(ValueTags.ENUM, "orientation-requested-supported", toByteArray(3), toByteArray(4), toByteArray(5),
                        toByteArray(6)));
        response.setJobAttribute(Attribute.of("page-ranges-supported", true));
        return response;
    }
    
    private static byte[] toByteArray(int x) {
        byte[] array = new byte[4];
        ByteBuffer.wrap(array).putInt(x);
        return array;
    }

}
