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
 * (c)reated 09.02.2018 by oboehm (ob@oasd.de)
 */
package j4cups.protocol

import java.nio.ByteBuffer

/**
 * The IppRequest represents an IPP request as is defined in RFC-2910.
 * <pre>
 * -----------------------------------------------
 * |                  version-number             |   2 bytes  - required
 * -----------------------------------------------
 * |               operation-id (request)        |   2 bytes  - required
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
class IppRequest : AbstractIpp {

    /**
     * This constructor is needed for the [java.io.Externalizable]
     * interface.
     */
    constructor() : super() {}

    /**
     * Instantiates a new IPP request from the given bytes.
     *
     * @param bytes the bytes of the IPP request
     */
    constructor(bytes: ByteArray) : this(ByteBuffer.wrap(bytes)) {}

    /**
     * Instantiates a new IPP request from the given bytes.
     *
     * @param bytes the bytes of the IPP request
     */
    constructor(bytes: ByteBuffer) : super(bytes) {}

    /**
     * Returns the 2nd part (byte 2-3) with the operation-id.
     *
     * @return e.g. [IppOperations.CREATE_JOB]
     */
    val operation: IppOperations
        get() = IppOperations.of(super.opCode.toInt())

    /**
     * Returns the 2nd part (byte 2-3) with the operation-id as string.
     *
     * @return e.g. "Create-Job"
     */
    override val opCodeAsString: String
        get() = when (operation) {
            IppOperations.ADDITIONAL_REGISTERED_OPERATIONS, IppOperations.RESERVED_FOR_VENDOR_EXTENSIONS -> String.format("0x%04x", opCode)
            else -> operation.toString()
        }

}