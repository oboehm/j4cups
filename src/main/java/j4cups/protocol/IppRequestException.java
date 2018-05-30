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
 * (c)reated 30.05.2018 by oboehm (ob@oasd.de)
 */
package j4cups.protocol;

/**
 * The class IppRequestException can be thrown if there is a problem with
 * an {@link IppRequest}.
 *
 * @author oboehm
 * @since 0.5 (30.05.2018)
 */
public class IppRequestException extends RuntimeException {

    /**
     * Constructs a new runtime exception with the specified IPP response.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param ippResponse the response which was received with a problematic
     *                    {@link IppRequest}
     */
    public IppRequestException(IppResponse ippResponse) {
        super(asString(ippResponse));
    }

    /**
     * Constructs a new runtime exception with the specified IPP response
     * and cause.
     *
     * @param ippResponse the response which was received with a problematic
     *                    {@link IppRequest}
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A <tt>null</tt> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     */
    public IppRequestException(IppResponse ippResponse, Throwable cause) {
        super(asString(ippResponse), cause);
    }
    
    private static String asString(IppResponse response) {
        return response.getStatusCode() + " - " + response.getStatusMessage();
    }

}
