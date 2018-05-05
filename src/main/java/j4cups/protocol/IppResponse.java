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
import j4cups.protocol.tags.ValueTags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class IppResponse extends AbstractIpp {
    
    private static final Logger LOG = LoggerFactory.getLogger(IppResponse.class);

    /**
     * This constructor is needed for the {@link java.io.Externalizable}
     * interface.
     */
    public IppResponse() {
        super();
    }

    /**
     * Instantiates a new IPP response from the given bytes.
     *
     * @param bytes the bytes of the IPP request
     */
    public IppResponse(byte[] bytes) {
        super(bytes);
    }

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
        for (AttributeGroup g : request.getAttributeGroups()) {
            groups.add(new AttributeGroup(g));
        }
        return groups;
    }

    /**
     * Set the 2nd part (byte 2-3) with the new status-code.
     * 
     * @param code the new status code
     */
    public void setStatusCode(StatusCode code) {
        setOpCode(code.getCode());
    }

    /**
     * Returns the 2nd part (byte 2-3) with the status-code.
     *
     * @return e.g. {@link StatusCode#SUCCESSFUL_OK}
     */
    public StatusCode getStatusCode() {
        return StatusCode.of(getOpCode());
    }

    /**
     * In case of a bad request or other error status you can set the
     * message for this status with this function.
     *
     * @param message the status message
     * @since 0.5
     */
    public void setStatusMessage(String message) {
        Attribute attr = Attribute.of(ValueTags.TEXT_WITHOUT_LANGUAGE, "status-message", message);
        setOperationAttribute(attr);
    }

    /**
     * Gets the content of the attribute 'status-message'.
     *
     * @return string with the status message
     * @since 0.5
     */
    public String getStatusMessage() {
        return getAttribute("status-message").getStringValue();
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
