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
import j4cups.protocol.tags.DelimiterTags;
import j4cups.protocol.tags.ValueTags;

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
        initPrinterAttributes(response.getAttributeGroup(DelimiterTags.JOB_ATTRIBUTES_TAG));
        return response;
    }
    
}
