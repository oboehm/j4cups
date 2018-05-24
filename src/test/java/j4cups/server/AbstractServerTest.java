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
 * (c)reated 27.04.18 by oliver (ob@oasd.de)
 */

package j4cups.server;

import j4cups.protocol.AbstractIppTest;
import j4cups.protocol.IppRequest;
import org.junit.jupiter.api.AfterAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Class AbstractServerTest has some common methods which are used in the
 * different sub test classes.
 *
 * @author oliver
 */
public abstract class AbstractServerTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractServerTest.class);
    protected static CupsServer cupsServer;

    /**
     * For the unit tests we can start the server here.
     */
    protected static void startServer() {
        int port = 1024 + (int) (System.currentTimeMillis() % 8000);
        while (isOnline("localhost", port)) {
            port++;
        }
        cupsServer = new CupsServer(port);
        assertFalse(cupsServer.isStarted());
        cupsServer.start();
        LOG.info("{} is started.", cupsServer);
    }

    /**
     * Reads a recorded request and checks if the needed CUPS server or printer
     * is available. If not the test will be ignored.
     *
     * @param filename of the request resource
     * @return recorded IPP request
     */
    protected static IppRequest readIppRequest(String filename) {
        IppRequest ippRequest = AbstractIppTest.readIppRequest("request", filename);
        URI printerURI = ippRequest.getPrinterURI();
        return checkIppPrinterURI(printerURI, ippRequest);
    }

    /**
     * Reads a recorded request and replaces the printerURI in the recorded
     * request with the given printer-uri.
     *
     * @param filename   of the request resource
     * @param printerURI the printer uri which will be used
     * @return recorded IPP request with replaces printer-uri
     */
    protected static IppRequest readIppRequest(String filename, URI printerURI) {
        IppRequest ippRequest = AbstractIppTest.readIppRequest("request", filename);
        return checkIppPrinterURI(printerURI, ippRequest);
    }

    private static IppRequest checkIppPrinterURI(URI printerURI, IppRequest ippRequest) {
        assumeTrue(isOnline(printerURI), printerURI + " is not available");
        LOG.debug("Test is executed because {} is available.", printerURI);
        ippRequest.setPrinterURI(printerURI);
        ippRequest.setPrinterURI(printerURI);
        return ippRequest;
    }

    /**
     * Some tests need to know if the CUPS server for testing is available.
     * Use this method to check it.
     *
     * @param uri to the CUPS server or printer
     * @return true or false
     */
    public static boolean isOnline(URI uri) {
        if ("file".equals(uri.getScheme())) {
            Path dir = Paths.get(uri);
            return Files.isDirectory(dir);
        } else {
            int port = uri.getPort();
            return isOnline(uri.getHost(), port == -1 ? 631 : port);
        }
    }

    /**
     * Some tests need to know if the CUPS server for testing is available.
     * Use this method to check it.
     *
     * @param host CUPS server or printer
     * @param port e.g. 631
     * @return true or false
     */
    public static boolean isOnline(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 200);
            LOG.debug("Socket {} for {}:{} is created.", socket, host, port);
            return socket.isConnected();
        } catch (IOException ex) {
            LOG.info("Cannot connect to {}:{} ({}).", host, port, ex.getMessage());
            LOG.debug("Details:", ex);
            return false;
        }
    }

    /**
     * At the end of the tests we shut down the server. And we test if it is
     * really down.
     */
    @AfterAll
    public static void shutdownServer() {
        if (cupsServer != null) {
            cupsServer.shutdown();
            LOG.info("{} is shut down.", cupsServer);
            assertFalse(isOnline("localhost", cupsServer.getPort()));
            LOG.info("{} is offline (as expected).", cupsServer);
        }
    }
}
