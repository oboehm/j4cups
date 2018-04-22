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


import j4cups.protocol.AbstractIppTest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link CupsServer}.
 */
class CupsServerTest {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CupsServerTest.class);
    private static final CupsServer SERVER = new CupsServer(6310);
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
        CloseableHttpClient client = HttpClients.createDefault();
        httpPost.setEntity(new StringEntity("hello"));
        CloseableHttpResponse response = client.execute(httpPost);
        assertEquals(400, response.getStatusLine().getStatusCode());
        client.close();
    }

    /**
     * As a second test we send an valid request.
     *
     * @throws IOException e.g. in case of network prolblems
     */
    @Test
    public void testSendRequest() throws IOException {
        httpPost.setEntity(new ByteArrayEntity(AbstractIppTest.REQUEST_GET_JOBS.toByteArray()));
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = client.execute(httpPost);
        assertEquals(200, response.getStatusLine().getStatusCode());
        client.close();
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
        try (Socket socket = new Socket("localhost", SERVER.getPort())) {
            assertFalse("yet connected to " + SERVER, socket.isConnected());
        } catch(IOException expected)  {
            LOG.info("{} is down (as expected).", SERVER);
            LOG.debug("Details:", expected);
        }
    }

}
