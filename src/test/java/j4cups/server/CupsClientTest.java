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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for class {@link CupsClient}.
 *
 * @author oliver (boehm@javatux.de)
 */
class CupsClientTest {

    private static final Logger LOG = LoggerFactory.getLogger(CupsClientTest.class);
    private final CupsClient client = new CupsClient(URI.create("http://localhost:631"));

    /**
     * Unit test for {@link CupsClient#send(IppRequest)}.
     *
     * @throws IOException the io exception
     */
    @Test
    public void sendGetPrinters() throws IOException {
        if (isOnline("localhost", 631)) {
            IppRequest getPrintersRequest = AbstractIppTest.readIppRequest("request", "Get-Printers.bin");
            CloseableHttpResponse response = client.send(getPrintersRequest);
            assertEquals(200, response.getStatusLine().getStatusCode());
        } else {
            LOG.info("Test 'sendGetPrinters' is SKIPPED because localhost:631 is not reachable.");
        }
    }

    private static boolean isOnline(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            LOG.debug("Socket {} for {}:{} is created.", socket, host, port);
            return socket.isConnected();
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException("invalid host: " + host, ex);
        } catch (IOException ex) {
            LOG.info("Cannot connect to {}:{} ({}).", host, port, ex.getMessage());
            LOG.debug("Details:", ex);
            return false;
        }
    }

}
