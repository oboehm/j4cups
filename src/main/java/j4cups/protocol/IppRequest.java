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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private static final Logger LOG = LoggerFactory.getLogger(IppRequest.class);

    private final String version;
    private final IppOperations operation;
    private final int requestId;
    private final List<AttributeGroup> attributeGroups;
    private final byte[] data;

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
        LOG.debug("IPP request with {} received.", bytes);
        this.version = bytes.get() + "." + bytes.get();
        this.operation = IppOperations.of(bytes.getShort());
        this.requestId = bytes.getInt();
        this.attributeGroups = readAttributeGroups(bytes);
        DelimiterTags endOfAttributeTag = DelimiterTags.of(bytes.get());
        LOG.trace("{} was read (and ignored).", endOfAttributeTag);
        this.data = readBytes(bytes);
        trace(bytes.array());
    }

    private void trace(byte[] bytes) {
        if (LOG.isTraceEnabled()) {
            LOG.trace(DatatypeConverter.printHexBinary(bytes));
            try {
                Path logFile = Files.createTempFile("IPP-" + requestId + "-", ".bin");
                Files.write(logFile, bytes);
                LOG.info("IPP request with {} bytes is recorded to '{}'.", bytes.length, logFile);
            } catch (IOException ioe) {
                LOG.debug("Cannot record {} bytes to temporary log file.", ioe);
                LOG.trace(DatatypeConverter.printHexBinary(bytes));
            }
        } else {
            LOG.debug("IPP request {} {} received (use TRACE level to dump it).", requestId, operation);
        }
    }

    private static List<AttributeGroup> readAttributeGroups(ByteBuffer buffer) {
        List<AttributeGroup> values = new ArrayList<>();
        while (buffer.remaining() > 4) {
            int pos = buffer.position();
            if (DelimiterTags.END_OF_ATTRIBUTES_TAG == DelimiterTags.of(buffer.get(pos))) {
                break;
            }
            values.add(new AttributeGroup(buffer));
        }
        return values;
    }

    private static byte[] readBytes(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
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
        return attributeGroups;
    }

    /**
     * Gets a collected list of all attributes of the attribute-groups.
     * 
     * @return list of all attibutes
     */
    public List<Attribute> getAttributes() {
        List<Attribute> attributes = new ArrayList<>();
        for (AttributeGroup group : attributeGroups) {
            attributes.addAll(group.getAttributes());
        }
        return attributes;
    }

    /**
     * Gets the attribute with the given name. If not attribute with the given
     * name is found an {@link IllegalArgumentException} will be thrown.
     * 
     * @param name name of the attribute
     * @return attribute with given name
     */
    public Attribute getAttribute(String name) {
        for (Attribute attr : getAttributes()) {
            if (name.equals(attr.getName())) {
                return attr;
            }
        }
        throw new IllegalArgumentException("no attribute '" + name + "' found");
    }

    /**
     * Returns the data part of the request. If no data part is present a
     * 0-length byte array is returned.
     * 
     * @return data part
     */
    public byte[] getData() {
        return data;
    }

    /**
     * The data part of the request can be empty.
     * 
     * @return false if data part is empty, true otherwise
     */
    public boolean hasData() {
        return data.length > 0;
    }

    /**
     * With this toString() implementation we want to provide the most
     * important values which are useful for logging and debugging.
     * 
     * @return a String with operation, request-id and other stuff
     */
    @Override
    public String toString() {
        String hex = "";
        if (hasData()) {
            hex = StringUtils.abbreviateMiddle(DatatypeConverter.printHexBinary(this.getData()), "...", 100) + "|";
        }
        return "|" + getVersion() + "|" + getOperation() + "|" + getRequestId() + "|...(" +
                getAttributes().size() + " attributes)...|" + hex;
    }
    
}
