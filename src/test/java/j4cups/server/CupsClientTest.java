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
 * (c)reated 26.04.18 by oliver (ob@oasd.de)
 */

package j4cups.server;

import j4cups.protocol.AbstractIppTest;
import j4cups.protocol.IppRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for class {@link CupsClient}.
 *
 * @author oliver (boehm@javatux.de)
 */
class CupsClientTest {

    private final CupsClient client = new CupsClient(URI.create("http://localhost:631"));

    /**
     * Unit test for {@link CupsClient#send(IppRequest)}.
     *
     * @throws IOException the io exception
     */
    @Test
    public void sendGetPrinters() throws IOException {
        IppRequest getPrintersRequest = AbstractIppTest.readIppRequest("request", "Get-Printers.bin");
        CloseableHttpResponse response = client.send(getPrintersRequest);
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

}
