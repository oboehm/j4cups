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

import j4cups.protocol.IppRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for class {@link CupsClient}. For tests with a real I used
 * http://localhost:631/printers/Brother_MFC_J5910DW_2 in my local home
 * network.
 *
 * @author oliver (boehm@javatux.de)
 */
class CupsClientTest extends AbstractServerTest {

    private static final Logger LOG = LoggerFactory.getLogger(CupsClientTest.class);
    private static final CupsClient CLIENT = new CupsClient(URI.create("http://localhost:631"));

    /**
     * Unit test for {@link CupsClient#send(IppRequest)}.
     *
     * @throws IOException the io exception
     */
    @Test
    public void sendGetPrinters() throws IOException {
        IppRequest getPrintersRequest = readIppRequest("Get-Printers.bin");
        CloseableHttpResponse response = CLIENT.send(getPrintersRequest);
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    /**
     * Another nit test for {@link CupsClient#send(IppRequest)}.
     *
     * @throws IOException the io exception
     */
    @Test
    public void sendGetJobs() throws IOException {
        IppRequest jobsRequest = readIppRequest("Get-Jobs.bin");
        CloseableHttpResponse response = CLIENT.send(jobsRequest);
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @AfterAll
    public static void closeClient() throws IOException {
        CLIENT.close();
    }

}
