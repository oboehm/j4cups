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
 * (c)reated 05.06.2018 by oboehm (ob@oasd.de)
 */
package j4cups.op;

import j4cups.protocol.IppOperations;
import j4cups.protocol.attr.Attribute;
import j4cups.protocol.attr.AttributeGroup;
import j4cups.protocol.tags.DelimiterTags;
import j4cups.protocol.tags.ValueTags;
import org.apache.commons.io.FilenameUtils;

import java.net.URI;

/**
 * The class GetPrinters is called if you want to get a list of printers
 * from CUPS.
 *
 * @author oboehm
 * @since 0.5 (05.06.2018)
 */
public class GetPrinters extends Operation {

    /**
     * Instantiates a new Get printers.
     */
    public GetPrinters() {
        super(IppOperations.GET_PRINTERS);
    }

    /**
     * Adds a printer into the response.
     *
     * @param supported the supported printer
     */
    public void addPrinter(URI supported) {
        AttributeGroup printerGroup = new AttributeGroup(DelimiterTags.PRINTER_ATTRIBUTES_TAG);
        initPrinterAttributes(printerGroup);
        printerGroup.addAttribute(Attribute.of("printer-uri-supported", supported));
        String printername = FilenameUtils.getBaseName(supported.getPath());
        printerGroup.addAttribute(Attribute.of(ValueTags.NAME_WITHOUT_LANGUAGE, "printer-name", printername));
        printerGroup.addAttribute(Attribute.of(ValueTags.TEXT_WITHOUT_LANGUAGE, "printer-location",
                "internal (/tmp/IPP/printer/" + printername + ")"));
        printerGroup.addAttribute(Attribute.of(ValueTags.TEXT_WITHOUT_LANGUAGE, "printer-info", "virtual printer"));
        getIppResponse().addAttributeGroup(printerGroup);
    }
    
}
