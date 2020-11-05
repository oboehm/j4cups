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
 * (c)reated 30.05.2018 by oboehm (ob@oasd.de)
 */
package j4cups.protocol

/**
 * The class IppRequestException can be thrown if there is a problem with
 * an [IppRequest].
 *
 * @author oboehm
 * @since 0.5 (30.05.2018)
 */
class IppRequestException : RuntimeException {

    /**
     * Gets the error response of the request
     *
     * @return the IPP response
     */
    val response: IppResponse

    /**
     * Constructs a new runtime exception with the specified IPP response.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to [.initCause].
     *
     * @param ippResponse the response which was received with a problematic
     * [IppRequest]
     */
    constructor(ippResponse: IppResponse) : super(asString(ippResponse)) {
        response = ippResponse
    }

    /**
     * Constructs a new runtime exception with the specified IPP response
     * and cause.
     *
     * @param ippResponse the response which was received with a problematic
     * [IppRequest]
     * @param cause   the cause (which is saved for later retrieval by the
     * [.getCause] method). (A null value is
     * permitted, and indicates that the cause is nonexistent or
     * unknown.)
     */
    constructor(ippResponse: IppResponse, cause: Throwable?) : super(asString(ippResponse), cause) {
        response = ippResponse
    }

    companion object {
        private fun asString(response: IppResponse): String {
            return response.statusCode.toString() + " - " + response.statusMessage
        }
    }

}