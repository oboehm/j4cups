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
 * (c)reated 11.07.2018 by oboehm (ob@oasd.de)
 */
package j4cups.op;

import j4cups.protocol.IppOperations;
import j4cups.protocol.IppRequest;
import j4cups.protocol.IppResponse;
import j4cups.protocol.attr.Attribute;
import j4cups.protocol.attr.AttributeGroup;
import j4cups.protocol.tags.DelimiterTags;
import j4cups.protocol.tags.ValueTags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * The class GetPrinters is called if you want to get the default printer
 * from CUPS.
 *
 * @author oboehm
 * @since 0.5.1 (11.07.2018)
 */
public class GetDefault extends Operation {

    private static final Logger LOG = LoggerFactory.getLogger(GetDefault.class);

    /**
     * Instantiates a new Get printers.
     */
    public GetDefault() {
        super(IppOperations.GET_DEFAULT, initIppRequest());
        setPrinterURI(URI.create("http://localhost:631/printers"));
    }

    private static IppRequest initIppRequest() {
        IppRequest request = createIppRequest(IppOperations.GET_DEFAULT);
        Attribute attr = Attribute
                .of(ValueTags.KEYWORD, "requested-attributes", "printer-name", "printer-uri-supported",
                        "printer-location");
        request.setOperationAttribute(attr);
        return request;
    }

    /**
     * Gets the IPP response for the stored IPP request.
     *
     * @return IPP response
     */
    @Override
    public IppResponse getIppResponse() {
        IppResponse response = super.getIppResponse();
        response.setPrinterURI(getPrinterURI());
        Attribute requestedAttributes = getAttribute("requested-attributes");
        response.setOperationAttribute(requestedAttributes);
        return response;
    }

    /**
     * Sets the default printer.
     *
     * @param printername the name of the default printer
     */
    public void setPrinterName(String printername) {
        AttributeGroup printerGroup = new AttributeGroup(DelimiterTags.PRINTER_ATTRIBUTES_TAG);
        initPrinterAttributes(printerGroup);
        URI supported = URI.create(getPrinterURI() + "/" + printername);
        printerGroup.addAttribute(Attribute.of("printer-uri-supported", supported));
        printerGroup.addAttribute(Attribute.of(ValueTags.NAME_WITHOUT_LANGUAGE, "printer-name", printername));
        printerGroup.addAttribute(Attribute.of(ValueTags.TEXT_WITHOUT_LANGUAGE, "printer-location",
                "internal (/tmp/IPP/printer/" + printername + ")"));
        printerGroup.addAttribute(Attribute.of(ValueTags.TEXT_WITHOUT_LANGUAGE, "printer-info", "virtual printer"));
        getIppResponse().addAttributeGroup(printerGroup);
        LOG.debug("Printer {} with URI {} is set as default.", printername, supported);
    }

}
