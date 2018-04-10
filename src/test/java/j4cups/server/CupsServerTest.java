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


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link CupsServer}.
 */
public class CupsServerTest {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CupsServerTest.class);
    private static final CupsServer SERVER = new CupsServer(6310);
    private static Thread serverThread;

    @BeforeAll
    public static void startServer() {
        serverThread = SERVER.start();
        LOG.info("{} is started.", SERVER);
    }
    
    /**
     * Test method for {@link CupsServer#start()}.
     * 
     * @throws IOException e.g. in case of network prolblems
     */
    @Test
    public void testIsStarted() throws IOException {
        Socket socket = new Socket("localhost", SERVER.getPort());
        assertTrue("not connected to " + SERVER, socket.isConnected());
    }
    
    @AfterAll
    public static void shutdownServer() {
        serverThread.interrupt();
        LOG.info("{} is shut down.", SERVER);
    }

}
