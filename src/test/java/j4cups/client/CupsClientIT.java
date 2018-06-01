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
 * (c)reated 29.05.18 by oliver (ob@oasd.de)
 */
package j4cups.client;

import j4cups.op.OperationTest;
import j4cups.protocol.IppRequestException;
import j4cups.protocol.IppResponse;
import j4cups.protocol.StatusCode;
import j4cups.server.AbstractServerTest;
import j4cups.server.IppHandlerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for {@link CupsClient}. To specify a printer for
 * testing use
 * <pre>
 *     -DprinterURI=http://localhost:631/printers/Brother_MFC_J5910DW_2
 * </pre>
 * <p>
 * to set it. Otherwise the tests would be skipped.
 * </p>
 */
final class CupsClientIT {

    private static final Logger LOG = LoggerFactory.getLogger(IppHandlerTest.class);
    private CupsClient cupsClient;
    private URI printerURI;

    @BeforeEach
    public void setUpCupsClient() {
        cupsClient = getCupsClient();
        printerURI = getPrinterURI();
    }

    /**
     * Creates a job for a printer.
     */
    @Test
    void testCreateJob() {
        IppResponse ippResponse = cupsClient.createJob(printerURI);
        cancelJob(ippResponse);
        OperationTest.checkIppResponse(ippResponse, "Create-Jobs.bin");
    }

    /**
     * Tests the print-job operation by sending one document using the
     * {@link CupsClient#print(URI, Path)} method.
     */
    @Test
    public void testPrintJob() {
        Path file = Paths.get("src/test/resources/j4cups/test.txt");
        assertTrue(Files.exists(file));
        IppResponse ippResponse = cupsClient.print(printerURI, file);
        cancelJob(ippResponse);
        assertEquals(StatusCode.SUCCESSFUL_OK, ippResponse.getStatusCode());
    }

    /**
     * Tests the send-document operation by sending two documents using the
     * {@link CupsClient#print(URI, Path...)} method.
     */
    @Test
    public void testPrintDocuments() {
        Path file = Paths.get("src/test/resources/j4cups/test.txt");
        assertTrue(Files.exists(file));
        IppResponse ippResponse = cupsClient.print(printerURI, file, file);
        cancelJob(ippResponse);
        assertEquals(StatusCode.SUCCESSFUL_OK, ippResponse.getStatusCode());
    }

    private void cancelJob(IppResponse ippResponse) {
        int jobId = ippResponse.getJobId();
        try {
            cupsClient.cancelJob(jobId, printerURI);
        } catch (IppRequestException ex) {
            LOG.warn("Cannot cancel job {}: {}", jobId, ex);
        }
    }

    private static URI getPrinterURI() {
        String printer = System.getProperty("printerURI");
        assumeTrue(printer != null, "specify printer with '-DprinterURI=...'");
        return URI.create(printer);
    }

    /**
     * If you have no CUPS running on your local machine you must set the
     * envrionment variable 'forwardURI' to your CUPS server in the
     * network. Otherwise the test fails.
     *
     * @return your CupsClient for testing
     */
    public static CupsClient getCupsClient() {
        URI cupsURI = URI.create(System.getProperty("forwardURI", "http://localhost:631"));
        assumeTrue(AbstractServerTest.isOnline(cupsURI),
                cupsURI + " is offline - use '-DcupsURI=...' to use another CUPS");
        return new CupsClient(cupsURI);
    }

}
