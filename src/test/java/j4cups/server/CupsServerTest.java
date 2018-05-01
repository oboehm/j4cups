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
 * (c)reated 27.03.2018 by oboehm (ob@oasd.de)
 */
package j4cups.server;


import j4cups.protocol.IppRequest;
import j4cups.server.http.IppEntity;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link CupsServer}.
 */
class CupsServerTest extends AbstractServerTest {

    private static final Logger LOG = LoggerFactory.getLogger(CupsServerTest.class);
    private static final CupsServer SERVER = new CupsServer(6310);
    private static final URI PRINTER_URI = URI.create("http://localhost:" + SERVER.getPort() + "/printers/text");
    private final HttpPost httpPost = new HttpPost("http://localhost:" + SERVER.getPort());

    /**
     * For the unit tests we start the server here.
     */
    @BeforeAll
    public static void startServer() {
        assertFalse(SERVER.isStarted());
        SERVER.start();
        LOG.info("{} is started.", SERVER);
    }
    
    /**
     * Test method for {@link CupsServer#start()}.
     */
    @Test
    public void testIsStarted() {
        assertTrue(SERVER.isStarted());
    }

    /**
     * As a first test we send an invalid request with "hello" as content.
     *
     * @throws IOException e.g. in case of network prolblems
     */
    @Test
    public void testSendInvalidRequest() throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            httpPost.setEntity(new StringEntity("hello"));
            CloseableHttpResponse response = client.execute(httpPost);
            assertEquals(400, response.getStatusLine().getStatusCode());
        }
    }

    /**
     * As a second test we send an valid request.
     *
     * @throws IOException e.g. in case of network prolblems
     */
    @Test
    public void testSendRequest() throws IOException {
        IppRequest getJobsReqeust = readIppRequest("Get-Jobs.bin", PRINTER_URI);
        httpPost.setEntity(new IppEntity(getJobsReqeust));
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            CloseableHttpResponse response = client.execute(httpPost);
            assertEquals(200, response.getStatusLine().getStatusCode());
        }
    }


    /**
     * This is the same test as before except that we ask the CupsServer as
     * printer.
     *
     * @throws IOException e.g. in case of network prolblems
     */
    @Test
    public void testSendRequestToPrinter() throws IOException {
        assertTrue(isOnline(PRINTER_URI));
        HttpPost httpPrinterPost = new HttpPost(PRINTER_URI);
        IppRequest getJobsRequest = readIppRequest("Get-Jobs.bin", PRINTER_URI);
        httpPrinterPost.setEntity(new IppEntity(getJobsRequest));
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            CloseableHttpResponse response = client.execute(httpPrinterPost);
            assertEquals(200, response.getStatusLine().getStatusCode());
        }
    }

    /**
     * We ask the CUPS server for the registered printers.
     *
     * @throws IOException e.g. in case of network problems
     */
    @Test
    public void testSendGetPrinters() throws IOException {
        IppRequest getPrintersRequest = readIppRequest("Get-Printers.bin");
        httpPost.setEntity(new ByteArrayEntity(getPrintersRequest.toByteArray()));
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            CloseableHttpResponse response = client.execute(httpPost);
            assertEquals(200, response.getStatusLine().getStatusCode());
            byte[] content = IOUtils.toByteArray(response.getEntity().getContent());
            assertThat(content.length, greaterThan(10));
            LOG.info("{} bytes received.", content.length);
        }
    }

    /**
     * We want to see a useful toString implementation.
     */
    @Test
    public void testToString() {
        String s = SERVER.toString();
        assertThat(s, containsString(Integer.toString(SERVER.getPort())));
    }

    /**
     * At the end of the tests we shut down the server. And we test if it is
     * really down.
     */
    @AfterAll
    public static void shutdownServer() {
        SERVER.shutdown();
        LOG.info("{} is shut down.", SERVER);
        assertFalse(isOnline("localhost", SERVER.getPort()));
        LOG.info("{} is offline (as expected).", SERVER);
    }

}
