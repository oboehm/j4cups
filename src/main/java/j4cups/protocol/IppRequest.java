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

import j4cups.protocol.attr.AttributeGroup;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * The IppRequest represents an IPP request as is defined in RFC-2910.
 * <pre>
 *  -----------------------------------------------
 *  |                  version-number             |   2 bytes  - required
 *  -----------------------------------------------
 *  |               operation-id (request)        |
 *  |                      or                     |   2 bytes  - required
 *  |               status-code (response)        |
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
public class IppRequest {
    
    private final ByteBuffer bytes;
    private final String version;
    private final IppOperations operation;
    private final int requestId;

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
        this.bytes = bytes;
        this.version = bytes.get(0) + "." + bytes.get(1);
        this.operation = IppOperations.of(bytes.getShort(2));
        this.requestId = bytes.getInt(4);
    }

    /**
     * Gets the first part (byte 0-1) with the supported IPP version. This is a
     * number from 1.0 till 1.4 for version 1 or 2.0 for version 2.
     *
     * @return e.g. "2.0"
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the 2nd part (byte 2-3) with the operation-id.
     *
     * @return e.g. {@link IppOperations#CREATE_JOB}
     */
    public IppOperations getOperation() {
        return operation;
    }

    /**
     * Retunns the 3rd part (byte 4-7) of the request which
     * contains the request-id.
     * 
     * @return the request-id
     */
    public int getRequestId() {
        return requestId;
    }

    /**
     * The fourth field is the "attribute-group" field, and it occurs 0 or
     * more times.
     * 
     * @return a list of attribute-groups
     */
    public List<AttributeGroup> getAttributeGroups() {
        bytes.position(8);
        AttributeGroup group = new AttributeGroup(bytes);
        List<AttributeGroup> attributeGroups = new ArrayList<>();
        attributeGroups.add(group);
        return attributeGroups;
    }

}
