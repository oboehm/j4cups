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

import j4cups.protocol.attr.Attribute;
import j4cups.protocol.attr.AttributeGroup;
import j4cups.protocol.tags.DelimiterTags;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
public class IppResponse extends AbstractIpp {
    
    /**
     * The IppResponse is the response to a IppRequest. So you need the id
     * of the IppRequest to create a response.
     * 
     * @param request the request for this response
     */
    public IppResponse(IppRequest request) {
        super(DEFAULT_VERSION, StatusCode.SUCCESSFUL_OK.getCode(), request.getRequestId(), fillAttributesGroupsFor(request));
    }
    
    private static List<AttributeGroup> fillAttributesGroupsFor(IppRequest request) {
        List<AttributeGroup> groups = new ArrayList<>();
        switch (request.getOperation()) {
            case PRINT_JOB:
                groups.add(createPrintJobOperations());
                break;
        }
        return groups;
    }

    private static AttributeGroup createPrintJobOperations() {
        AttributeGroup group = new AttributeGroup(DelimiterTags.OPERATIONS_ATTRIBUTES_TAG);
        group.addAttribute(Attribute.of("attributes-charset", StandardCharsets.UTF_8));
        group.addAttribute(Attribute.of("attributes-natural-language", Locale.getDefault()));
        return group;
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
        buffer.putShort(2, getOpCode());
        buffer.putInt(4, getRequestId());
        return bytes;
    }

    /**
     * Returns the 2nd part (byte 2-3) with the status-code.
     *
     * @return e.g. {@link StatusCode#SUCCESSFUL_OK}
     */
    public StatusCode getStatusCode() {
        return StatusCode.of(super.getOpCode());
    }
    
    /**
     * Returns the 2nd part (byte 2-3) with the status-code as string.
     *
     * @return e.g. "successful-ok"
     */
    @Override
    protected String getOpCodeAsString() {
        return getStatusCode().toString();
    }

}
