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
 * (c)reated 10.02.2018 by oboehm (boehm@javatux.de)
 */
package j4cups.protocol.attr;

import j4cups.protocol.Binary;
import j4cups.protocol.tags.DelimiterTags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Each "attribute-group" field represents a single group of
 * attributes, such as an Operation Attributes group or a Job Attributes
 * group (see the Model document). The IPP model document specifies the
 * required attribute groups and their order for each operation request
 * and response.
 * <p>
 * Each "attribute-group" field is encoded as follows:
 * </p>
 * <pre>
 * -----------------------------------------------
 * |           begin-attribute-group-tag         |  1 byte
 * ----------------------------------------------------------
 * |                   attribute                 |  p bytes |- 0 or more
 * ----------------------------------------------------------
 * </pre>
 *
 * @author oboehm
 * @since 0.0.2 (10.02.2018)
 */
public class AttributeGroup implements Binary {

    private static final Logger LOG = LoggerFactory.getLogger(AttributeGroup.class);
    private final DelimiterTags beginTag;
    private final List<Attribute> attributes;

    /**
     * This is the copy construtor.
     * 
     * @param other AttributeGroup which is copied
     */
    public AttributeGroup(AttributeGroup other) {
        this(ByteBuffer.wrap(other.toByteArray()));
    }

    /**
     * Instantiates a new empty attribute group from the given bytes.
     *
     * @param type e.g. {@link DelimiterTags#OPERATION_ATTRIBUTES_TAG}
     */
    public AttributeGroup(DelimiterTags type) {
        this.beginTag = type;
        this.attributes = new ArrayList<>();
    }

    /**
     * Instantiates a new attribute group from the given bytes.
     * The given {@link ByteBuffer} must be positioned at the beginning of the
     * attribute-group.
     *
     * @param bytes ByteBuffer positioned at the beginning
     */
    public AttributeGroup(ByteBuffer bytes) {
        this.beginTag = DelimiterTags.of(bytes.get());
        this.attributes = readAttributes(bytes);
    }

    private static List<Attribute> readAttributes(ByteBuffer buffer) {
        List<Attribute> values = new ArrayList<>();
        while (buffer.remaining() > 4) {
            int pos = buffer.position();
            if (DelimiterTags.isValid(buffer.get(pos)) && (buffer.getShort(pos+1) != 0)) {
                break;
            }
            values.add(new Attribute(buffer));
        }
        return values;
    }

    /**
     * The "begin-attribute-group-tag" field marks the beginning of an
     * "attribute-group" field and its value identifies the type of
     * attribute group, e.g. Operations Attributes group versus a Job
     * Attributes group.
     * 
     * @return e.g. {@link DelimiterTags#OPERATION_ATTRIBUTES_TAG}
     */
    public DelimiterTags getBeginTag() {
        return beginTag;
    }

    /**
     * An "attribute-group" field contains zero or more "attribute" fields.
     * 
     * @return list of attributes
     */
    public List<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * Looks for the attribute with the given name and returns it. If the 
     * attribute does not exist an {@link IllegalArgumentException} will be
     * thrown.
     * 
     * @param name of the attribute
     * @return found attribute
     */
    public Attribute getAttribute(String name) {
        for (Attribute attr : attributes) {
            if (name.equals(attr.getName())) {
                return attr;
            }
        }
        throw new IllegalArgumentException("attribute '" + name + "' not found in " + attributes);
    }

    /**
     * Checks if the given attribute is available.
     *
     * @param name the name of the attribute
     * @return true or false
     * @since 0.5
     */
    public boolean hasAttribute(String name) {
        for (Attribute attr : attributes) {
            if (name.equals(attr.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds an attribute to the given attributes.
     *
     * @param attr the new attribute
     */
    public void addAttribute(Attribute attr) {
        if (hasAttribute(attr.getName())) {
            Attribute existing = getAttribute(attr.getName());
            LOG.debug("{} is overwritten with {}.", existing, attr);
            existing.setValue(attr.getValue());
        } else {
            attributes.add(attr);
            LOG.debug("{} is added.", attr);
        }
    }

    /**
     * With this toString() implementation we want to provide the most
     * important values which are useful for logging and debugging.
     *
     * @return a String with operation, request-id and other stuff
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("|");
        for (Attribute attr : getAttributes()) {
            buffer.append(attr.toString()).append('|');
        }
        return buffer.toString();
    }

    /**
     * All attribute values are put into the resulting string.
     *
     * @return string with all attributes
     */
    public String toLongString() {
        StringBuilder buffer = new StringBuilder("|");
        buffer.append(String.format("%02x", getBeginTag().getValue())).append('|');
        for (Attribute attr : getAttributes()) {
            buffer.append(attr.toLongString()).append('|');
        }
        return buffer.toString();
    }

    /**
     * Converts the attribute-group to a byte array as described in RFC-2910 and
     * writes it to the given output-stream.
     * <pre>
     * -----------------------------------------------
     * |           begin-attribute-group-tag         |  1 byte
     * ----------------------------------------------------------
     * |                   attribute                 |  p bytes |- 0 or more
     * ----------------------------------------------------------
     * </pre> 
     *
     * @param ostream an output stream
     * @throws IOException in case of I/O problems
     */
    @Override
    public void writeBinaryTo(OutputStream ostream) throws IOException {
        DataOutputStream dos = new DataOutputStream(ostream);
        dos.writeByte(getBeginTag().getValue());
        for (Attribute attr : getAttributes()) {
            dos.write(attr.toByteArray());
        }
    }

}
