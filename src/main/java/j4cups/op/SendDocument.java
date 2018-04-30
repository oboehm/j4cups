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
import j4cups.protocol.attr.Attribute;

/**
 * Class SendDocument represents the Send-Document operation if IPP.
 *
 * @author oboehm
 * @since 0.5 (26.03.2018)
 */
public class SendDocument extends PrintJob {

    /**
     * Instantiates an operation for 'send-document'.
     */
    public SendDocument() {
        super(IppOperations.SEND_DOCUMENT);
        setLastDocument(true);
    }

    /**
     * To print multiple documents the last document must be marked as
     * "last-document".
     *
     * @param ok true if dcoument is the last one
     */
    public void setLastDocument(boolean ok) {
        setOperationAttribute(Attribute.of("last-document", ok));
    }

    /**
     * Returns true if the operation belongs to the last document.
     *
     * @return true if it is the last document
     */
    public boolean isLastDocument() {
        Attribute lastDocument = getAttribute("last-document");
        return lastDocument.getBooleanValue();
    }

}
