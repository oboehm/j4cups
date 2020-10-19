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
package j4cups.protocol

import j4cups.protocol.attr.Attribute
import j4cups.protocol.attr.AttributeGroup
import j4cups.protocol.enums.JobState
import j4cups.protocol.enums.JobStateReasons
import j4cups.protocol.tags.DelimiterTags
import j4cups.protocol.tags.ValueTags
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.SystemUtils
import org.slf4j.LoggerFactory
import java.io.*
import java.net.URI
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import javax.validation.ValidationException
import javax.xml.bind.DatatypeConverter

/**
 * The class AbstractIpp is the common super class of [IppRequest] and
 * [IppResponse] which encapsulates the common code of both classes.
 * An operation request or response is defined in RFC-2910:
 * <pre>
 * -----------------------------------------------
 * |                  version-number             |   2 bytes  - required
 * -----------------------------------------------
 * |               operation-id (request)        |
 * |                      or                     |   2 bytes  - required
 * |               status-code (response)        |
 * -----------------------------------------------
 * |                   request-id                |   4 bytes  - required
 * -----------------------------------------------
 * |                 attribute-group             |   n bytes - 0 or more
 * -----------------------------------------------
 * |              end-of-attributes-tag          |   1 byte   - required
 * -----------------------------------------------
 * |                     data                    |   q bytes  - optional
 * -----------------------------------------------
 * </pre>
 *
 * @author oboehm
 * @since 0.2 (16.02.2018)
 */
abstract class AbstractIpp @JvmOverloads constructor(

        @field:Transient var version: Version,
        var opCode: Short,
        var requestId: Int, @field:Transient internal val attributeGroups: MutableList<AttributeGroup>,
        var data: ByteArray = ByteArray(0)) : Externalizable {

    /**
     * This constructor is needed for the [Externalizable] interface.
     */
    protected constructor() : this(Version(2.toByte(), 0.toByte()), 0.toShort(), 0, ArrayList<AttributeGroup>(), ByteArray(0)) {}

    /**
     * Instantiates a new IPP request or response from the given bytes.
     *
     * @param bytes the bytes of the IPP request
     */
    protected constructor(bytes: ByteArray?) : this(ByteBuffer.wrap(bytes)) {}

    /**
     * Instantiates a new IPP request or response from the given bytes.
     *
     * @param bytes the bytes of the IPP request
     */
    constructor(bytes: ByteBuffer) : this(Version(bytes.get(), bytes.get()), bytes.short, bytes.int, readAttributeGroups(bytes),
            readData(bytes)) {
        LOG.debug("IPP package with {} received.", bytes)
        trace(bytes.array())
    }

    private fun trace(bytes: ByteArray) {
        if (LOG.isTraceEnabled) {
            val logDir = Paths.get(SystemUtils.getJavaIoTmpDir().toString(), "IPP")
            recordTo(logDir, bytes)
        } else {
            LOG.debug("{}-{} {} received (use TRACE level to dump it).", javaClass.simpleName, requestId,
                    opCodeAsString)
        }
    }

    /**
     * This method allows you to record a IPP package into a file. Watcht the
     * log to see where the file is stored.
     *
     * @param logDir the directory where the file is stored
     * @since 0.5
     */
    fun recordTo(logDir: Path) {
        recordTo(logDir, toByteArray())
    }

    private fun recordTo(logDir: Path, bytes: ByteArray) {
        recordTo(logDir, bytes,
                this.javaClass.simpleName + requestId + "-" + opCodeAsString + ".ipp")
    }

    /**
     * Returns the 2nd part (byte 2-3) with the operation-id or status-code as
     * string.
     *
     * @return e.g. "Create-Job"
     */
    abstract val opCodeAsString: String

    /**
     * The job-id of the request or response.
     */
    var jobId: Int
        get() {
            val attr = getAttribute("job-id")
            return attr.intValue
        }
        set(id) {
            val attr = Attribute.of("job-id", id)
            setJobAttribute(attr)
        }

    /**
     * Sets and gets the job-state.
     *
     * @since 0.5
     */
    var jobState: JobState
        get() {
            val attr = getAttribute("job-state")
            return JobState.of(attr.intValue)
        }
        set(state) {
            val attr = Attribute.of(ValueTags.ENUM, "job-state", state.value)
            setJobAttribute(attr)
        }

    /**
     * Sets or gets the job-state-rease.
     *
     * @since 0.5
     */
    var jobStateReasons: JobStateReasons
        get() {
            val attr = getAttribute("job-state-reasons")
            return JobStateReasons.of(attr.stringValue)
        }
        set(reason) {
            val attr = Attribute.of(ValueTags.KEYWORD, "job-state-reasons", reason.toString())
            setJobAttribute(attr)
        }

    /**
     * Sets or gets the job-state-message.
     *
     * @since 0.5
     */
    var jobStateMessage: String
        get() {
            val attr = getAttribute("job-state-message")
            return attr.stringValue
        }
        set(msg) {
            val attr = Attribute.of(ValueTags.TEXT_WITHOUT_LANGUAGE, "job-state-message", msg)
            setJobAttribute(attr)
        }

    /**
     * Sets or gets the job-uri.
     *
     * @since 0.5
     */
    var jobURI: URI
        get() {
            val attr = getAttribute("job-uri")
            return attr.uriValue
        }
        set(uri) {
            val attr = Attribute.of("job-uri", uri)
            setJobAttribute(attr)
        }

    /**
     * The fourth field is the "attribute-group" field, and it occurs 0 or
     * more times.
     *
     * @return a list of attribute-groups
     */
    fun getAttributeGroups(): List<AttributeGroup> {
        return attributeGroups
    }

    /**
     * Adds the given group. I.e. group of 'printer-attributs-tag' can be
     * set several times.
     *
     * @param group the group
     * @since 0.5
     */
    fun addAttributeGroup(group: AttributeGroup) {
        attributeGroups.add(group)
    }

    /**
     * Returns all attributes of the given delimiter-tag.
     *
     * @param tag the delimiter-tag
     * @return list of attributes
     */
    fun getAttributeGroup(tag: DelimiterTags): AttributeGroup {
        for (group in getAttributeGroups()) {
            if (group.beginTag == tag) {
                return group
            }
        }
        throw IllegalArgumentException("attribute-group $tag not found")
    }

    /**
     * Gets a collected list of all attributes of the attribute-groups.
     *
     * @return list of all attibutes
     */
    val attributes: List<Attribute>
        get() {
            val attributes: MutableList<Attribute> = ArrayList()
            for (group in attributeGroups) {
                attributes.addAll(group.attributes)
            }
            return attributes
        }

    /**
     * Sets the attribute with the given name. If it exits the old value will
     * be overridden. If it does not exist it will be inserted in the
     * unsupported attribute-groups section with "unknown" as value tag.
     *
     * @param name name of the attribute
     * @param value byte values of the attribute
     * @see .setAttribute
     */
    fun setAttribute(name: String, value: ByteArray) {
        try {
            val attr = getAttribute(name)
            attr.value = value
        } catch (iae: IllegalArgumentException) {
            LOG.debug("Attribute '{}' will be inserted as 'unsupported/unknown', because not found in existing attributes.", name)
            LOG.trace("Details:", iae)
            setAttribute(Attribute.of(ValueTags.UNKNOWN, name, value), DelimiterTags.UNSUPPORTED_ATTRIBUTES_TAG)
        }
    }

    /**
     * Sets the attribute into the group defined by the groupTag.
     *
     * @param attr the attribute
     * @param groupTag defines the attribute-group where the attribute is inserted
     */
    fun setAttribute(attr: Attribute?, groupTag: DelimiterTags) {
        val group = getAttributeGroup(groupTag)
        group.addAttribute(attr)
    }

    /**
     * Gets the attribute with the given name. If not attribute with the given
     * name is not found an [IllegalArgumentException] will be thrown.
     *
     * @param name name of the attribute
     * @return attribute with given name
     */
    fun getAttribute(name: String): Attribute {
        for (attr in attributes) {
            if (name == attr.name) {
                return attr
            }
        }
        throw IllegalArgumentException("no attribute '$name' found")
    }

    /**
     * Checks the attribute with the given name.
     *
     * @param name name of the attribute
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
     * Sets the operation attribute.
     *
     * @param attr attribute
     * @since 0.5
     */
    fun setOperationAttribute(attr: Attribute) {
        setAttribute(attr, DelimiterTags.OPERATION_ATTRIBUTES_TAG)
    }

    /**
     * Sets the job attribute.
     *
     * @param attr attribute
     * @since 0.5
     */
    fun setJobAttribute(attr: Attribute) {
        setAttribute(attr, DelimiterTags.JOB_ATTRIBUTES_TAG)
    }

    /**
     * Gets a collected list of all operation attributes.
     *
     * @return list of operation attibutes
     * @since 0.5
     */
    val operationAttributes: List<Attribute>
        get() = getAttributeGroup(DelimiterTags.OPERATION_ATTRIBUTES_TAG).attributes

    /**
     * Sets or gets the printer-uri attribute. If it is already set it will be
     * overwritten.
     *
     * @since 0.5
     */
    var printerURI: URI
        get() = getAttribute("printer-uri").uriValue
        set(printerURI) {
            val attr = Attribute.of("printer-uri", printerURI)
            setOperationAttribute(attr)
        }

    /**
     * Sets the attributes-charset attribute.
     *
     * @param charset e.g. UTF_8
     * @since 0.5
     */
    fun setAttributesCharset(charset: Charset) {
        val attr = Attribute.of("attributes-charset", charset)
        setOperationAttribute(attr)
    }

    /**
     * Sets the attributes-natural-language attribute.
     *
     * @param locale e.g. GERMANY
     * @since 0.5
     */
    fun setAttributesNaturalLanguage(locale: Locale) {
        val attr = Attribute.of("attributes-natural-language", locale)
        setOperationAttribute(attr)
    }

    /**
     * Sets the requesting-user-name attribute.
     *
     * @param username user name
     * @since 0.5
     */
    fun setRequestingUserName(username: String) {
        val attr = Attribute.of(ValueTags.NAME_WITHOUT_LANGUAGE, "requesting-user-name", username)
        setOperationAttribute(attr)
    }

    /**
     * The data part of the request can be empty.
     *
     * @return false if data part is empty, true otherwise
     */
    fun hasData(): Boolean {
        return data.size > 0
    }

    /**
     * With this toString() implementation we want to provide the most
     * important values which are useful for logging and debugging.
     *
     * @return a String with operation, request-id and other stuff
     */
    override fun toString(): String {
        val buffer = StringBuilder("-")
        buffer.append(requestId)
        for (attr in attributes) {
            buffer.append("|")
            buffer.append(attr)
        }
        buffer.append("|")
        return buildString(buffer.toString())
    }

    /**
     * Provides a shorter version with only the main elements and
     * attributes.
     *
     * @return a string with operation and main operation attributes
     * @since 0.5
     */
    fun toShortString(): String {
        val buffer = StringBuilder("|")
        buffer.append(opCodeAsString)
        buffer.append("-")
        buffer.append(requestId)
        for (attr in operationAttributes) {
            if (!attr.name.startsWith("attributes-")) {
                buffer.append("|")
                buffer.append(attr)
            }
        }
        buffer.append("|")
        return buffer.toString()
    }

    /**
     * Puts all attribute information into the resulting string. The data
     * value may be abbreviated if it is too long. But you can set the log
     * level to TRACE if you want to record the full request.
     *
     * @return string with all attribute values
     */
    fun toLongString(): String {
        val attrs = StringBuilder()
        for (group in attributeGroups) {
            if (!group.attributes.isEmpty()) {
                attrs.append('|').append(group.toLongString())
            }
        }
        return buildString(attrs.substring(1))
    }

    private fun buildString(attrs: String): String {
        var hex = ""
        if (hasData()) {
            hex = StringUtils.abbreviateMiddle(DatatypeConverter.printHexBinary(data), "...", 100) + "|"
        }
        return "|$version|$opCodeAsString$attrs$hex"
    }

    /**
     * Converts the IppResponse to a byte array as described in RFC-2910.
     * <pre>
     * -----------------------------------------------
     * |                  version-number             |   2 bytes  - required
     * -----------------------------------------------
     * |          operation-id or status-code        |   2 bytes  - required
     * -----------------------------------------------
     * |                   request-id                |   4 bytes  - required
     * -----------------------------------------------
     * |                 attribute-group             |   n bytes - 0 or more
     * -----------------------------------------------
     * |              end-of-attributes-tag          |   1 byte   - required
     * -----------------------------------------------
     * |                     data                    |   q bytes  - optional
     * -----------------------------------------------
     * </pre>
     *
     * @return at least 9 bytes
     */
    fun toByteArray(): ByteArray {
        try {
            ByteArrayOutputStream().use { byteStream ->
                writeTo(byteStream)
                byteStream.flush()
                return byteStream.toByteArray()
            }
        } catch (ioe: IOException) {
            throw IllegalStateException("cannot dump package", ioe)
        }
    }

    /**
     * Calculates the size of the request or response.
     *
     * @return length in bytes
     */
    val length: Long
        get() = toByteArray().size.toLong()

    @Throws(IOException::class)
    private fun writeTo(ostream: OutputStream) {
        DataOutputStream(ostream).use { dos ->
            dos.write(version.toByteArray())
            dos.writeShort(opCode.toInt())
            dos.writeInt(requestId)
            for (group in getAttributeGroups()) {
                if (!group.attributes.isEmpty()) {
                    dos.write(group.toByteArray())
                }
            }
            dos.writeByte(DelimiterTags.END_OF_ATTRIBUTES_TAG.value.toInt())
            dos.write(data)
            dos.flush()
        }
    }

    /**
     * The object implements the writeExternal method to save its contents
     * by storing it as byte array.
     *
     * @param out the stream to write the object to
     * @throws IOException Includes any I/O exceptions that may occur
     */
    @Throws(IOException::class)
    override fun writeExternal(out: ObjectOutput) {
        out.write(toByteArray())
    }

    /**
     * The object implements the readExternal method to restore its
     * contents by reading the byte array and use this array to build up
     * an IPP request or response.
     *
     * @param in the stream to read data from in order to restore the object
     * @throws IOException            if I/O errors occur
     */
    @Throws(IOException::class)
    override fun readExternal(`in`: ObjectInput) {
        version = Version(`in`.readByte(), `in`.readByte())
        opCode = `in`.readShort()
        requestId = `in`.readInt()
        ByteArrayOutputStream().use { ostream ->
            while (true) {
                val x = `in`.read()
                if (x < 0) {
                    break
                }
                ostream.write(x)
            }
            ostream.flush()
            val buffer = ByteBuffer.wrap(ostream.toByteArray())
            attributeGroups.addAll(readAttributeGroups(buffer))
            data = readData(buffer)
        }
    }

    /**
     * Validates an request. If it is not valid (e.g. if it has several
     * attribute-groups of the same type) a [ValidationException]
     * will be thrown.
     *
     * @since 0.5.1
     */
    fun validate() {
        validateAttributeGroups()
        if (opCode < 0x0400) {
            validateAttributes()
        }
    }

    private fun validateAttributeGroups() {
        val tags: MutableSet<DelimiterTags> = HashSet()
        for (group in attributeGroups) {
            val beginTag = group.beginTag
            if (tags.contains(beginTag)) {
                throw ValidationException("duplicate '" + beginTag + "' in " + toShortString())
            }
            tags.add(beginTag)
        }
    }

    private fun validateAttributes() {
        for (attr in attributes) {
            val value = attr.value
            if (value.size == 0) {
                throw ValidationException("empty value: $attr")
            }
        }
    }

    /**
     * The binary representation of this class is used to compare to request
     * or respone objects. I.e. for two objects the attributes must be defined
     * not only with same values but also in the same order to be equal.
     *
     * @param other the other response or request
     * @return true if they are equals
     */
    override fun equals(other: Any?): Boolean {
        if (other !is AbstractIpp) {
            return false
        }
        return Arrays.equals(this.toByteArray(), other.toByteArray())
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(data) + requestId + opCode
    }

    /**
     * Container for the version information.
     */
    class Version(major: Byte, minor: Byte) {
        private val bytes = ByteArray(2)

        /**
         * Prints the two bytes as string.
         *
         * @return normally "2.0"
         */
        override fun toString(): String {
            return bytes[0].toString() + "." + bytes[1]
        }

        fun toByteArray(): ByteArray {
            return bytes
        }

        override fun equals(other: Any?): Boolean {
            if (other !is Version) {
                return false
            }
            return Arrays.equals(bytes, other.bytes)
        }

        override fun hashCode(): Int {
            return bytes[0] * 256 + bytes[1]
        }

        /**
         * Expects the first two bytes of an IPP request or response.
         */
        init {
            bytes[0] = major
            bytes[1] = minor
        }
    }



    companion object {

        private val LOG = LoggerFactory.getLogger(AbstractIpp::class.java)

        /** The actual supported version is 2.  */
        @JvmField
        val DEFAULT_VERSION = Version(2.toByte(), 0.toByte())

        private fun fillAttributeGroups(values: MutableList<AttributeGroup>) {
            val requiredTags: ArrayList<DelimiterTags> = ArrayList<DelimiterTags>(Arrays.asList(DelimiterTags.OPERATION_ATTRIBUTES_TAG, DelimiterTags.JOB_ATTRIBUTES_TAG,
                    DelimiterTags.PRINTER_ATTRIBUTES_TAG, DelimiterTags.UNSUPPORTED_ATTRIBUTES_TAG))
            for (group in values) {
                val tag = group.beginTag
                requiredTags.remove(tag)
            }
            for (tag in requiredTags) {
                val group = AttributeGroup(tag)
                values.add(group)
            }
        }

        /**
         * This method is public because it is used also by other methods.
         *
         * @param logDir logging directory
         * @param bytes  data to be recorded
         * @param name   suffix, which is used as filename
         */
        @JvmStatic
        fun recordTo(logDir: Path, bytes: ByteArray, name: String) {
            try {
                Files.createDirectories(logDir)
                val logFile = Paths.get(logDir.toString(),
                        java.lang.Long.toString(System.currentTimeMillis(), Character.MAX_RADIX) + "-" + name)
                Files.write(logFile, bytes)
                LOG.info("IPP package with {} bytes is recorded to '{}'.", bytes.size, logFile)
            } catch (ioe: IOException) {
                LOG.info("Cannot record {} bytes to temporary log file {}.", name, ioe)
                LOG.trace(DatatypeConverter.printHexBinary(bytes))
            }
        }

        private fun readAttributeGroups(buffer: ByteBuffer): MutableList<AttributeGroup> {
            val values: MutableList<AttributeGroup> = ArrayList()
            while (buffer.remaining() > 4) {
                val pos = buffer.position()
                if (DelimiterTags.END_OF_ATTRIBUTES_TAG == DelimiterTags.of(buffer[pos].toInt())) {
                    break
                }
                values.add(AttributeGroup(buffer))
            }
            return values
        }

        private fun readData(buffer: ByteBuffer): ByteArray {
            val endOfAttributeTag = DelimiterTags.of(buffer.get().toInt())
            LOG.trace("{} was read (and ignored).", endOfAttributeTag)
            val bytes = ByteArray(buffer.remaining())
            buffer[bytes]
            return bytes
        }
    }

    init {
        fillAttributeGroups(attributeGroups)
    }

}