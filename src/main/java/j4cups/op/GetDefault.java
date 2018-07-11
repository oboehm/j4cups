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
import j4cups.protocol.attr.Attribute;
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
        super(IppOperations.GET_DEFAULT);
        setPrinterURI(URI.create("http://localhost:631/printers"));
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
                .of(ValueTags.KEYWORD, "requested-attributes", "printer-name", "printer-uri-supported",
                        "printer-location");
        request.setOperationAttribute(attr);
        return request;
    }

}
