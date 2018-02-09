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

import java.math.BigInteger;
import java.util.Arrays;

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
    
    private final byte[] bytes;

    /**
     * Instantiates a new IPP request from the given bytes.
     *
     * @param bytes the bytes of the IPP request
     */
    public IppRequest(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * Gets the first part (byte 0-1) with the supported IPP version. This is a
     * number from 1.0 till 1.4 for version 1 or 2.0 for version 2.
     *
     * @return e.g. "2.0"
     */
    public String getVersion() {
        return bytes[0] + "." + bytes[1];
    }

    /**
     * Returns the 2nd part (byte 2-3) with the operation-id.
     *
     * @return e.g. {@link IppOperations#CREATE_JOB}
     */
    public IppOperations getOperation() {
        return IppOperations.of(getAsInt(2, 3));
    }

    /**
     * Retunns the 3rd part (byte 4-7) of the request which
     * contains the request-id.
     * 
     * @return the request-id
     */
    public int getRequestId() {
        return getAsInt(4, 7);
    }

    private int getAsInt(int start, int end) {
        byte[] subbytes = Arrays.copyOfRange(bytes, start, end+1);
        return new BigInteger(subbytes).intValue();
    }

}
