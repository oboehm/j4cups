/*
 * Copyright (c) 2018-2020 by Oliver Boehm
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
package j4cups.protocol.attr

import j4cups.protocol.Binary
import j4cups.protocol.attr.AttributeGroup
import j4cups.protocol.tags.DelimiterTags
import org.slf4j.LoggerFactory
import java.io.DataOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.*

/**
 * Each "attribute-group" field represents a single group of
 * attributes, such as an Operation Attributes group or a Job Attributes
 * group (see the Model document). The IPP model document specifies the
 * required attribute groups and their order for each operation request
 * and response.
 *
 * Each "attribute-group" field is encoded as follows:
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
class AttributeGroup : Binary {

    /**
     * The "begin-attribute-group-tag" field marks the beginning of an
     * "attribute-group" field and its value identifies the type of
     * attribute group, e.g. Operations Attributes group versus a Job
     * Attributes group.
     */
    val beginTag: DelimiterTags
    private val attributes: MutableList<Attribute>

    /**
     * This is the copy construtor.
     *
     * @param other AttributeGroup which is copied
     */
    constructor(other: AttributeGroup) : this(ByteBuffer.wrap(other.toByteArray())) {}

    /**
     * Instantiates a new empty attribute group from the given bytes.
     *
     * @param type e.g. [DelimiterTags.OPERATION_ATTRIBUTES_TAG]
     */
    constructor(type: DelimiterTags) {
        beginTag = type
        attributes = ArrayList()
    }

    /**
     * Instantiates a new attribute group from the given bytes.
     * The given [ByteBuffer] must be positioned at the beginning of the
     * attribute-group.
     *
     * @param bytes ByteBuffer positioned at the beginning
     */
    constructor(bytes: ByteBuffer) {
        beginTag = DelimiterTags.of(bytes.get().toInt())
        attributes = readAttributes(bytes)
    }

    /**
     * An "attribute-group" field contains zero or more "attribute" fields.
     *
     * @return list of attributes
     */
    fun getAttributes(): List<Attribute> {
        return attributes
    }

    /**
     * Looks for the attribute with the given name and returns it. If the
     * attribute does not exist an [IllegalArgumentException] will be
     * thrown.
     *
     * @param name of the attribute
     * @return found attribute
     */
    fun getAttribute(name: String): Attribute {
        for (attr in attributes) {
            if (name == attr.name) {
                return attr
            }
        }
        throw IllegalArgumentException("attribute '$name' not found in $attributes")
    }

    /**
     * Checks if the given attribute is available.
     *
     * @param name the name of the attribute
     * @return true or false
     * @since 0.5
     */
    fun hasAttribute(name: String): Boolean {
        for (attr in attributes) {
            if (name == attr.name) {
                return true
            }
        }
        return false
    }

    /**
     * Adds an attribute to the given attributes.
     *
     * @param attr the new attribute
     */
    fun addAttribute(attr: Attribute) {
        if (hasAttribute(attr.name)) {
            val existing = getAttribute(attr.name)
            LOG.debug("{} is overwritten with {}.", existing, attr)
            existing.value = attr.value
        } else {
            attributes.add(attr)
            LOG.debug("{} is added.", attr)
        }
    }

    /**
     * With this toString() implementation we want to provide the most
     * important values which are useful for logging and debugging.
     *
     * @return a String with operation, request-id and other stuff
     */
    override fun toString(): String {
        val buffer = StringBuilder("|")
        for (attr in getAttributes()) {
            buffer.append(attr.toString()).append('|')
        }
        return buffer.toString()
    }

    /**
     * All attribute values are put into the resulting string.
     *
     * @return string with all attributes
     */
    fun toLongString(): String {
        val buffer = StringBuilder("|")
        buffer.append(String.format("%02x", beginTag.value)).append('|')
        for (attr in getAttributes()) {
            buffer.append(attr.toLongString()).append('|')
        }
        return buffer.toString()
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
    @Throws(IOException::class)
    override fun writeBinaryTo(ostream: OutputStream) {
        val dos = DataOutputStream(ostream)
        dos.writeByte(beginTag.value.toInt())
        for (attr in getAttributes()) {
            dos.write(attr.toByteArray())
        }
    }



    companion object {

        private val LOG = LoggerFactory.getLogger(AttributeGroup::class.java)

        private fun readAttributes(buffer: ByteBuffer): MutableList<Attribute> {
            val values: MutableList<Attribute> = ArrayList()
            while (buffer.remaining() > 4) {
                val pos = buffer.position()
                if (DelimiterTags.isValid(buffer[pos].toInt()) && buffer.getShort(pos + 1).toInt() != 0) {
                    break
                }
                values.add(Attribute(buffer))
            }
            return values
        }
    }

}
