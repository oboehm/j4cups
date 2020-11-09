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
 * (c)reated 11.02.2018 by Oli B. (boehm@javatux.de)
 */
package j4cups.protocol.attr

import j4cups.protocol.Binary
import j4cups.protocol.tags.ValueTags
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.OutputStream
import java.math.BigInteger
import java.net.URI
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import javax.xml.bind.DatatypeConverter

/**
 * An "attribute" field is encoded as attribute-with-one-value and (optional)
 * several addtional-values.
 * <pre>
 * -----------------------------------------------
 * |          attribute-with-one-value           |  q bytes
 * ----------------------------------------------------------
 * |             additional-value                |  r bytes |- 0 or more
 * ----------------------------------------------------------
 * </pre>
 *
 * When an attribute is single valued (e.g. "copies" with value of 10)
 * or multi-valued with one value (e.g. "sides-supported" with just the
 * value 'one-sided') it is encoded with just an "attribute-with-one-
 * value" field. When an attribute is multi-valued with n values (e.g.
 * "sides-supported" with the values 'one-sided' and 'two-sided-long-
 * edge'), it is encoded with an "attribute-with-one-value" field
 * followed by n-1 "additional-value" fields.
 *
 * @author Oli B.
 * @since 0.0.2 (11.02.2018)
 */
class Attribute private constructor(additionalValues: List<AdditionalValue>) : Binary {

    private val additionalValues: MutableList<AdditionalValue> = ArrayList()

    /**
     * Instantiates a new (single valued or multi-valued) attribute from the
     * given bytes. The given [ByteBuffer] must be positioned at the
     * beginning of the attribute.
     *
     * @param bytes ByteBuffer positioned at the beginning
     */
    constructor(bytes: ByteBuffer) : this(readAdditionalValues(bytes)) {}
    private constructor(value: AttributeWithOneValue) : this(listOf(AdditionalValue(value))) {}

    /**
     * The "value-tag" field specifies the attribute syntax, e.g. 0x44
     * for the attribute syntax 'keyword'.
     *
     * @return e.g. [ValueTags.KEYWORD]
     */
    val valueTag: ValueTags
        get() = additionalValues[0].valueTag

    /**
     * The "name" field contains the textual name of the attribute.
     *
     * @return e.g. "slides-supported"
     */
    val name: String
        get() = additionalValues[0].name

    /**
     * The "value" field contains the value of the attribute.
     * If the attribute is a multi-value attribute this returns only the
     * fist value.
     */
    var value: ByteArray
        get() = additionalValues[0].value
        set(value) {
            additionalValues[0].value = value
        }

    /**
     * Gets the different values for a multi-value attribute.
     *
     * @return the values
     */
    fun getAdditionalValues(): List<AdditionalValue> {
        return additionalValues
    }

    /**
     * The "value" field contains the value of a integer attribute.
     *
     * @return a 32-bit number
     */
    val intValue: Int
        get() = BigInteger(value).toInt()

    /**
     * The "value" field contains the value of a boolean attribute. The boolean
     * value is stored as byte, whereas 0x00 is 'false' and 0x01 is 'true'.
     *
     * @return true (0x01) or false (0x00)
     */
    val booleanValue: Boolean
        get() {
            val booleanByte = value[0]
            return when (booleanByte) {
                0x00.toByte() -> false
                0x01.toByte() -> true
                else -> throw IllegalStateException("invalid boolean value stored: 0x" + Integer.toHexString(booleanByte.toInt()))
            }
        }

    /**
     * The "value" field contains the value of a charset attribute.
     *
     * @return e.g. "one-sided"
     */
    val stringValue: String
        get() = String(value, StandardCharsets.UTF_8)

    /**
     * The "value" field contains the value of a URI attribute.
     *
     * @return an URI
     */
    val uriValue: URI
        get() = URI.create(stringValue)

    /**
     * When the attribte value is multi-valued this method returns true.
     *
     * @return true or false
     */
    val isMultiValue: Boolean
        get() = additionalValues.size > 1

    /**
     * This method shows a short representation of an attribute. The
     * difference to [.toLongString] is, that in case of a multi-value
     * attribute only the first value is shown.
     *
     * @return e.g. "requested-attributes=copies-supported,..."
     */
    override fun toString(): String {
        val buffer = StringBuilder(name)
        buffer.append("=")
        if (isMultiValue) {
            buffer.append("...")
        } else {
            getSingleValueAsString(buffer)
        }
        return buffer.toString()
    }

    /**
     * This method shows a short representation of an attribute. I.e. in case
     * of a multi-value attribute all values are shown.
     *
     * @return string with all values
     */
    fun toLongString(): String {
        val buffer = StringBuilder(name)
        buffer.append("=")
        for (addValue in additionalValues) {
            if (valueTag.isCharacterStringValue) {
                buffer.append(addValue.stringValue)
            } else {
                buffer.append(DatatypeConverter.printHexBinary(addValue.value))
            }
            buffer.append(',')
        }
        buffer.deleteCharAt(buffer.length - 1)
        return buffer.toString()
    }

    private fun getSingleValueAsString(buffer: StringBuilder) {
        val tag = valueTag
        if (tag.isIntegerValue) {
            buffer.append(intValue)
        } else if (tag.isCharacterStringValue) {
            buffer.append(stringValue)
        } else {
            buffer.append(DatatypeConverter.printHexBinary(value))
        }
    }

    /**
     * Converts the attribute to a byte array as described in RFC-2910 and
     * writes it to the given output-stream.
     *
     * @param ostream an output stream
     * @throws IOException in case of I/O problems
     */
    @Throws(IOException::class)
    override fun writeBinaryTo(ostream: OutputStream) {
        val dos = DataOutputStream(ostream)
        for (av in additionalValues) {
            dos.write(av.toByteArray())
        }
    }

    /**
     * For multi-value attributes you can use this method to add the single
     * values to the attribute.
     *
     * @param value the value
     */
    fun add(value: Attribute) {
        additionalValues.add(AdditionalValue(value))
    }

    /**
     * An "attribute-with-one-value" field is encoded with five subfields.
     * <pre>
     * -----------------------------------------------
     * |                   value-tag                 |   1 byte
     * -----------------------------------------------
     * |               name-length  (value is u)     |   2 bytes
     * -----------------------------------------------
     * |                     name                    |   u bytes
     * -----------------------------------------------
     * |              value-length  (value is v)     |   2 bytes
     * -----------------------------------------------
     * |                     value                   |   v bytes
     * -----------------------------------------------
     * </pre>
     *
     * The "value-tag" field specifies the attribute syntax, e.g. 0x44
     * for the attribute syntax 'keyword'.
     *
     * The "name-length" field specifies the length of the "name" field
     * in bytes, e.g. u in the above diagram or 15 for the name "sides-
     * supported".
     *
     * The "name" field contains the textual name of the attribute, e.g.
     * "sides-supported".
     *
     * The "value-length" field specifies the length of the "value" field
     * in bytes, e.g. v in the above diagram or 9 for the (keyword) value
     * 'one-sided'.
     *
     * The "value" field contains the value of the attribute, e.g. the
     * textual value 'one-sided'.
     */
    open class AttributeWithOneValue {

        /**
         * The "value-tag" field specifies the attribute syntax, e.g. 0x44
         * for the attribute syntax 'keyword'.
         */
        val valueTag: ValueTags

        /**
         * The "name" field contains the textual name of the attribute.
         */
        val name: String
        /**

         * The "value" field contains the value of the attribute.
         */
        var value: ByteArray

        /**
         * Instantiates a new attribute-with-one-value from the given bytes.
         * The given [ByteBuffer] must be positioned at the beginning of the
         * attribute-group.
         *
         * @param bytes ByteBuffer positioned at the beginning
         */
        constructor(bytes: ByteBuffer) {
            valueTag = ValueTags.of(bytes.get().toInt())
            val nameLength = bytes.short
            name = readString(bytes, nameLength)
            val valueLength = bytes.short
            value = readBytes(bytes, valueLength)
        }

        /**
         * Instantiates a new attribute-with-one-value.
         *
         * @param valueTag the value-tag
         * @param name     name of the attribute
         * @param value    binary value of the attribute
         */
        constructor(valueTag: ValueTags, name: String, value: ByteArray) {
            this.valueTag = valueTag
            this.name = name
            this.value = value
        }

        /**
         * The "value" field contains the value of the attribute.
         *
         * @return e.g. "one-sided"
         */
        val stringValue: String
            get() = String(value, StandardCharsets.UTF_8)

        /**
         * Converts an attribute-with-one-value to a byte array.
         * <pre>
         * -----------------------------------------------
         * |                   value-tag                 |   1 byte
         * -----------------------------------------------
         * |               name-length  (value is u)     |   2 bytes
         * -----------------------------------------------
         * |                     name                    |   u bytes
         * -----------------------------------------------
         * |              value-length  (value is v)     |   2 bytes
         * -----------------------------------------------
         * |                     value                   |   v bytes
         * -----------------------------------------------
         * </pre>
         *
         * @return byte array
         */
        fun toByteArray(): ByteArray {
            try {
                ByteArrayOutputStream().use { byteStream ->
                    writeTo(byteStream)
                    byteStream.flush()
                    return byteStream.toByteArray()
                }
            } catch (ioe: IOException) {
                throw IllegalStateException("cannot dump attribute", ioe)
            }
        }

        @Throws(IOException::class)
        private fun writeTo(ostream: OutputStream) {
            val dos = DataOutputStream(ostream)
            dos.writeByte(valueTag.value.toInt())
            dos.writeShort(name.length)
            dos.write(name.toByteArray(StandardCharsets.UTF_8))
            dos.writeShort(value.size)
            dos.write(value)
        }

        companion object {
            private fun readString(buffer: ByteBuffer, length: Short): String {
                val bytes = readBytes(buffer, length)
                return String(bytes, StandardCharsets.UTF_8)
            }

            private fun readBytes(buffer: ByteBuffer, length: Short): ByteArray {
                val value = ByteArray(length.toInt())
                for (i in 0 until length) {
                    value[i] = buffer.get()
                }
                return value
            }
        }
    }

    /**
     * An "additional-value" has four entries.
     * <pre>
     * -----------------------------------------------
     * |                   value-tag                 |   1 byte
     * -----------------------------------------------
     * |            name-length  (value is 0x0000)   |   2 bytes
     * -----------------------------------------------
     * |              value-length (value is w)      |   2 bytes
     * -----------------------------------------------
     * |                     value                   |   w bytes
     * -----------------------------------------------
     * </pre>
     */
    class AdditionalValue(bytes: ByteBuffer) : AttributeWithOneValue(bytes) {
        constructor(attr: Attribute) : this(attr.toByteArray()) {}
        constructor(attr: AttributeWithOneValue) : this(attr.toByteArray()) {}
        constructor(bytes: ByteArray?) : this(ByteBuffer.wrap(bytes)) {}
    }



    companion object {

        private fun readAdditionalValues(buffer: ByteBuffer): List<AdditionalValue> {
            val values: MutableList<AdditionalValue> = ArrayList()
            while (buffer.remaining() > 4) {
                values.add(AdditionalValue(buffer))
                val pos = buffer.position()
                if (buffer.remaining() <= 4 || !ValueTags.isValid(buffer[pos].toInt()) || buffer.getShort(pos + 1).toInt() != 0) {
                    break
                }
            }
            return values
        }

        /**
         * Creates a singe-value attribute for integer values.
         *
         * @param name e.g. "job-id"
         * @param value e.g. 42
         * @return the attribute
         */
        @JvmStatic
        fun of(name: String, value: Int): Attribute {
            return of(ValueTags.INTEGER, name, value)
        }

        /**
         * Creates a singe-value attribute for a range of integer values.
         *
         * @param name e.g. "copies-supported"
         * @param min e.g. 1
         * @param max e.g. 9999
         * @return the attribute
         */
        @JvmStatic
        fun of(name: String, min: Int, max: Int): Attribute {
            val data = ByteArray(8)
            val buf = ByteBuffer.wrap(data)
            buf.putInt(min)
            buf.putInt(max)
            return of(ValueTags.RANGE_OF_INTEGER, name, data)
        }

        /**
         * Creates a singe-value attribute for boolean values.
         *
         * @param name e.g. "last-document"
         * @param value true or falce
         * @return the attribute
         */
        @JvmStatic
        fun of(name: String, value: Boolean): Attribute {
            val booleanByte = ByteArray(1)
            booleanByte[0] = (if (value) 0x01 else 0x00).toByte()
            return of(ValueTags.BOOLEAN, name, booleanByte)
        }

        /**
         * Creates a singe-value attribute for URI values.
         *
         * @param name e.g. "job-uri"
         * @param value an URI
         * @return the attribute
         */
        @JvmStatic
        fun of(name: String, value: URI): Attribute {
            return of(ValueTags.URI, name, value.toString().toByteArray(StandardCharsets.UTF_8))
        }

        /**
         * Creates a singe-value attribute for charset values.
         *
         * @param name e.g. "attributes-charset"
         * @param value e.g. [StandardCharsets.UTF_8]
         * @return the attribute
         */
        @JvmStatic
        fun of(name: String, value: Charset): Attribute {
            return of(ValueTags.CHARSET, name, value.name().toLowerCase())
        }

        /**
         * Creates a singe-value attribute for the given language.
         *
         * @param name e.g. "attributes-natural-language"
         * @param value e.g. the English [Locale]
         * @return the attribute
         */
        @JvmStatic
        fun of(name: String, value: Locale): Attribute {
            return of(ValueTags.NATURAL_LANGUAGE, name, value.language)
        }

        /**
         * Creates a singe-value attribute for integer based values.
         *
         * @param tag   the value-tag
         * @param name  the name of the attribute
         * @param value the string value of the attribute
         * @return the attribute
         */
        @JvmStatic
        fun of(tag: ValueTags, name: String, value: Int): Attribute {
            val bytes = ByteArray(4)
            ByteBuffer.wrap(bytes).putInt(value)
            return of(tag, name, bytes)
        }

        /**
         * Creates a singe-value attribute.
         *
         * @param tag   the value-tag
         * @param name  the name of the attribute
         * @param value the binary value of the attribute
         * @return the attribute
         */
        @JvmStatic
        fun of(tag: ValueTags, name: String, value: ByteArray): Attribute {
            val attr = AttributeWithOneValue(tag, name, value)
            return Attribute(attr)
        }

        /**
         * Creates a multi-value attribute for the given value-tag.
         *
         * @param tag              the value-tag
         * @param name             the name of the attribute
         * @param additionalValues the additional values
         * @return the attribute
         */
        @JvmStatic
        fun of(tag: ValueTags, name: String, vararg additionalValues: ByteArray): Attribute {
            val attr = Attribute(AttributeWithOneValue(tag, name, ByteArray(0)))
            for (bytes in additionalValues) {
                attr.add(of(tag, "", bytes))
            }
            return attr
        }

        /**
         * Creates a multi-value attribute for character-string values.
         *
         * @param tag              the value-tag
         * @param name             the name of the attribute
         * @param additionalValues additional values
         * @return the attribute
         */
        @JvmStatic
        fun of(tag: ValueTags, name: String, vararg additionalValues: String): Attribute {
            val attr = Attribute(AttributeWithOneValue(tag, name, ByteArray(0)))
            for (s in additionalValues) {
                attr.add(of(tag, "", s.toByteArray(StandardCharsets.UTF_8)))
            }
            return attr
        }

        /**
         * Creates a single-value attribute for character-string values.
         *
         * @param tag   the value-tag
         * @param name  the name of the attribute
         * @param value the value
         * @return the attribute
         */
        @JvmStatic
        fun of(tag: ValueTags, name: String, value: String): Attribute {
            return Attribute(AttributeWithOneValue(tag, name, value.toByteArray(StandardCharsets.UTF_8)))
        }

        /**
         * Creates a multi-value attribute with the different printer resolutions.
         *
         * @param name        the name
         * @param resolutions the different printer resolutions
         * @return the attribute
         */
        @JvmStatic
        fun of(name: String, vararg resolutions: PrinterResolution): Attribute {
            val attr = Attribute(AttributeWithOneValue(ValueTags.RESOLUTION, name, ByteArray(0)))
            for (pr in resolutions) {
                attr.add(of(ValueTags.RESOLUTION, "", pr.toByteArray()))
            }
            return attr
        }
    }

    init {
        this.additionalValues.addAll(additionalValues)
    }

}