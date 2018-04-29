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
import j4cups.protocol.attr.Attribute;
import j4cups.protocol.tags.ValueTags;

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
        super(IppOperations.PRINT_JOB);
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
        getIppRequest().setOperationAttribute(attr);
    }

    /**
     * Sets the document-name.
     *
     * @param name document name
     */
    public void setDocumentName(String name) {
        Attribute attr = Attribute.of(ValueTags.NAME_WITHOUT_LANGUAGE, "document-name", name);
        getIppRequest().setOperationAttribute(attr);
    }

}
