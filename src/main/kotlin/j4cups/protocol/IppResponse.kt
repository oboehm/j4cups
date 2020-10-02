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
package j4cups.protocol

import j4cups.protocol.StatusCode
import j4cups.protocol.StatusCode.Companion.of
import j4cups.protocol.attr.Attribute
import j4cups.protocol.attr.AttributeGroup
import j4cups.protocol.tags.ValueTags
import org.slf4j.LoggerFactory
import java.util.*

/**
 * The IppResponse represents an IPP response as is defined in RFC-2910.
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
 * @since 0.0.1 (09.02.2018)
 */
class IppResponse : AbstractIpp {

    /**
     * This constructor is needed for the [java.io.Externalizable]
     * interface.
     */
    constructor() : super() {}

    /**
     * Instantiates a new IPP response from the given bytes.
     *
     * @param bytes the bytes of the IPP request
     */
    constructor(bytes: ByteArray) : super(bytes) {}

    /**
     * The IppResponse is the response to a IppRequest. So you need the id
     * of the IppRequest to create a response.
     *
     * @param request the request for this response
     */
    constructor(request: IppRequest) : super(DEFAULT_VERSION, StatusCode.SUCCESSFUL_OK.code, request.requestId, fillAttributesGroupsFor(request)) {}

    /**
     * Returns the 2nd part (byte 2-3) with the status-code.
     *
     * @return e.g. [StatusCode.SUCCESSFUL_OK]
     */
    var statusCode: StatusCode
        get() = of(opCode.toInt())
        set(code) {
            opCode = code.code
        }

    /**
     * In case of a bad request or other error status you can set or get the
     * content of the attribute 'status-message'.
     *
     * @since 0.5
     */
    var statusMessage: String
        get() {
            val name = "status-message"
            return if (hasAttribute(name)) {
                getAttribute(name).stringValue
            } else {
                LOG.info("{} has no attribute '{}'.", this, name)
                "unknown"
            }
        }
        set(message) {
            val attr = Attribute.of(ValueTags.TEXT_WITHOUT_LANGUAGE, "status-message", message)
            setOperationAttribute(attr)
        }

    /**
     * Returns the 2nd part (byte 2-3) with the status-code as string.
     *
     * @return e.g. "successful-ok"
     */
    override fun getOpCodeAsString(): String {
        return statusCode.toString()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(IppResponse::class.java)
        private fun fillAttributesGroupsFor(request: IppRequest): List<AttributeGroup> {
            val groups: MutableList<AttributeGroup> = ArrayList()
            for (g in request.attributeGroups) {
                groups.add(AttributeGroup(g))
            }
            return groups
        }
    }

}