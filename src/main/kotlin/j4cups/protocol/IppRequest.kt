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
 * (c)reated 09.02.2018 by oboehm (ob@oasd.de)
 */
package j4cups.protocol;

import java.nio.ByteBuffer;

/**
 * The IppRequest represents an IPP request as is defined in RFC-2910.
 * <pre>
 *  -----------------------------------------------
 *  |                  version-number             |   2 bytes  - required
 *  -----------------------------------------------
 *  |               operation-id (request)        |   2 bytes  - required
 *  -----------------------------------------------
 *  |                   request-id                |   4 bytes  - required
 *  -----------------------------------------------
 *  |                 attribute-group             |   n bytes - 0 or more
 *  -----------------------------------------------
 *  |              end-of-attributes-tag          |   1 byte   - required
 *  -----------------------------------------------
 *  |                     data                    |   q bytes  - optional
 *  -----------------------------------------------
 * </pre>
 *
 * @author oboehm
 * @since 0.0.1 (09.02.2018)
 */
public class IppRequest extends AbstractIpp {

    /**
     * This constructor is needed for the {@link java.io.Externalizable} 
     * interface.
     */
    public IppRequest() {
        super();
    }

    /**
     * Instantiates a new IPP request from the given bytes.
     *
     * @param bytes the bytes of the IPP request
     */
    public IppRequest(byte[] bytes) {
        this(ByteBuffer.wrap(bytes));
    }

    /**
     * Instantiates a new IPP request from the given bytes.
     *
     * @param bytes the bytes of the IPP request
     */
    public IppRequest(ByteBuffer bytes) {
        super(bytes);
    }
    
    /**
     * Returns the 2nd part (byte 2-3) with the operation-id.
     *
     * @return e.g. {@link IppOperations#CREATE_JOB}
     */
    public IppOperations getOperation() {
        return IppOperations.of(super.getOpCode());
    }

    /**
     * Returns the 2nd part (byte 2-3) with the operation-id as string.
     *
     * @return e.g. "Create-Job"
     */
    @Override
    protected String getOpCodeAsString() {
        switch (getOperation()) {
            case ADDITIONAL_REGISTERED_OPERATIONS:
            case RESERVED_FOR_VENDOR_EXTENSIONS:
                return String.format("0x%04x", getOpCode());
            default:
                return getOperation().toString();
        }
    }

}
