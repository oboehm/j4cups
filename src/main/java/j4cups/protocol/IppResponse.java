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
import j4cups.protocol.tags.DelimiterTags;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * The IppResponse represents an IPP response as is defined in RFC-2910.
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
public class IppResponse {
    
    private final int requestId;
    private final StatusCode statusCode;
    private final List<AttributeGroup> attributeGroups;

    /**
     * The IppResponse is the response to a IppRequest. So you need the id
     * of the IppRequest to create a response.
     * 
     * @param request the request for this response
     */
    public IppResponse(IppRequest request) {
        this.requestId = request.getRequestId();
        this.attributeGroups = fillAttributesGroupsFor(request);
        this.statusCode = StatusCode.SUCCESSFUL_OK;
    }
    
    private static List<AttributeGroup> fillAttributesGroupsFor(IppRequest request) {
        List<AttributeGroup> groups = new ArrayList<>();
        switch (request.getOperation()) {
            case PRINT_JOB:
                groups.add(new AttributeGroup(DelimiterTags.OPERATIONS_ATTRIBUTES_TAG));
                break;
        }
        return groups;
    }

    /**
     * Converts the IppResponse to a byte array as described in RFC-2910.
     * <pre>
     *  -----------------------------------------------
     *  |                  version-number             |   2 bytes  - required
     *  -----------------------------------------------
     *  |               status-code (response)        |   2 bytes  - required
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
     * @return at least 9 bytes
     */
    public byte[] toByteArray() {
        byte[] bytes = { 2, 0, 0, 0, 0, 0, 0, 0, 3 };
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.putShort(2, statusCode.getCode());
        buffer.putInt(4, requestId);
        return bytes;
    }

    private static void setInt(int n, int start, int end, byte[] bytes) {
        int length = end + 1 - start;
        byte[] subbytes = ByteBuffer.allocate(length).putInt(n).array();
        System.arraycopy(subbytes, 0, bytes, start, length);
    }

    /**
     * The fourth field is the "attribute-group" field, and it occurs 0 or
     * more times.
     *
     * @return a list of attribute-groups
     */
    public List<AttributeGroup> getAttributeGroups() {
        return attributeGroups;
    }

}
