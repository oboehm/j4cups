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
 * (c)reated 16.02.2018 by oboehm (ob@oasd.de)
 */
package j4cups.protocol;

import j4cups.protocol.attr.Attribute;
import j4cups.protocol.attr.AttributeGroup;
import j4cups.protocol.tags.DelimiterTags;
import j4cups.protocol.tags.ValueTags;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The class AbstractIpp is the common super class of {@link IppRequest} and
 * {@link IppResponse} which encapsulates the common code of both classes.
 * An operation request or response is defined in RFC-2910:
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
 * @since 0.2 (16.02.2018)
 */
public abstract class AbstractIpp implements Externalizable {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractIpp.class);
    
    /** The actual supported version is 2. **/
    protected static final Version DEFAULT_VERSION = new Version((byte) 2, (byte) 0);

    private transient Version version;
    private short opCode;
    private transient List<AttributeGroup> attributeGroups;
    private int requestId;
    private byte[] data;

    /**
     * This constructor is needed for the {@link Externalizable} interface.
     */
    protected AbstractIpp() {
        this(new Version((byte) 2, (byte) 0), (short) 0, 0, new ArrayList<>(), new byte[0]);
    }

    /**
     * Instantiates a new IPP request or responsel from the given bytes.
     *
     * @param bytes the bytes of the IPP request
     */
    protected AbstractIpp(byte[] bytes) {
        this(ByteBuffer.wrap(bytes));
    }

    /**
     * Instantiates a new IPP request or response from the given bytes.
     *
     * @param bytes the bytes of the IPP request
     */
    public AbstractIpp(ByteBuffer bytes) {
        this(new Version(bytes.get(), bytes.get()), bytes.getShort(), bytes.getInt(), readAttributeGroups(bytes), 
                readData(bytes));
        LOG.debug("IPP package with {} received.", bytes);
        trace(bytes.array());
    }

    /**
     * Instantiates a new IPP request or response with no data.
     *
     * @param version   the version
     * @param opCode    the code for operation-id or status-code
     * @param requestId the request id
     * @param groups    the attribute groups
     */
    public AbstractIpp(Version version, short opCode, int requestId, List<AttributeGroup> groups) {
        this(version, opCode, requestId, groups, new byte[0]);
    }

    /**
     * Instantiates a new IPP request or response.
     *
     * @param version   the version
     * @param opCode    the code for operation-id or status-code
     * @param requestId the request id
     * @param groups    the attribute groups
     * @param data      the data
     */
    public AbstractIpp(Version version, short opCode, int requestId, List<AttributeGroup> groups, byte[] data) {
        this.version = version;
        this.opCode = opCode;
        this.requestId = requestId;
        this.attributeGroups = groups;
        this.data = data;
        fillAttributeGroups(groups);
    }

    private static void fillAttributeGroups(List<AttributeGroup> values) {
        List<DelimiterTags> requiredTags =
                new ArrayList(Arrays.asList(DelimiterTags.OPERATIONS_ATTRIBUTES_TAG, DelimiterTags.JOB_ATTRIBUTES_TAG,
                        DelimiterTags.PRINTER_ATTRIBUTES_TAG, DelimiterTags.UNSUPPORTED_ATTRIBUTES_TAG));
        for (AttributeGroup group : values) {
            DelimiterTags tag = group.getBeginTag();
            requiredTags.remove(tag);
        }
        for (DelimiterTags tag : requiredTags) {
            AttributeGroup group = new AttributeGroup(tag);
            values.add(group);
            LOG.trace("Empty {} added.", group);
        }
    }

    private void trace(byte[] bytes) {
        if (LOG.isTraceEnabled()) {
            LOG.trace(DatatypeConverter.printHexBinary(bytes));
            try {
                Path logDir = Paths.get(SystemUtils.getJavaIoTmpDir().toString(), "IPP");
                Files.createDirectories(logDir);
                Path logFile = Paths.get(logDir.toString(),
                        Long.toString(System.currentTimeMillis(), Character.MAX_RADIX) + "-" +
                                this.getClass().getSimpleName() + this.getRequestId() + this.getOpCodeAsString() +
                                ".bin");
                Files.write(logFile, bytes);
                LOG.info("IPP package with {} bytes is recorded to '{}'.", bytes.length, logFile);
            } catch (IOException ioe) {
                LOG.debug("Cannot record {} bytes to temporary log file.", ioe);
                LOG.trace(DatatypeConverter.printHexBinary(bytes));
            }
        } else {
            LOG.debug("{}-{} {} received (use TRACE level to dump it).", getClass().getSimpleName(), requestId,
                    getOpCodeAsString());
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

    private static byte[] readData(ByteBuffer buffer) {
        DelimiterTags endOfAttributeTag = DelimiterTags.of(buffer.get());
        LOG.trace("{} was read (and ignored).", endOfAttributeTag);
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
    public Version getVersion() {
        return version;
    }

    /**
     * Sets the 2nd part (byte 2-3) with the new operation-id or status-code.
     * 
     * @param opCode e.g.0x0000 for status-code 'successful_ok'
     */
    public void setOpCode(short opCode) {
        this.opCode = opCode;
    }

    /**
     * Returns the 2nd part (byte 2-3) with the operation-id or status-code.
     *
     * @return e.g. 0x0005 for 'Create-Job'
     */
    public short getOpCode() {
        return opCode;
    }

    /**
     * Returns the 2nd part (byte 2-3) with the operation-id or status-code as
     * string.
     * 
     * @return e.g. "Create-Job"
     */
    abstract protected String getOpCodeAsString();

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
     * Returns all attributes of the given delimiter-tag.
     * 
     * @param tag the delimiter-tag
     * @return list of attributes
     */
    public AttributeGroup getAttributeGroup(DelimiterTags tag) {
        for (AttributeGroup group : getAttributeGroups()) {
            if (group.getBeginTag() == tag) {
                return group;
            }
        }
        throw new IllegalArgumentException("attribute-group " + tag + " not found");
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
     * Sets the attribute with the given name. If it exits the old value will
     * be overridden. If it does not exist it will be inserted in the
     * unsupported attribute-groups section with "unknown" as value tag.
     * 
     * @param name name of the attribute
     * @param value byte values of the attribute
     * @see #setAttribute(Attribute, DelimiterTags) 
     */
    public void setAttribute(String name, byte[] value) {
        try {
            Attribute attr = getAttribute(name);
            attr.setValue(value);
        } catch (IllegalArgumentException iae) {
            LOG.debug("Attribute '{}' will be inserted as 'unsupported/unknown', because not found in existing attributes.", name);
            LOG.trace("Details:", iae);
            setAttribute(Attribute.of(ValueTags.UNKNOWN, name, value), DelimiterTags.UNSUPPORTED_ATTRIBUTES_TAG);
        }
    }

    /**
     * Sets the attribute into the group defined by the groupTag.
     *
     * @param attr the attribute
     * @param groupTag defines the attribute-group where the attribute is inserted
     */
    public void setAttribute(Attribute attr, DelimiterTags groupTag) {
        AttributeGroup group = getAttributeGroup(groupTag);
        group.addAttribute(attr);
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
     * Sets the data part of the request or response.
     * 
     * @param data new data
     * @since 0.3
     */
    public void setData(byte[] data) {
        this.data = data;
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
        String attrs = "|" + getRequestId() + "|...(" + getAttributes().size() + " attributes)...|";
        return buildString(attrs);
    }

    /**
     * Puts all attribute information into the resulting string. The data
     * value may be abbreviated if it is too long. But you can set the log
     * level to TRACE if you want to record the full request.
     *
     * @return string with all attribute values
     */
    public String toLongString() {
        StringBuilder attrs = new StringBuilder();
        for (AttributeGroup group : attributeGroups) {
            attrs.append('|').append(group.toLongString());
        }
        return buildString(attrs.substring(1));
    }

    private String buildString(String attrs) {
        String hex = "";
        if (hasData()) {
            hex = StringUtils.abbreviateMiddle(DatatypeConverter.printHexBinary(this.getData()), "...", 100) + "|";
        }
        return "|" + getVersion() + "|" + getOpCodeAsString() + attrs + hex;
    }

    /**
     * Converts the IppResponse to a byte array as described in RFC-2910.
     * <pre>
     *  -----------------------------------------------
     *  |                  version-number             |   2 bytes  - required
     *  -----------------------------------------------
     *  |          operation-id or status-code        |   2 bytes  - required
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
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            writeTo(byteStream);
            byteStream.flush();
            return byteStream.toByteArray();
        } catch (IOException ioe) {
            throw new IllegalStateException("cannot dump package", ioe);
        }
    }
    
    private void writeTo(OutputStream ostream) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(ostream)) {
            dos.write(version.toByteArray());
            dos.writeShort(getOpCode());
            dos.writeInt(getRequestId());
            for (AttributeGroup group : getAttributeGroups()) {
                if (!group.getAttributes().isEmpty()) {
                    dos.write(group.toByteArray());
                }
            }
            dos.writeByte(DelimiterTags.END_OF_ATTRIBUTES_TAG.getValue());
            dos.write(getData());
            dos.flush();
        }
    }

    /**
     * The object implements the writeExternal method to save its contents
     * by storing it as byte array.
     *
     * @param out the stream to write the object to
     * @throws IOException Includes any I/O exceptions that may occur
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.write(toByteArray());
    }

    /**
     * The object implements the readExternal method to restore its
     * contents by reading the byte array and use this array to build up
     * an IPP request or response.
     *
     * @param in the stream to read data from in order to restore the object
     * @throws IOException            if I/O errors occur
     * @throws ClassNotFoundException If the class for an object being
     *                                restored cannot be found.
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.version = new Version(in.readByte(), in.readByte());
        this.opCode = in.readShort();
        this.requestId = in.readInt();
        try (ByteArrayOutputStream ostream = new ByteArrayOutputStream()) {
            while (true) {
                int x = in.read();
                if (x < 0) {
                    break;
                }
                ostream.write(x);
            }
            ostream.flush();
            ByteBuffer buffer = ByteBuffer.wrap(ostream.toByteArray());
            this.attributeGroups.addAll(readAttributeGroups(buffer));
            this.data = readData(buffer);
        }
    }

    /**
     * Container for the version information.
     */
    public static class Version {
        
        private final byte[] bytes = new byte[2];

        /**
         * Expects the first two bytes of an IPP request or response.
         * 
         * @param major first byte
         * @param minor 2nd byte
         */
        public Version(byte major, byte minor) {
            bytes[0] = major;
            bytes[1] = minor;
        }

        /**
         * Prints the two bytes as string.
         * 
         * @return normally "2.0"
         */
        @Override
        public String toString() {
            return bytes[0] + "." + bytes[1];
        }
        
        public byte[] toByteArray() {
            return bytes;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Version)) {
                return false;
            }
            Version other = (Version) obj;
            return Arrays.equals(this.bytes, other.bytes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bytes);
        }
    }
    
}
