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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express orimplied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * (c)reated 05.09.18 by oliver (ob@oasd.de)
 */
package j4cups.protocol;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit-Tests fuer {@link IppRequestException}.
 *
 * @author <a href="ob@aosd.de">oliver</a>
 * @since (05.09.18)
 */
public final class IppRequestExceptionTest {

    private static final IppResponse IPP_RESPONSE = new IppResponse();

    @BeforeAll
    static void setUpIppResponse() {
        IPP_RESPONSE.setStatusCode(StatusCode.CLIENT_ERROR_BAD_REQUEST);
        IPP_RESPONSE.setStatusMessage("oh no");
    }

    @Test
    public void testGetResponse() {
        IppRequestException ex = new IppRequestException(IPP_RESPONSE);
        assertEquals(IPP_RESPONSE, ex.getResponse());
    }

    @Test
    public void testGetCause() {
        Throwable cause = new IllegalArgumentException("illegal ikea regal");
        IppRequestException ex = new IppRequestException(IPP_RESPONSE, cause);
        assertEquals(cause, ex.getCause());
    }

}