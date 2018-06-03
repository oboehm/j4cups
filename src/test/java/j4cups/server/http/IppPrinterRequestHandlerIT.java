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
 * (c)reated 03.06.18 by oliver (ob@oasd.de)
 */
package j4cups.server.http;

import j4cups.client.CupsClient;
import j4cups.server.AbstractServerTest;
import j4cups.server.CupsServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Integration tests for {@link IppPrinterRequestHandler}.
 */
final class IppPrinterRequestHandlerIT extends AbstractIppRequestHandlerTest {

    private static final Logger LOG = LoggerFactory.getLogger(IppPrinterRequestHandlerIT.class);
    private static CupsServer cupsServer;
    private static CupsClient cupsClient;
    private static URI printerURI;

    @BeforeAll
    static void startCupsServer() {
        cupsServer = AbstractServerTest.startServer();
        LOG.info("{} was started.", cupsServer);
        cupsClient = new CupsClient(URI.create("http://localhost:" + cupsServer.getPort()));
        printerURI = URI.create("http://localhost:" + cupsServer.getPort() + "/printers/test-printer");
    }

    @Test
    void testPrintJob() {
        cupsClient.print(printerURI, Paths.get("src", "test", "resources", "j4cups", "test.txt"));
    }

    @Test
    void testSendDocuments() {
        Path testFile = Paths.get("src", "test", "resources", "j4cups", "test.txt");
        cupsClient.print(printerURI, testFile, testFile);
    }

    @AfterAll
    static void stopCupsServer() {
        cupsServer.shutdown();
    }

}